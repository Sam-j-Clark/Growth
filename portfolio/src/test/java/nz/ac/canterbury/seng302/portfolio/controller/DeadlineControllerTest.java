package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeadlineControllerTest {

    private final DeadlineRepository deadlineRepository = createMockDeadlineRespository();


    private static final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private static final PrincipalAttributes mockPrincipal = mock(PrincipalAttributes.class);
    private static final UserAccountsClientService clientService = mock(UserAccountsClientService.class);
    private final Authentication principal = new Authentication(AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build());

    private final DeadlineController deadlineController = new DeadlineController(mockProjectRepository, deadlineRepository, new RegexService());
    private final ArrayList<Deadline> deadlines = new ArrayList<>();
    private static Project project;

    private static final String validDate = "2022-08-01";
    private static final String dateBeforeProject = "2020-08-01";
    private static final String dateAfterProject = "2023-08-01";
    private static final String validTime = "12:30:21";
    private static final String validName = "Deadline name";
    private static final String invalidName = "$ Deadline name $";

    @BeforeAll
    public static void beforeAll() {
        project = new Project("default", LocalDate.parse("2022-01-01"), LocalDate.parse("2022-12-31"), "test");
        when(mockProjectRepository.findById(project.getId())).thenReturn(java.util.Optional.ofNullable(project));
    }

    /**
     * Used to create an unauthorised user and to create the mock response for the Principal Attributes
     */
    public void createUnauthorisedUser() {
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

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), clientService)).thenReturn(user.build());
    }

    private void createAuthorisedUser() {
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
        user.addRoles(UserRole.TEACHER);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), clientService)).thenReturn(user.build());
    }

    // These tests are for the create method

    @Test
    void createDeadlineInvalidProjectId() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(2L, validName, validDate, 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineValidDeadline() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), "Deadline Name", "2022-04-10T17:00:00", 1);
        String expectedName = "Deadline Name";
        Assertions.assertEquals(expectedName, deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void createDeadlineNameLongerThan50Characters() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), "This is fifty-one characters, which is more than 50", validDate, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineDateBeforeProjectStartDate() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), validName, dateBeforeProject, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineDateAfterProjectEndDate() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), validName, dateAfterProject, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineInvalidDateString() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), validName, "INVALID", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    @Test
    void createDeadlineInvalidTypeOfOccasion() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.addDeadline(project.getId(), validName, validDate, 0);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    // These tests are for the edit method

    @Test
    void editDeadlineInvalidDeadlineId() {
        createAuthorisedUser();
        ResponseEntity<String> response = deadlineController.editDeadline(UUID.randomUUID().toString(), validName, validDate, validTime , 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void editDeadlineValidName() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), "NewName", validDate, validTime, 1);
        Assertions.assertEquals("NewName", deadlines.get(0).getName());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineNameLongerThan50Characters() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), "This is fifty-one characters, which is more than 50", validDate, validTime, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("ToBeEdited", deadlines.get(0).getName());
    }

    @Test
    void editDeadlineNoName() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), "", validDate, validTime, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editDeadlineValidDate() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, "2022-06-18", validTime, 1);
        LocalDate expectedDate = LocalDate.parse("2022-06-18");
        Assertions.assertEquals(expectedDate, deadlines.get(0).getEndDate());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineDateBeforeProjectStartDate() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "Test name", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, "2021-01-01", validTime, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineDateAfterProjectEndDate() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, "2023-01-01", validTime, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineInvalidDateString() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);

        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, "INVALID", validTime, 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(project.getStartDate(), deadlines.get(0).getEndDate());
    }

    @Test
    void editDeadlineValidTime() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, validDate, "12:30:22", 1);
        LocalTime expectedTime = LocalTime.parse("12:30:22");
        Assertions.assertEquals(expectedTime, deadlines.get(0).getEndTime());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void editDeadlineInvalidTimeString() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.parse("12:30:21"), 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(),validName, validDate, "INVALID", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(LocalTime.parse("12:30:21"), deadlines.get(0).getEndTime());
    }

    @Test
    void editDeadlineInvalidTypeOfOccasion() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<String> response = deadlineController.editDeadline(deadline.getId(), validName, validDate, validDate, 0);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals(1, deadlines.get(0).getType());
    }

    @Test
    void editDeadlineMultipleDeadlinesSaved() {
        createAuthorisedUser();
        Deadline deadline1 = new Deadline(project, "ToStayTheSame", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline1);

        Deadline deadline2 = new Deadline(project, "ToBeEdited", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline2);

        ResponseEntity<String> response = deadlineController.editDeadline(deadline2.getId(), "NewName", validDate, validTime, 1);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("NewName", deadlines.get(1).getName());
        Assertions.assertEquals("ToStayTheSame", deadlines.get(0).getName());
    }

    // These tests are for the delete method

    @Test
    void deleteDeadlineInvalidDeadlineId() {
        createAuthorisedUser();
        ResponseEntity<Object> response = deadlineController.deleteDeadline(UUID.randomUUID().toString());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteDeadlineValidDeadlineId() {
        createAuthorisedUser();
        Deadline deadline = new Deadline(project, "ToBeDeleted", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline);
        ResponseEntity<Object> response = deadlineController.deleteDeadline(deadline.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, deadlines.size());
    }

    // These tests are for the get one deadline method

    @Test
    void getDeadlineInvalidId() {
        createAuthorisedUser();
        Deadline deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline1);

        Deadline deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline2);
        String invalidId = UUID.randomUUID().toString();
        //used to ensure it is actually invalid
        while (Objects.equals(invalidId, deadline1.getId()) || Objects.equals(invalidId, deadline2.getId())) {
            invalidId = UUID.randomUUID().toString();
        }

        ResponseEntity<Object> response = deadlineController.getDeadline(invalidId);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void getDeadlineValidId() {
        createAuthorisedUser();
        Deadline deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline1);

        Deadline deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline2);

        ResponseEntity<Object> response = deadlineController.getDeadline(deadline2.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("aDeadline2", ((Deadline) response.getBody()).getName());
    }

    // These tests are for the get list of deadlines method

    @Test
    void getDeadlinesListInvalidId() {
        createAuthorisedUser();
        createAuthorisedUser();

        Deadline deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline1);

        Deadline deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline2);

        Long invalidId = project.getId() + 1;
        ResponseEntity<Object> response = deadlineController.getDeadlinesList(invalidId);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, ((List<Deadline>) response.getBody()).size());
    }

    @Test
    void getDeadlinesListValidId() {
        createAuthorisedUser();

        Deadline deadline1 = new Deadline(project, "aDeadline", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline1);

        Deadline deadline2 = new Deadline(project, "aDeadline2", project.getStartDate(), LocalTime.MIN, 1);
        deadlineRepository.save(deadline2);

        Long invalidId = project.getId();
        ResponseEntity<Object> response = deadlineController.getDeadlinesList(invalidId);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, ((List<Deadline>) response.getBody()).size());
    }



    private DeadlineRepository createMockDeadlineRespository() {
        return new DeadlineRepository() {
            @Override
            public List<Deadline> findAllByProjectId(Long projectId) {
                List<Deadline> deadlineList = new ArrayList<>();
                for (Deadline deadline : deadlines) {
                    if (Objects.equals(deadline.getProject().getId(), projectId)) {
                        deadlineList.add(deadline);
                    }
                }
                return deadlineList;
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
            public Deadline getById(String eventId) {
                return null;
            }

            @Override
            public <S extends Deadline> S save(S entity) {
                if (!deadlines.contains(entity)) {
                    entity.setUuid(UUID.randomUUID().toString());
                    deadlines.add(entity);
                }
                return entity;
            }

            @Override
            public <S extends Deadline> Iterable<S> saveAll(Iterable<S> entities) {
                return null;
            }

            @Override
            public Optional<Deadline> findById(String uuid) {
                for (Deadline deadline : deadlines) {
                    if (Objects.equals(deadline.getId(), uuid)) {
                        return Optional.of(deadline);
                    }
                }
                return Optional.empty();
            }

            @Override
            public boolean existsById(String uuid) {
                return false;
            }

            @Override
            public Iterable<Deadline> findAll() {
                return null;
            }

            @Override
            public Iterable<Deadline> findAllById(Iterable<String> uuids) {
                return null;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(String uuid) {

            }

            @Override
            public void delete(Deadline entity) {
                deadlines.remove(entity);
            }

            @Override
            public void deleteAllById(Iterable<? extends String> uuids) {

            }

            @Override
            public void deleteAll(Iterable<? extends Deadline> entities) {

            }

            @Override
            public void deleteAll() {

            }
        };
    }
}
