package cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.DeadlineController;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.ResponseEntity;

import javax.naming.InvalidNameException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OccasionFeature {


    private final DeadlineRepository deadlineRepository = new DeadlineRepository() {
        @Override
        public List<Deadline> findAllByProjectId(Long projectId) {
            return null;
        }

        @Override
        public Long countDeadlineByProjectId(Long projectId) {
            return (long) deadlines.size();
        }

        @Override
        public List<Deadline> findAllByProjectIdOrderByEndDate(Long projectId) {
            return null;
        }

        @Override
        public <S extends Deadline> S save(S entity) {
            deadlines.add(entity);
            return entity;
        }

        @Override
        public <S extends Deadline> Iterable<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public Optional<Deadline> findById(String s) {
            return Optional.empty();
        }

        @Override
        public Deadline getById(String s) {
            return null;
        }

        @Override
        public boolean existsById(String s) {
            return false;
        }

        @Override
        public Iterable<Deadline> findAll() {
            return null;
        }

        @Override
        public Iterable<Deadline> findAllById(Iterable<String> strings) {
            return null;
        }


        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(String s) {

        }

        @Override
        public void delete(Deadline entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends String> strings) {

        }

        @Override
        public void deleteAll(Iterable<? extends Deadline> entities) {

        }

        @Override
        public void deleteAll() {

        }
    };

    private static final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private static final PrincipalAttributes mockPrincipal = mock(PrincipalAttributes.class);
    private static final UserAccountsClientService clientService = mock(UserAccountsClientService.class);
    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();

    private final ArrayList<Deadline> deadlines = new ArrayList<>();
    private Project project;
    private final DeadlineController deadlineController = new DeadlineController(mockProjectRepository, deadlineRepository, new RegexService());
    private Milestone milestone;

    @Given("the user is authenticated: {string}")
    public void the_user_is_authenticated(String isAuthenticatedString) {
        boolean isAuthenticated = Boolean.parseBoolean(isAuthenticatedString);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);
        if (isAuthenticated) {
            user.addRoles(UserRole.TEACHER);
        }
        when(PrincipalAttributes.getUserFromPrincipal(principal, clientService)).thenReturn(user.build());
    }

    @Given("a project exists from {string} to {string}")
    public void a_project_exists_from_start_date_to_end_date(String startDate, String endDate) {
        project = new Project("default", LocalDate.parse(startDate), LocalDate.parse(endDate), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
        Assertions.assertNotNull(project);
    }

    @When("the user creates a deadline for {string} with name {string}")
    public void a_user_creates_a_deadline_for_deadline_date_with_name_deadline_name(String deadlineDate, String deadlineName) {
        String dateTime = null;
        if (!deadlineDate.equals("left blank")) {
            LocalDateTime parsedDate = LocalDateTime.parse(deadlineDate);
            dateTime = parsedDate.toString();
        }
        if (deadlineName.equals("left blank")) {
            deadlineName = null;
        }
        ResponseEntity<Object> stat = deadlineController.addDeadline(project.getId(), deadlineName, dateTime, 1);
    }

    @When("a user creates a milestone for {string} with name {string} and type {int}")
    public void a_user_creates_a_milestone_for_milestone_date_with_name_milestone_name(String milestoneDate, String milestoneName, int type) {
        if (milestoneDate.equals("left blank")) {
            milestoneDate = null;
        } else if (milestoneName.equals("left blank")) {
            milestoneName = null;
        }
        try {
            LocalDate parsedDate = LocalDate.parse(milestoneDate);
            milestone = new Milestone(project, milestoneName, parsedDate, type);
        } catch (DateTimeException | NullPointerException e) {
            milestone = null;
        }
    }

    @Then("The deadline exists: {string}")
    public void the_deadline_exists(String deadlineExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(deadlineExistsString);
        assertEquals(deadlineExists, deadlineRepository.countDeadlineByProjectId(project.getId()) == 1);
    }

    @Then("The milestone exists: {string}")
    public void the_milestone_exists(String milestoneExistsString) {
        boolean deadlineExists = Boolean.parseBoolean(milestoneExistsString);
        assertEquals(deadlineExists, milestone != null);
    }
}
