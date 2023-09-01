package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EvidenceServiceTest {

    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);
    private final ProjectRepository projectRepository = Mockito.mock(ProjectRepository.class);
    private final EvidenceRepository evidenceRepository = Mockito.mock(EvidenceRepository.class);
    private final WebLinkRepository webLinkRepository = Mockito.mock(WebLinkRepository.class);
    private final SkillRepository skillRepository = Mockito.mock(SkillRepository.class);
    private final RegexService regexService = Mockito.spy(RegexService.class);
    private final SkillFrequencyService skillFrequencyService = new SkillFrequencyService(evidenceRepository, skillRepository);
    private Authentication principal;
    private Evidence evidence;
    private EvidenceService evidenceService;
    private EvidenceDTO evidenceDTO;

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(userAccountsClientService,
                projectRepository,
                evidenceRepository,
                webLinkRepository,
                skillRepository,
                regexService,
                skillFrequencyService);
        // TODO maybe change 2 in second param to 1
        evidence = new Evidence(1, 2, "Title", LocalDate.now(), "description");
        when(userAccountsClientService.getUserAccountById(any())).thenReturn(UserResponse.newBuilder().setId(1).build());
        when(evidenceRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(skillRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        evidenceService = Mockito.spy(evidenceService);
        Skill skill1 = new Skill(1,"Testing");
        Skill skill2 = new Skill(2, "Backend");
        evidenceDTO = new EvidenceDTO.EvidenceDTOBuilder()
                .setId(10)
                .setTitle("New Title")
                .setDate(LocalDate.now().toString())
                .setDescription("New description")
                .setWebLinks(new ArrayList<>(
                        Arrays.asList(
                                new WebLinkDTO("New weblink 1", "http://www.google.com"),
                                new WebLinkDTO("New weblink 2", "https://localhost:9000/test")
                        )))
                .setCategories(new ArrayList<>(
                        Arrays.asList("SERVICE", "QUANTITATIVE"
                        )))
                .setSkills(new ArrayList<>(
                        Arrays.asList(skill1, skill2)
                ))
                .setAssociateIds(new ArrayList<>(
                        Arrays.asList(2, 3, 4, 5)
                ))
                .setProjectId(1L)
                .build();
        evidence.addSkill(skill1);
        evidence.addSkill(skill2);
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndId(1, 1)).thenReturn(Optional.of(skill1));
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndId(1, 2)).thenReturn(Optional.of(skill2));
        Mockito.when(skillRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(skill1));
        Mockito.when(skillRepository.findById(Integer.valueOf(2))).thenReturn(Optional.of(skill2));

        Project project = new Project("Testing");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    }


    @Test
    void addEvidence() throws MalformedURLException {
        setUserToStudent();

        String expectedTitle = evidenceDTO.getTitle();

        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(expectedTitle, evidence.getTitle());
    }


    @Test
    void addEvidenceWithWeblinks() throws MalformedURLException {
        setUserToStudent();

        List<WebLinkDTO> links = new ArrayList<>();
        String url = "https://www.google.com";
        links.add(new WebLinkDTO("name", url));

        evidenceDTO.setWebLinks(links);

        evidenceService.addEvidence(principal,
                evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(url, evidence.getWebLinks().iterator().next().getUrl());
    }


    @Test
    void testBadProjectId() {
        setUserToStudent();

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("project id"));
    }


    @Test
    void testBadDateFormat() {
        setUserToStudent();

        String date = "WOW this shouldn't work";
        evidenceDTO.setDate(date);

        Assertions.assertThrows(
                DateTimeParseException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
    }


    @Test
    void testDateInFuture() {
        setUserToStudent();

        String date = LocalDate.now().plusDays(1).toString();
        evidenceDTO.setDate(date);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("future"));
    }


    @Test
    void testDateOutsideProject() {
        setUserToStudent();

        String date = LocalDate.now().minusDays(1).toString();
        evidenceDTO.setDate(date);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("outside project dates"));
    }


    @Test
    void testShortTitle() {
        setUserToStudent();

        evidenceDTO.setTitle("");

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("title is shorter than the minimum length of 5 characters"));
    }


    @Test
    void testLongTitle() {
        setUserToStudent();

        String title = "This string is exactly 31 chars".repeat(5);
        evidenceDTO.setTitle(title);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("title is longer than the maximum length of 50 characters"));
    }


    @Test
    void testShortDescription() {
        setUserToStudent();

        String description = "";
        evidenceDTO.setDescription(description);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("description is shorter than the minimum length of 5 characters"));
    }


    @Test
    void testLongDescription() {
        setUserToStudent();

        String description = "This string is exactly 31 chars".repeat(20);
        evidenceDTO.setDescription(description);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addEvidence(principal, evidenceDTO)
        );
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("description is longer than the maximum length of 500 characters"));
    }


    @Test
    void addEvidenceWithNoCategories() throws MalformedURLException {
        setUserToStudent();

        evidenceDTO.setCategories(new ArrayList<>());

        evidenceService.addEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }


    @Test
    void addEvidenceWithOneCategory() throws MalformedURLException {
        setUserToStudent();

        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");

        evidenceDTO.setCategories(categories);

        evidenceService.addEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(1, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
    }


    @Test
    void addEvidenceWithAllCategories() throws MalformedURLException {
        setUserToStudent();

        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");
        categories.add("QUANTITATIVE");
        categories.add("QUALITATIVE");

        evidenceDTO.setCategories(categories);

        evidenceService.addEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(3, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUANTITATIVE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
    }


    @Test
    void addEvidenceCategoriesCantBeAddedTwice() throws MalformedURLException {
        setUserToStudent();

        List<String> categories = new ArrayList<>();
        categories.add("SERVICE");
        categories.add("QUALITATIVE");
        categories.add("QUALITATIVE");

        evidenceDTO.setCategories(categories);

        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(2, evidence.getCategories().size());
        Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
        Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
    }


    @Test
    void addEvidenceCategoriesDoesNothingWithNotExistingCategories() throws MalformedURLException {
        setUserToStudent();

        List<String> categories = new ArrayList<>();
        categories.add("NOT");

        evidenceDTO.setCategories(categories);
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(0, evidence.getCategories().size());
    }


    @Test
    void addEvidenceWithAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        List<Integer> associates = new ArrayList<>(List.of(1, 12, 13, 14));

        evidenceDTO.setAssociateIds(associates);

        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called two times per user id
        Mockito.verify(evidenceRepository, atLeast(associates.size())).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(4, evidence.getAssociateIds().size());
        Assertions.assertEquals(associates, evidence.getAssociateIds());
        Assertions.assertTrue(associates.contains(evidence.getUserId()));
    }

    @Test
    void addEvidenceWithNoAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        evidenceDTO.setAssociateIds(new ArrayList<>());
        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called three times per user id
        Mockito.verify(evidenceRepository, atLeast(2)).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(1, evidence.getAssociateIds().size());
        Assertions.assertEquals(List.of(1), evidence.getAssociateIds());
        Assertions.assertTrue(evidence.getAssociateIds().contains(evidence.getUserId()));
    }

    @Test
    void addEvidenceWithDuplicateAssociatedUsers() throws MalformedURLException {
        setUserToStudent();

        List<Integer> associates = new ArrayList<>(List.of(1, 12, 13, 14, 12));
        List<Integer> expectedAssociates = new ArrayList<>(List.of(1, 12, 13, 14));
        evidenceDTO.setAssociateIds(associates);

        evidenceService.addEvidence(principal, evidenceDTO);
        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        // Verify that it saved more than usual - currently evidenceRepository.save is called three times per user id
        Mockito.verify(evidenceRepository, atLeast(associates.size())).save(captor.capture());

        Evidence evidence = captor.getValue();
        Assertions.assertEquals(4, evidence.getAssociateIds().size()); // The creator is considered an associate, so expected size is 1
        Assertions.assertEquals(expectedAssociates, evidence.getAssociateIds());
        Assertions.assertTrue(expectedAssociates.contains(evidence.getUserId()));
    }


    @Test
    void addEvidenceWithAssociatedUsersInvalidAssociateId() {
        setUserToStudent();

        List<Integer> associates = new ArrayList<>(List.of(12, 13, -14));
        evidenceDTO.setAssociateIds(associates);
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(-14).build();
        when(userAccountsClientService.getUserAccountById(request)).thenReturn(UserResponse.newBuilder().setId(-1).build());


        Assertions.assertThrows(CheckException.class, () -> evidenceService.addEvidence(principal, evidenceDTO));
    }




    // ----------------------------- Add Skill Tests --------------------------


    @Test
    void testAddSkillToEvidenceWhenNoSkill() {
        List<Skill> listSkills = new ArrayList<>();
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.never()).findByNameIgnoreCase(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExist() {
        Skill usersSkill1 = new Skill(1, "Skill_1");
        Mockito.when(skillRepository.findById(Integer.valueOf(anyInt()))).thenReturn(Optional.of(usersSkill1));
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(1,"Skill_1"));

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findById(Integer.valueOf(anyInt()));
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillExistInDiffCase() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(usersSkill1));
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(1,"sKILL 1"));
        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findById(Integer.valueOf((anyInt())));
        Mockito.verify(skillRepository, times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsExist() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(usersSkill1));
        Skill usersSkill2 = new Skill(2, "Skill 2");
        Mockito.when(skillRepository.findById(Integer.valueOf(2))).thenReturn(Optional.of(usersSkill2));

        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(1,"Skill 1"));
        listSkills.add(new Skill(2,"Skill 2"));

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(2)).findById(Integer.valueOf(anyInt()));
        Mockito.verify(evidenceRepository, times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillToEvidenceWhenSkillNotExist() {

        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(null,"Skill 1"));
        evidenceService.addSkills(evidence, listSkills);

        Mockito.verify(skillRepository, Mockito.never()).findById(Integer.valueOf(anyInt()));
        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSkillsNotExist() {
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(null,"Skill 1"));
        listSkills.add(new Skill(null,"Skill 2"));
        evidenceService.addSkills(evidence, listSkills);

        Mockito.verify(skillRepository, Mockito.times(2)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddMultipleSkillsToEvidenceWhenSomeSkillsExistSomeNot() {
        Skill usersSkill1 = new Skill(1, "Skill 1");
        Mockito.when(skillRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(usersSkill1));

        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(1,"Skill 1"));
        listSkills.add(new Skill(null,"Skill 2"));

        evidenceService.addSkills(evidence, listSkills);
        Mockito.verify(skillRepository, Mockito.times(1)).findById(Integer.valueOf(anyInt()));
        Mockito.verify(skillRepository, Mockito.atLeast(1)).save(Mockito.any());
        Mockito.verify(evidenceRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void testAddSkillNameTooShort() {
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(3,""));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("is shorter than the minimum length of"));
    }

    @Test
    void testAddSkillNameTooLong() {
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(3,"A Decently Long Skill Name, Which as of the time of writing " +
                "should exceed the limit of thirty characters"));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("is longer than the maximum length of"));
    }

    @Test
    void testAddSkillNameContainsIllegalSymbol() {
        List<Skill> listSkills = new ArrayList<>();
        listSkills.add(new Skill(3,"Dangerous Skill: ☢"));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.addSkills(evidence, listSkills)
        );
        Assertions.assertTrue(exception.getMessage().contains("Skill name can only contain unicode letters, numbers, " +
                "punctuation, symbols (but not emojis) and whitespace"));
    }


    @Test
    void testSkillSavesUniquelyToUser() {
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), eq("SKILL"))).thenReturn(Optional.empty());

        List<Skill> newSkill = new ArrayList<>();
        newSkill.add(new Skill(null,"SKILL"));
        evidenceService.addSkills(evidence, newSkill);

        Mockito.verify(skillRepository, Mockito.times(1)).save(Mockito.any());
    }


    @Test
    void testEditServiceUpdatesWhenAllFieldsAreValid() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();
        Integer originalEvidenceUserId = evidence.getUserId();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        evidence = captor.getValue();

        assertEvidenceDtoMatchesEvidence(originalEvidenceUserId);
    }


    @Test
    void testEditEvidenceWhenUserDoesntOwnTheEvidence() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();
        evidence.setUserId(2);

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );
        Mockito.verify(evidenceRepository, never()).save(any());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("owned by a different user"));
    }


    @Test
    void testEditEvidenceWhenEvidenceDoesntExist() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        Mockito.when(evidenceRepository.findById(evidenceDTO.getId())).thenReturn(Optional.empty());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );
        Mockito.verify(evidenceRepository, never()).save(any());
        Assertions.assertTrue(exception.getMessage().toLowerCase().contains("no evidence found"));
    }


    @Test
    void testRequiredValidationIsCalledOnEvidenceEdit() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();
        UserResponse user = UserResponse.newBuilder().setId(1).build();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        Mockito.verify(regexService, times(1)).checkInput(eq(RegexPattern.GENERAL_UNICODE), any(), anyInt(), anyInt(), eq("Title"));
        Mockito.verify(regexService, times(1)).checkInput(eq(RegexPattern.GENERAL_UNICODE), any(), anyInt(), anyInt(), eq("Description"));
        Mockito.verify(evidenceService, times(1)).checkValidEvidenceDTO(user, evidenceDTO);
        evidence = captor.getValue();
    }


    @Test
    void testOnlyTheRightUsersGetNewEvidenceOnEdit() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        evidenceService.editEvidence(principal, evidenceDTO);

        ArgumentCaptor<Evidence> captor = ArgumentCaptor.forClass(Evidence.class);
        Mockito.verify(evidenceRepository, atLeast(1)).save(captor.capture());

        List<Evidence> capturedEvidence = captor.getAllValues();
        Set<Integer> usersWithSavedEvidence = capturedEvidence.stream().map(Evidence::getUserId).collect(Collectors.toSet());
        Set<Integer> expectedUsersToHaveSave = new HashSet<>(Arrays.asList(1, 4, 5));

        for (Integer user : usersWithSavedEvidence) {
            Assertions.assertTrue(expectedUsersToHaveSave.contains(user));
        }
        Assertions.assertEquals(expectedUsersToHaveSave.size(), usersWithSavedEvidence.size());
    }


    @Test
    void testCannotEditAnotherUsersSkill() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        List<Skill> otherUsersSkill = new ArrayList<>(List.of(new Skill(5, "Python")));
        evidenceDTO.setSkills(otherUsersSkill);
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndId(1, 5)).thenReturn(Optional.empty());

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );

        Assertions.assertEquals("Could not retrieve one or more skills", exception.getMessage());
    }


    @Test
    void testEditSkillInvalidId() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        List<Skill> otherUsersSkill = new ArrayList<>(List.of(new Skill(5, "Python")));
        evidenceDTO.setSkills(otherUsersSkill);
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndId(1, 5)).thenReturn(Optional.of(otherUsersSkill.get(0)));

        CheckException exception = Assertions.assertThrows(
                CheckException.class,
                () -> evidenceService.editEvidence(principal, evidenceDTO)
        );

        Assertions.assertEquals("Invalid Skill Id", exception.getMessage());
    }


    @Test
    void testEditEvidenceRemoveSkills() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        evidenceDTO.setSkills(new ArrayList<>());

        Evidence changedEvidence = evidenceService.editEvidence(principal, evidenceDTO);

        Assertions.assertEquals(0, changedEvidence.getSkills().size());
    }


    @Test
    void testEditEvidenceChangeSkillName() throws Exception {
        setUserToStudent();
        setupEditEvidenceTests();

        evidenceDTO.setSkills(new ArrayList<>(List.of(new Skill(1, "python"))));

        Evidence changedEvidence = evidenceService.editEvidence(principal, evidenceDTO);

        List<Skill> retrievedSkills = changedEvidence.getSkills().stream().toList();

        Assertions.assertEquals(1, retrievedSkills.size());
        Assertions.assertEquals("python", retrievedSkills.get(0).getName());
    }


    // ---------------------------------------------------


    private void setupEditEvidenceTests() throws Exception {
        evidence = new Evidence(10,
                1,
                "Test Original title",
                LocalDate.now().minusDays(1) ,
                "Test Original Description");
        //TODO CHECK PLEASE
        WebLinkDTO webLinkDTO = new WebLinkDTO( "Original Link", "https://localhost:8080");
        evidence.addWebLink(new WebLink(evidence, webLinkDTO));
        evidence.addSkill(new Skill("Java"));
        evidence.addCategory(Category.QUALITATIVE);
        evidence.addCategory(Category.QUANTITATIVE);
        evidence.addAssociateId(2);
        // Adds the archived ID
        evidence.addAssociateId(3);
        evidence.removeAssociateId(3);

        Project project = new Project("Project title");
        Mockito.when(skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(anyInt(), any())).thenReturn(Optional.empty());
        Mockito.when(evidenceRepository.findById(evidenceDTO.getId())).thenReturn(Optional.of(evidence));
        Mockito.when(projectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));
    }


    private void assertEvidenceDtoMatchesEvidence(Integer userId) {
        Assertions.assertEquals(evidence.getId(), evidenceDTO.getId());
        Assertions.assertEquals(evidence.getUserId(), userId);
        Assertions.assertEquals(evidence.getTitle(), evidenceDTO.getTitle());
        Assertions.assertEquals(evidence.getDate(), LocalDate.parse(evidenceDTO.getDate()));
        Assertions.assertEquals(evidence.getDescription(), evidenceDTO.getDescription());
        Assertions.assertEquals(evidence.getWebLinks().size(), evidenceDTO.getWebLinks().size());
        for (WebLinkDTO webLinkDTO : evidenceDTO.getWebLinks()) {
            Assertions.assertTrue(evidence.getWebLinks().stream().anyMatch(link -> link.getAlias().equals(webLinkDTO.getName())));
        }
        Assertions.assertEquals(evidence.getSkills().size(), evidenceDTO.getSkills().size());
        for (Skill skillInfo : evidenceDTO.getSkills()) {
            Assertions.assertTrue(evidence.getSkills().stream().anyMatch(skill -> skill.getName().equals(skillInfo.getName())));
        }
        Assertions.assertEquals(evidence.getCategories().size(), evidenceDTO.getCategories().size());
        for (String categoryString : evidenceDTO.getCategories()) {
            switch (categoryString) {
                case "QUANTITATIVE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.QUANTITATIVE));
                case "QUALITATIVE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.QUALITATIVE));
                case "SERVICE" -> Assertions.assertTrue(evidence.getCategories().contains(Category.SERVICE));
            }
        }
        Assertions.assertEquals(evidence.getAssociateIds().size(), evidenceDTO.getAssociateIds().size());
        for (Integer associateId : evidenceDTO.getAssociateIds()) {
            Assertions.assertTrue(evidence.getAssociateIds().contains(associateId));
        }
    }


    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());
    }
}