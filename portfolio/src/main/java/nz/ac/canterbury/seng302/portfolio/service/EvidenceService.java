package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;


/**
 * A utility class for more complex actions involving Evidence
 */
@Service
public class EvidenceService {

    /** For logging the flow of evidence service events */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** For retrieving information about the users. */
    private final UserAccountsClientService userAccountsClientService;

    /** For checking evidence occurs within the project. */
    private final ProjectRepository projectRepository;

    /** For persisting CRUD operations on pieces of evidence. */
    private final EvidenceRepository evidenceRepository;

    /** For persisting CRUD operations on weblinks. */
    private final WebLinkRepository webLinkRepository;

    /** For persisting CRUD operations on skills. */
    private final SkillRepository skillRepository;

    /** For validating inputs against the central regex. */
    private final RegexService regexService;

    /** For all services related to skill frequency */
    private final SkillFrequencyService skillFrequencyService;


    /**
     * Autowired constructor for injecting the required dependencies.
     *
     * @param userAccountsClientService for retrieving information about the users.
     * @param projectRepository for checking evidence occurs within the project.
     * @param evidenceRepository for persisting CRUD operations on pieces of evidence.
     * @param webLinkRepository for persisting CRUD operations on weblinks.
     * @param skillRepository for persisting CRUD operations on skills.
     * @param regexService for validating inputs against the central regex.
     * @param skillFrequencyService for all services related to skill frequency.
     */
    @Autowired
    public EvidenceService(
            UserAccountsClientService userAccountsClientService,
            ProjectRepository projectRepository,
            EvidenceRepository evidenceRepository,
            WebLinkRepository webLinkRepository,
            SkillRepository skillRepository,
            RegexService regexService,
            SkillFrequencyService skillFrequencyService
    ) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.skillRepository = skillRepository;
        this.regexService = regexService;
        this.skillFrequencyService = skillFrequencyService;
    }


    /**
     * Creates a new evidence object and saves it to the repository. Adds and saves any web link objects and categories
     * to the evidence object.
     *
     * @param principal The authentication principal
     * @return The evidence object, after it has been added to the database.
     * @throws MalformedURLException When one of the web links has a malformed url
     * @throws CheckException when one or more variables fail the validation
     */
    public Evidence addEvidence(Authentication principal,
                                EvidenceDTO evidenceDTO) throws MalformedURLException, CheckException {
        logger.info("CREATING EVIDENCE - Attempting to create evidence with title: {}", evidenceDTO.getTitle());
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        checkValidEvidenceDTO(user, evidenceDTO);

        evidenceDTO.setAssociateIds(evidenceDTO.getAssociateIds() == null ?
                new ArrayList<>() :
                new ArrayList<>(new LinkedHashSet<>(evidenceDTO.getAssociateIds())));
        evidenceDTO.addAssociatedId(user.getId());

        return createEvidenceForUsers(evidenceDTO, evidenceDTO.getAssociateIds());
    }


    /**
     * Updates the evidence object with the same evidence ID and saves it to the repository.
     *
     * Also creates new evidence objects with the same details for any associated users who are newly added.
     *
     * @param principal the authentication principal of the user making the update.
     * @param evidenceDTO the evidenceDto containing the details of the evidence update.
     * @return the piece of evidence created for the user.
     * @throws MalformedURLException when one of the web links has a malformed url
     * @throws CheckException when one or more variables fail the validation
     */
    public Evidence editEvidence(Authentication principal,
                                 EvidenceDTO evidenceDTO) throws MalformedURLException, CheckException, DateTimeParseException {
        logger.info("EDITING EVIDENCE - Attempting to edit evidence with title: {}", evidenceDTO.getTitle());
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        Optional<Evidence> optionalOriginalEvidence = evidenceRepository.findById(evidenceDTO.getId());
        if (optionalOriginalEvidence.isEmpty()) {
            String message = "No evidence found with id " + evidenceDTO.getId();
            logger.warn("Failed to edit evidence with message - {}", message);
            throw new CheckException(message);
        }
        Evidence originalEvidence = optionalOriginalEvidence.get();
        if (originalEvidence.getUserId() != user.getId()) {
            throw new CheckException("Cannot edit evidence owned by a different user");
        }
        checkValidEvidenceDTO(user, evidenceDTO);
        evidenceDTO.setAssociateIds(evidenceDTO.getAssociateIds() == null ?
                                    new ArrayList<>() :
                                    new ArrayList<>(new LinkedHashSet<>(evidenceDTO.getAssociateIds())));
        evidenceDTO.addAssociatedId(user.getId());

        List<Integer> unassociatedUsers = getUnassociatedUsers(originalEvidence, evidenceDTO.getAssociateIds());
        if (!unassociatedUsers.isEmpty()) {
            createEvidenceForUsers(evidenceDTO, unassociatedUsers);
        }
        return updateExistingEvidence(originalEvidence, evidenceDTO);
    }


    /**
     * Validates the values of a EvidenceDTO, if any issues are found, a Check Exception is thrown
     *
     * @param evidenceDTO the evidenceDTO to be validated
     * @throws CheckException an exception containing the message why the validation failed.
     */
    protected void checkValidEvidenceDTO(UserResponse user, EvidenceDTO evidenceDTO) throws CheckException, DateTimeParseException {
        long projectId = evidenceDTO.getProjectId();
        List<WebLinkDTO> webLinks = evidenceDTO.getWebLinks();
        String date = evidenceDTO.getDate();

        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new CheckException("Project Id does not match any project");
        }
        if (webLinks.size() > 10) {
            throw new CheckException("This piece of evidence has too many weblinks attached to it; 10 is the limit");
        }
        for (Skill skillInfo : evidenceDTO.getSkills()) {
            if (skillInfo.getId() != null) {
                Optional<Skill> optionalSkill = skillRepository.findDistinctByEvidenceUserIdAndId(user.getId(), skillInfo.getId());
                if (optionalSkill.isEmpty()) {
                    throw new CheckException("Could not retrieve one or more skills");
                }
            }
        }

        Project project = optionalProject.get();
        LocalDate localDate = LocalDate.parse(date);
        checkDate(project, localDate);

        regexService.checkInput(RegexPattern.GENERAL_UNICODE, evidenceDTO.getTitle(), 5, 50, "Title");
        regexService.checkInput(RegexPattern.GENERAL_UNICODE, evidenceDTO.getDescription(), 5, 500, "Description");
    }


    /**
     * Creates a piece of evidence for each of the users in the given list.
     *
     * @param evidenceDTO the evidenceDto with all the attributes of the evidence to be made.
     * @param userIds the userIds of the users that are getting the new evidence.
     * @return The last piece of evidence created.
     * @throws MalformedURLException when a weblink is invalid.
     */
    public Evidence createEvidenceForUsers(EvidenceDTO evidenceDTO, List<Integer> userIds) throws MalformedURLException, CheckException  {
        Evidence ownerEvidence = null;
        for (Integer ownersId : userIds) {
            checkAssociateId(ownersId);
            ownerEvidence = addEvidenceForUser(ownersId, evidenceDTO);
            addWeblinks(ownerEvidence, evidenceDTO.getWebLinks());
            addSkills(ownerEvidence, evidenceDTO.getSkills());
        }
        return ownerEvidence;
    }


    /**
     * Used for editing a piece of evidence, this method updated an existing piece of evidence,
     * keeping its ID the same. On successful modification, the evidence is saved and returned.
     *
     * @param originalEvidence the piece of evidence to be modified in place.
     * @param evidenceDTO an evidenceDto object with all the required updates.
     * @return the newly updated piece of evidence.
     * @throws MalformedURLException when the URL is not parse correctly.
     */
    private Evidence updateExistingEvidence(Evidence originalEvidence, EvidenceDTO evidenceDTO) throws CheckException {
        logger.info("Updating evidence details for evidence {}", originalEvidence.getId());

        originalEvidence.setTitle(evidenceDTO.getTitle());
        originalEvidence.setDescription(evidenceDTO.getDescription());
        originalEvidence.setDate(LocalDate.parse(evidenceDTO.getDate()));
        originalEvidence.clearCategories();

        addCategoriesToEvidence(originalEvidence, evidenceDTO.getCategories());

        originalEvidence.clearSkills();
        addSkills(originalEvidence, evidenceDTO.getSkills());

        originalEvidence.clearWeblinks();
        addWeblinks(originalEvidence, evidenceDTO.getWebLinks());

        originalEvidence.clearAssociatedIds();
        for (Integer userId : evidenceDTO.getAssociateIds()) {
            originalEvidence.addAssociateId(userId);
        }
        return evidenceRepository.save(originalEvidence);
    }


    /**
     * Helper method that adds a piece of evidence to the specified user id.
     *
     * @param userId the user to have the evidence added for.
     * @param evidenceDTO an evidence dto which holds the attributes of the new evidence
     * @return The created piece of evidence
     */
    private Evidence addEvidenceForUser(int userId, EvidenceDTO evidenceDTO) {
        logger.info("CREATING EVIDENCE - attempting to create evidence for user: {}", userId);
        Evidence evidence = new Evidence(userId, evidenceDTO.getTitle(), LocalDate.parse(evidenceDTO.getDate()), evidenceDTO.getDescription());

        addCategoriesToEvidence(evidence, evidenceDTO.getCategories());
        logger.info("Adding associate IDs: {}", evidenceDTO.getAssociateIds());
        for (Integer associate : evidenceDTO.getAssociateIds()) {
            checkAssociateId(associate);
            evidence.addAssociateId(associate);
        }
        return evidenceRepository.save(evidence);
    }


    /**
     * Adds the given categories to the supplied piece of evidence.
     *
     * @param evidence the piece of evidence to add the categories to.
     * @param categories a list of strings containing string representations of the categories.
     */
    private void addCategoriesToEvidence(Evidence evidence, List<String> categories) {
        for (String categoryString : categories) {
            switch (categoryString) {
                case "SERVICE" -> evidence.addCategory(Category.SERVICE);
                case "QUANTITATIVE" -> evidence.addCategory(Category.QUANTITATIVE);
                case "QUALITATIVE" -> evidence.addCategory(Category.QUALITATIVE);
                default -> logger.warn("Evidence service - evidence {} attempted to add category {}", evidence.getId(), categoryString);
            }
        }
    }


    /**
     * Add a list of skills to a given piece of evidence. If the skills name is 'No Skills' it is ignored
     *
     * @param evidence - The  piece of evidence
     * @param skills   - The list of the skills in string form
     */
    public void addSkills(Evidence evidence, List<Skill> skills) throws CheckException {
        for (Skill skillInfo: skills) {
            try {
                regexService.checkInput(RegexPattern.GENERAL_UNICODE, skillInfo.getName(), 1, 30, "Skill name");
            } catch (CheckException e) {
                removeWeblinks(evidence);
                evidenceRepository.delete(evidence);
                throw new CheckException(e.getMessage());
            }
            Skill savedSkill;
            if (skillInfo.getId() == null) {
                if (skillInfo.getName().equalsIgnoreCase("No Skill")) {
                    continue;
                }
                Skill createSkill = new Skill(skillInfo.getName());
                savedSkill = skillRepository.save(createSkill);
            } else {
                Optional<Skill> optionalSkill = skillRepository.findById(skillInfo.getId());
                if (optionalSkill.isPresent()) {
                    savedSkill = optionalSkill.get();
                    if (! Objects.equals(savedSkill.getName(), skillInfo.getName())) {
                        Optional<Skill> optionalSkillWithSameName = skillRepository.findByNameIgnoreCase(skillInfo.getName());
                        savedSkill.setName(skillInfo.getName());
                        skillRepository.save(savedSkill);
                        if (optionalSkillWithSameName.isPresent()) {
                            Skill skillWithSameName = optionalSkillWithSameName.get();
                            Set<Evidence> evidenceList = skillWithSameName.getEvidence();
                            for (Evidence evidenceToChange : evidenceList) {
                                evidenceToChange.removeSkill(skillWithSameName);
                                evidenceToChange.addSkill(savedSkill);
                                evidenceRepository.save(evidenceToChange);
                            }
                            skillRepository.delete(skillWithSameName);
                        }
                    }
                } else {
                    throw new CheckException("Invalid Skill Id");
                }

            }
            evidence.addSkill(savedSkill);
        }
        skillFrequencyService.updateAllSkillFrequenciesForUser(evidence.getUserId());
        evidenceRepository.save(evidence);
    }

    /**
     * Takes a piece of evidence and deletes all the skills which aren't in any other evidence
     *
     * @param evidence the piece of evidence
     */
    public void deleteOrphanSkills(Evidence evidence) {
        for (Skill skill : evidence.getSkills()) {
            if (skill.getEvidence().size() == 1) {
                logger.info("DELETE SKILL {}", skill.getName());
                skillRepository.delete(skill);
                logger.info("DELETED SKILL {}", skill.getName());
            } else {
                skill.removeEvidence(evidence);
                skillRepository.save(skill);

            }
        }
    }


    /**
     * Deletes all the weblinks associated with a piece of evidence. This is needed as we are unable to delete evidence
     * if it has weblinks saved to it
     *
     * @param evidence The evidence with weblinks to delete
     */
    private void removeWeblinks(Evidence evidence) {
        webLinkRepository.deleteAll(evidence.getWebLinks());
    }


    /**
     * Helper method to add a list of weblinks to a piece of evidence
     *
     * @param evidence The evidence to add the weblinks to
     * @param webLinks The list of weblinks to add, in their raw DTO form
     * @throws MalformedURLException if a weblink has an invalid URL
     */
    private void addWeblinks(Evidence evidence, List<WebLinkDTO> webLinks) throws CheckException {
        for (WebLinkDTO webLinkDTO : webLinks) {
            // This requires the evidence object to be saved, since it needs to refer to it
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, webLinkDTO.getName(), 1, WebLink.MAXNAMELENGTH, "Weblink name");
            regexService.checkInput(RegexPattern.WEBLINK, webLinkDTO.getUrl(), 1, WebLink.MAXURLLENGTH, "Weblink url");

            WebLink webLink = new WebLink(evidence, webLinkDTO);
            webLinkRepository.save(webLink);
            evidence.addWebLink(webLink);
            evidenceRepository.save(evidence);
        }
    }


    /**
     * Takes a piece of evidence and updates the associated users for that evidence.
     *
     * If the piece of evidence has new associated users then the evidence is created
     * for that user too.
     */
    private List<Integer> getUnassociatedUsers(Evidence originalEvidence, List<Integer> associatedUsers) {
        List<Integer> usersToGiveEvidence = new ArrayList<>();
        for (Integer userId : associatedUsers) {
            if (! originalEvidence.getArchivedIds().contains(userId)) {
                usersToGiveEvidence.add(userId);
            }
        }
        return usersToGiveEvidence;
    }


    /**
     * Helper method that checks if a user exists.
     * Tries to find the user with the specific ID.
     * If it can't find it, throw an exception.
     *
     * @param associateId the ID of the associate/user you want to find
     */
    private void checkAssociateId(int associateId) {
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(associateId).build();
        UserResponse associate = userAccountsClientService.getUserAccountById(request);
        if (associate.getId() < 1) {
            logger.error("CREATING EVIDENCE: Bad id: {}", associateId);
            throw new CheckException("Could not find associated user with ID: " + associateId);
        }
    }


    /**
     * Checks if the evidence date is within the project dates.
     * Also checks that the date isn't in the future
     * Throws a checkException if it's not valid.
     *
     * @param project      the project to check dates for.
     * @param evidenceDate the date of the evidence
     * @throws CheckException if the date is not valid
     */
    private void checkDate(Project project, LocalDate evidenceDate) throws CheckException {
        if (evidenceDate.isBefore(project.getStartDateAsLocalDateTime().toLocalDate())
                || evidenceDate.isAfter(project.getEndDateAsLocalDateTime().toLocalDate())) {
            throw new CheckException("Date is outside project dates");
        }

        if (evidenceDate.isAfter(LocalDate.now())) {
            throw new CheckException("Date is in the future");
        }
    }
}
