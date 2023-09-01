package nz.ac.canterbury.seng302.portfolio.controller;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceResponseDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserDTO;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.SkillFrequencyService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersFilteredRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.util.BasicStringFilteringOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for all the Evidence based end points
 */
@Controller
public class EvidenceController {

    /** For logging the requests related to groups */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** For requesting user information form the IdP.*/
    private final UserAccountsClientService userAccountsClientService;

    /** The repository containing users pieces of evidence. */
    private final EvidenceRepository evidenceRepository;

    /** The repository containing the projects. */
    private final ProjectRepository projectRepository;

    /** Provides helper functions for Crud operations on evidence */
    private final EvidenceService evidenceService;

    /** Provides helper functions for skill frequency operations */
    private final SkillFrequencyService skillFrequencyService;

    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An unknown error occurred. Please try again";


    /**
     * Autowired constructor for injecting the required beans.
     *  @param userAccountsClientService For requesting user information form the IdP
     * @param projectRepository The repository containing the projects.
     * @param evidenceRepository The repository containing users pieces of evidence.
     * @param evidenceService Provides helper functions for Crud operations on evidence.
     * @param skillFrequencyService Provides helper functions for skill frequency operations
     */
    @Autowired
    public EvidenceController(UserAccountsClientService userAccountsClientService,
                              ProjectRepository projectRepository,
                              EvidenceRepository evidenceRepository,
                              EvidenceService evidenceService,
                              SkillFrequencyService skillFrequencyService) {
        this.userAccountsClientService = userAccountsClientService;
        this.projectRepository = projectRepository;
        this.evidenceRepository = evidenceRepository;
        this.evidenceService = evidenceService;
        this.skillFrequencyService = skillFrequencyService;
    }


    /**
     * Gets the evidence page for the logged-in user.
     *
     * @param principal The principal containing the logged-in user's Id.
     * @return A modelAndView object of the page.
     */
    @GetMapping("/evidence")
    public ModelAndView getEvidenceBySkillsPage(@AuthenticationPrincipal Authentication principal) {
        logger.info("GET REQUEST /evidence/skills - attempt to get page");

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

        ModelAndView modelAndView = new ModelAndView("evidenceBySkills");
        modelAndView.addObject("user", user);

        Project project = projectRepository.getProjectById(1L);
        LocalDate projectEndDate = project.getEndDate();
        LocalDate projectStartDate = project.getStartDate();
        LocalDate currentDate = LocalDate.now();
        LocalDate evidenceMaxDate = LocalDate.now();

        modelAndView.addObject("currentDate", currentDate.format(DateTimeService.yearMonthDay()));
        modelAndView.addObject("projectStartDate", projectStartDate.format(DateTimeService.yearMonthDay()));
        modelAndView.addObject("webLinkMaxUrlLength", WebLink.MAXURLLENGTH);
        modelAndView.addObject("webLinkMaxNameLength", WebLink.MAXNAMELENGTH);
        modelAndView.addObject(project);
        modelAndView.addObject("webLinkRegex", RegexPattern.WEBLINK);
        modelAndView.addObject("generalUnicodeRegex", RegexPattern.GENERAL_UNICODE);
        modelAndView.addObject("skillRegex", RegexPattern.SKILL);

        if (projectEndDate.isBefore(currentDate)) {
            evidenceMaxDate = projectEndDate;
        }
        modelAndView.addObject("evidenceMaxDate", evidenceMaxDate.format(DateTimeService.yearMonthDay()));

        return modelAndView;
    }


    /**
     * Gets the details for a piece of evidence with the given id
     *
     * Response codes: NOT_FOUND means the piece of evidence does not exist
     *                 OK means the evidence exists and an evidence details are returned.
     *                 BAD_REQUEST when the user doesn't interact with the endpoint correctly, i.e., no or invalid evidenceId
     *
     * @param evidenceId - The ID of the piece of evidence
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidencePiece")
    public ResponseEntity<Object> getOneEvidence(@RequestParam("evidenceId") Integer evidenceId) {
        logger.info("GET REQUEST /evidence - attempt to get evidence with Id {}", evidenceId);
        try {
            Optional<Evidence> evidence = evidenceRepository.findById(evidenceId);

            if (evidence.isEmpty()) {
                logger.info("GET REQUEST /evidence - evidence {} does not exist", evidenceId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            EvidenceResponseDTO response = new EvidenceResponseDTO(evidence.get(), getUsers(evidence.get().getAssociateIds()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Gets all the pieces of evidence for a requested user.
     *
     * Response codes: NOT_FOUND means the user does not exist
     * OK means the user exists and an evidence list is returned  (an empty list if no evidence exists)
     * BAD_REQUEST when the user doesn't interact with the endpoint correctly, i.e., no or invalid userId
     *
     * @param userId - The userId of the user whose evidence is wanted
     * @return A response entity with the required response code. Response body is the evidence is the status is OK
     */
    @GetMapping("/evidenceData")
    public ResponseEntity<Object> getAllEvidence(@RequestParam("userId") Integer userId) {
        logger.info("GET REQUEST /evidence - attempt to get evidence for user {}", userId);
        try {

            GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(userId).build();
            UserResponse userResponse = userAccountsClientService.getUserAccountById(request);
            if (userResponse.getId() == -1) {
                logger.info("GET REQUEST /evidence - user {} does not exist", userId);
                return new ResponseEntity<>("Error: User not found", HttpStatus.NOT_FOUND);
            }
            List<Evidence> evidences = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(userId);
            List<EvidenceResponseDTO> response = new ArrayList<>();
            for (Evidence evidence : evidences) {
                EvidenceResponseDTO dto = new EvidenceResponseDTO(evidence, getUsers(evidence.getAssociateIds()));
                response.add(dto);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Users-Name", userResponse.getFirstName() + ' ' + userResponse.getLastName());

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(response);
        } catch (Exception exception) {
            logger.warn(exception.getClass().getName());
            logger.warn(exception.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Entrypoint for creating an evidence object.
     *
     * @param principal   The authentication principal for the logged-in user
     * @param evidenceDTO The EvidenceDTO object containing the required data for the evidence instance being created.
     * @return returns a ResponseEntity. This entity includes the new piece of evidence if successful.
     */
    @PostMapping(value = "/evidence")
    @ResponseBody
    public ResponseEntity<Object> addEvidence(
            @AuthenticationPrincipal Authentication principal,
            @RequestBody EvidenceDTO evidenceDTO
    ) {
        logger.info("POST REQUEST /evidence - attempt to create new evidence");

        try {
            Evidence evidence = evidenceService.addEvidence(principal, evidenceDTO);
            return new ResponseEntity<>(evidence, HttpStatus.OK);
        } catch (CheckException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad input: {}", err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DateTimeParseException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad date");
            return new ResponseEntity<>("Date is not in a parsable format", HttpStatus.BAD_REQUEST);
        } catch (MalformedURLException err) {
            logger.warn("POST REQUEST /evidence - attempt to create new evidence: Bad url {}", err.getMessage());
            return new ResponseEntity<>("Submitted web link URL is malformed", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /evidence - attempt to create new evidence: ERROR: {}", err.getMessage());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Entrypoint for editing an evidence object
     *
     * @param principal The authentication principal for the logged-in user
     * @param evidenceDTO The EvidenceDTO object containing the required data for the evidence instance being created.
     *
     * @return returns a ResponseEntity. This entity includes the edited piece of evidence if successful.
     */
    @PatchMapping("/evidence")
    @ResponseBody
    public ResponseEntity<Object> editEvidence(
            @AuthenticationPrincipal Authentication principal,
            @RequestBody EvidenceDTO evidenceDTO
    ) {
        logger.info("PATCH REQUEST /evidence - attempt to edit evidence");

        try {
            return new ResponseEntity<>(evidenceService.editEvidence(principal, evidenceDTO), HttpStatus.OK);
        } catch (CheckException err) {
            logger.warn("PATCH REQUEST /evidence - attempt to edit evidence with id {}: Bad input: {}",
                    evidenceDTO.getId(), err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DateTimeParseException err) {
            logger.warn("PATCH REQUEST /evidence - attempt to edit evidence with id {}: Bad date", evidenceDTO.getId());
            return new ResponseEntity<>("Date is not in a parsable format", HttpStatus.BAD_REQUEST);
        } catch (MalformedURLException err) {
            logger.warn("PATCH REQUEST /evidence - attempt to edit evidence with id {}: Bad url {}",
                    evidenceDTO.getId(), err.getMessage());
            return new ResponseEntity<>("Submitted web link URL is malformed", HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("PATCH REQUEST /evidence - attempt to edit evidence with id {}: ERROR: {}",
                    evidenceDTO.getId(), err.getMessage());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Deletes a piece of evidence owned by the user making the request.
     *
     * If the evidence is not owned by the user making the request, then the response is a 401,
     * If the evidence doesn't exist, then the response is a 404,
     * Any other issues return a 500 error,
     * Otherwise the response is OK,
     *
     * @param principal The user who made the request.
     * @param evidenceId The Id of the piece of evidence to be deleted.
     * @return ResponseEntity containing the HTTP status and a response message.
     */
    @DeleteMapping("/evidence")
    public ResponseEntity<Object> deleteEvidence(@AuthenticationPrincipal Authentication principal,
                                                 @RequestParam Integer evidenceId) {
        String methodLoggingTemplate = "DELETE /evidence: {}";
        logger.info(methodLoggingTemplate, "Called");
        try {
            Optional<Evidence> optionalEvidence = evidenceRepository.findById(evidenceId);
            if (optionalEvidence.isEmpty()) {
                String message = "No evidence found with id " + evidenceId;
                logger.info(methodLoggingTemplate, message);
                return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
            }
            Evidence evidence = optionalEvidence.get();
            int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
            if (evidence.getUserId() != userId) {
                logger.warn(methodLoggingTemplate, "User attempted to delete evidence they don't own.");
                return new ResponseEntity<>("You can only delete evidence that you own.", HttpStatus.UNAUTHORIZED);
            }
            evidenceRepository.delete(evidence);
            evidenceService.deleteOrphanSkills(evidence);

            skillFrequencyService.updateAllSkillFrequenciesForUser(userId);
            String message = "Successfully deleted evidence " + evidenceId;
            logger.info(methodLoggingTemplate, message);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>("An unexpected error has occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * A get request for retrieving the users that match the string being typed
     *
     * @param name The name that is being typed by the user
     * @return A ResponseEntity. This entity includes the filtered users if successful, otherwise an error message
     */
    @GetMapping("/filteredUsers")
    public ResponseEntity<Object> getFilteredUsers(@RequestParam("name") String name){
        try {
            logger.info("GET REQUEST /filteredUsers - retrieving filtered users with string {}", name);
            PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                    .setOffset(0)
                    .setLimit(999999999) // we want to retrieve all users
                    .setOrderBy("name")
                    .setIsAscendingOrder(true)
                    .build();
            BasicStringFilteringOptions filter = BasicStringFilteringOptions.newBuilder()
                    .setFilterText(name)
                    .build();
            GetPaginatedUsersFilteredRequest request = GetPaginatedUsersFilteredRequest.newBuilder()
                    .setPaginationRequestOptions(options)
                    .setFilteringOptions(filter)
                    .build();
            PaginatedUsersResponse response = userAccountsClientService.getPaginatedUsersFilteredByName(request);
            ArrayList<UserDTO> users = new ArrayList<>();
            for (UserResponse user : response.getUsersList()){
                users.add(new UserDTO(user));
            }
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e){
            logger.warn(e.getClass().getName());
            logger.warn(e.getMessage());
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Helper method that returns a list of all users in a given list.
     *
     * @param userIds The ids of the users
     * @return A list of Users, populated with their details.
     * If any users in userIds list do not exist, they will not be added.
     */
    private List<UserDTO> getUsers(List<Integer> userIds) {
        List<UserDTO> associates = new ArrayList<>();
        for (Integer associate : userIds) {
            GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(associate).build();
            UserResponse user = userAccountsClientService.getUserAccountById(request);
            if (user.getId() < 0) continue; // If their id is negative the user doesn't exist

            UserDTO userDTO = new UserDTO(user);
            associates.add(userDTO);
        }
        return associates;
    }


    /**
     * Handles exceptions when a request body cannot be parsed to the required DTO.
     *
     * @param exception - The exception thrown by the endpoint
     * @return a response entity with a generic message, and a bad request status
     */
    @ExceptionHandler(InvalidDefinitionException.class)
    public ResponseEntity<Object> handleError(InvalidDefinitionException exception) {
        logger.warn("Evidence endpoint InvalidDefinitionError resolved {}", exception.getMessage());
        return new ResponseEntity<>("One or more fields are invalid", HttpStatus.BAD_REQUEST);
    }
}
