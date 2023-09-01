package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Project project;
    private final AuthenticateClientService authenticateClientService = mock(AuthenticateClientService.class);
    private final UserAccountsClientService userAccountsClientService = mock(UserAccountsClientService.class);
    private final SprintRepository sprintRepository = mock(SprintRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final RegexService regexService = spy(RegexService.class);
    private final Sprint mockSprint = mock(Sprint.class);
    private final DateTimeService dateTimeService = spy(new DateTimeService(sprintRepository));
    private final ProjectService projectService = new ProjectService(sprintRepository);
    @MockBean
    private SkillRepository skillRepository;

    @MockBean
    private EvidenceRepository evidenceRepository;

    @MockBean
    private DataInitialisationManagerPortfolio dataInitialisationManagerPortfolio;

    private Authentication principal = new Authentication(
            AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build()
    );

    @InjectMocks
    private final PortfolioController portfolioController = new PortfolioController(
            sprintRepository,
            projectRepository,
            userAccountsClientService,
            regexService,
            projectService,
            dateTimeService
    );


    @BeforeEach
    public void setup() {
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        project = new Project("Project Seng302",
                LocalDate.parse("2022-02-25"),
                LocalDate.parse("2022-09-30"),
                "SENG302 is all about putting all that you have learnt in" +
                        " other courses into a systematic development process to" +
                        " create software as a team.");
        userBuilder.addRoles(UserRole.TEACHER);
        UserResponse user = userBuilder.build();
        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(userAccountsClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        UserRegisterResponse userRegisterResponse = UserRegisterResponse.newBuilder().setIsSuccess(true).build();
        when(userAccountsClientService.register(any(UserRegisterRequest.class))).thenReturn(userRegisterResponse);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));
    }


    @Test
    void testGetPortfolio() {
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
    }


    @Test
    void testGetPortfolioNoProject() {
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }


    @Test
    void testGetPortfolioThrowsException() {
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testGetPortfolioRolesAreStudent() {
        setUserToStudent();
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
        Assertions.assertEquals(false, modelAndView.getModel().get("userCanEdit"));
    }

    @Test
    void testGetPortfolioRolesAreTeacherOrAbove() {
        ModelAndView modelAndView = portfolioController.getPortfolio(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("portfolio", modelAndView.getViewName());
        Assertions.assertEquals(true, modelAndView.getModel().get("userCanEdit"));
    }

    @Test
    void testGetEditProjectPage() {
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("projectEdit", modelAndView.getViewName());
    }

    @Test
    void testGetEditProjectPageNoProject() {
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testGetEditProjectPageThrowsException() {
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ModelAndView modelAndView = portfolioController.edit(principal, 1L);
        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertEquals("errorPage", modelAndView.getViewName());
    }

    @Test
    void testEditProject() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testEditProjectNoProject() {
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    void testEditProjectThrowsException() {
        when(projectRepository.findById(Mockito.any())).thenReturn(null);
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().isError());
    }

    @Test
    void testEditProjectNameTooLong() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name".repeat(400), LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Project name is longer than the maximum length"));
    }

    @Test
    void testEditProjectNameTooShort() {
        ProjectRequest projectRequest = new ProjectRequest("1", "", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Project name is shorter than the minimum length"));
    }

    @Test
    void testEditProjectDescriptionTooLong() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().plusDays(3).toString(), "New Description".repeat(400));
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Project description is longer than the maximum length"));
    }


    @Test
    void testEditProjectNewStartDateToFarInPast() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().minusYears(2).toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Project cannot start more than a year before its original date", response.getBody());
    }


    @Test
    void testEditProjectNewEndDateIsBeforeSprintsEnd() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().plusDays(1).toString(), LocalDate.now().plusDays(3).toString(), "New Description");
        when(sprintRepository.getAllByProjectOrderByEndDateDesc(Mockito.any())).thenReturn(getSprints());
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("There is a sprint that extends after that date", response.getBody());
    }


    @Test
    void testEditProjectNewStartDateIsAfterSprintsStart() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().plusMonths(4).toString(), LocalDate.now().plusMonths(4).plusDays(4).toString(), "New Description");
        when(sprintRepository.getAllByProjectOrderByStartDateAsc(Mockito.any())).thenReturn(getSprints());
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("There is a sprint that starts before that date", response.getBody());
    }

    @Test
    void testEditProjectNewStartDateBeforeNewEndDate() {
        ProjectRequest projectRequest = new ProjectRequest("1", "New Name", LocalDate.now().toString(), LocalDate.now().minusDays(3).toString(), "New Description");
        ResponseEntity<Object> response = portfolioController.editDetails(projectRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("End date cannot be before start date", response.getBody());
    }

    @Test
    void testAddSprint() {
        ResponseEntity<Object> response = portfolioController.addSprint(project.getId());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testAddSprintNoMoreRoom() {
        project = new Project("Project Seng302",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2),
                "SENG302 is all about putting all that you have learnt in" +
                        " other courses into a systematic development process to" +
                        " create software as a team.");
        ArrayList<Sprint> sprints = new ArrayList<>();
        Sprint sprint = new Sprint(project, "test", LocalDate.now());
        sprints.add(sprint);
        when(projectRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(project));
        when(sprintRepository.findAllByProjectId(Mockito.anyLong())).thenReturn(sprints);
        ResponseEntity<Object> response = portfolioController.addSprint(project.getId());
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("No more room to add sprints within project dates!", response.getBody());
    }


    @Test
    void testEditSprint() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "testing", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testEditSprintBadSprintId() {
        Project project = new Project("Test Project");
        SprintRequest sprintRequest = new SprintRequest("1", "testing", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.empty());
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Sprint id doesn't correspond to existing sprint", response.getBody());
    }

    @Test
    void testEditSprintBadName() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Sprint name is shorter than the minimum length"));
    }

    @Test
    void testEditSprintBadNameLong() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "test".repeat(400), LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Sprint name is longer than the maximum length"));
    }

    @Test
    void testEditSprintBadNameSpaces() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "       ", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Sprint name is shorter than the minimum length"));
    }


    @Test
    void testEditSprintDescriptionBadNameLong() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "test", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing".repeat(400), "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Sprint description is longer than the maximum length"));
    }


    @Test
    void testEditSprintBadColourLong() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "test", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "123123123123");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Sprint colour is longer than the maximum length"));
    }


    @Test
    void testEditSprintBadColour() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "test", LocalDate.now().toString(), LocalDate.now().plusDays(4).toString(), "testing", "#iii");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Sprint colour must be a valid hex colour.", response.getBody());
    }


    @Test
    void testEditSprintStartDatesWrong() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "testing", LocalDate.now().minusDays(5).toString(), LocalDate.now().plusDays(4).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Start date is before previous sprints end date / project start date", response.getBody());
    }


    @Test
    void testEditSprintEndDatesWrong() {
        Project project = new Project("Test Project");
        Sprint sprint = new Sprint(project, "Testing", LocalDate.now());
        SprintRequest sprintRequest = new SprintRequest("1", "testing", LocalDate.now().toString(), LocalDate.now().plusDays(700).toString(), "testing", "#fff");
        Mockito.when(sprintRepository.findById("1")).thenReturn(Optional.of(sprint));
        Mockito.when(mockSprint.getProject()).thenReturn(project);
        ResponseEntity<Object> response = portfolioController.updateSprint(sprintRequest);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("End date is after next sprints start date / project end date", response.getBody());
    }


    // -------------- Helper context functions ----------------------------------------------------

    void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userBuilder.build());
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private ArrayList<Sprint> getSprints() {
        Sprint sprint = new Sprint(project, "test", LocalDate.now().plusWeeks(1));
        Sprint sprint2 = new Sprint(project, "test2", LocalDate.now().plusWeeks(1).plusMonths(1));
        ArrayList<Sprint> arrayList = new ArrayList<>();
        arrayList.add(sprint);
        arrayList.add(sprint2);
        return arrayList;
    }

}
