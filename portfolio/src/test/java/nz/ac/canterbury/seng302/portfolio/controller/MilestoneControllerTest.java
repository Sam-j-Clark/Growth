package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;

class MilestoneControllerTest {

    private final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private final MilestoneRepository mockMilestoneRepository = mock(MilestoneRepository.class);

    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);


    private final MilestoneController milestoneController = new MilestoneController(mockProjectRepository, mockMilestoneRepository, new RegexService());

    private final Project project = new Project("test");


    @BeforeEach
    public void beforeEach() {

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
        userBuilder.addRoles(UserRole.TEACHER);
        UserResponse user = userBuilder.build();


        Mockito.when(PrincipalAttributes.getUserFromPrincipal(principal, mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        Mockito.when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);
        Mockito.when(mockProjectRepository.findById(Mockito.any())).thenReturn(Optional.of(project));

    }

    @Test
    void testAddMilestone() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void testAddMilestoneNoProject() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockProjectRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }


    @Test
    void testAddMilestoneBadTitle() {
        Milestone milestone = new Milestone(project, "@", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), milestone.getEndDate().toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Milestone title can only contain letters, numbers and spaces and must not start with whitespace.", response.getBody());
    }

    @Test
    void testAddMilestoneBadDate() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), LocalDate.now().minusYears(1).toString(), milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("End date must occur during project", response.getBody());
    }


    @Test
    void testAddMilestoneBadDateCantParse() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        ResponseEntity<Object> response = milestoneController.addMilestone(milestone.getProject().getId(), milestone.getName(), "cheese", milestone.getType());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Could not parse date(s)", response.getBody());
    }


    @Test
    void testEditMilestone() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().plusDays(1)), 2);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testEditMilestoneBadName() {
        Milestone milestone = new Milestone(project, "test", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "$$BAD_NAME$$", String.valueOf(LocalDate.now().plusDays(1)), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testEditMilestoneNoMilestone() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().plusDays(1)), 2);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testEditMilestoneBadParse() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", "cheese", 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Could not parse date(s)", response.getBody());
    }

    @Test
    void testEditMilestoneBadDate() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.editMilestone(milestone.getId(), "Name", String.valueOf(LocalDate.now().minusYears(1)), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("End date must occur during project", response.getBody());
    }


    @Test
    void testGetMilestoneList() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Milestone milestone2 = new Milestone(project, "testMilestone", LocalDate.now().plusDays(3), 1);
        Milestone milestone3 = new Milestone(project, "testMilestone", LocalDate.now(), 1);
        List<Milestone> milestoneList = new ArrayList<>();
        milestoneList.add(milestone);
        milestoneList.add(milestone2);
        milestoneList.add(milestone3);

        Mockito.when(mockMilestoneRepository.findAllByProjectIdOrderByEndDate(Mockito.any())).thenReturn(milestoneList);
        ResponseEntity<Object> response = milestoneController.getMilestonesList(project.getId());
        List<Milestone> milestoneListReturned = (List<Milestone>) response.getBody();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(milestoneListReturned.contains(milestone));
        Assertions.assertTrue(milestoneListReturned.contains(milestone2));
        Assertions.assertTrue(milestoneListReturned.contains(milestone3));
        Assertions.assertEquals(milestoneListReturned.get(0), milestone3);

    }


    @Test
    void testGetMilestone() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.getMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(milestone, response.getBody());
    }

    @Test
    void testGetMilestoneNotFound() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.getMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testDeleteMilestone() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.of(milestone));
        ResponseEntity<Object> response = milestoneController.deleteMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteMilestoneNotFound() {
        Milestone milestone = new Milestone(project, "testMilestone", LocalDate.now().plusDays(1), 1);
        Mockito.when(mockMilestoneRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        ResponseEntity<Object> response = milestoneController.deleteMilestone(milestone.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}



