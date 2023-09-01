package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.InvalidNameException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.mock;

@SpringBootTest
class EventControllerTest {


    private final ProjectRepository mockProjectRepository = mock(ProjectRepository.class);
    private final EventRepository mockEventRepository = mock(EventRepository.class);

    private final AuthState principal = AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build();
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);


    private final EventController eventController = new EventController(mockProjectRepository, mockEventRepository, new RegexService());

    private Project project;

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
        project = new Project("test");
        Mockito.when(mockProjectRepository.findById(project.getId())).thenReturn(Optional.of(project));

    }

    @Test
    void testAddEvent() throws InvalidNameException {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.save(Mockito.any())).thenReturn(event);
        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "testEvent", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


    }

    @Test
    void testAddEventBadDates() {

        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "testEvent", "not a date", "neither", 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }


    @Test
    void testAddEventBadName() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "test@Event", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Event title can only contain letters, numbers and spaces and must not start with whitespace.", response.getBody());

    }

    @Test
    void testAddEventEmptyName() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Event title is shorter than the minimum length of 1 character", response.getBody());

    }


    @Test
    void testAddEventNoProject() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        ResponseEntity<Object> response = eventController.addEvent(50L, "testEvent", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }


    @Test
    void testAddEventDatesOutsideOfProject() {
        LocalDateTime start = LocalDateTime.now().minusMonths(1);
        LocalDateTime end = LocalDateTime.now().plusMonths(1);

        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "testEvent", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Date(s) exist outside of project dates", response.getBody());

    }


    @Test
    void testAddEventThrowsException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Mockito.when(mockEventRepository.save(Mockito.any())).thenThrow(new RuntimeException());
        ResponseEntity<Object> response = eventController.addEvent(project.getId(), "testEvent", start.toString(), end.toString(), 1);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assertions.assertNull(response.getBody());

    }


    @Test
    void testDeleteEvent() throws InvalidNameException {

        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.deleteEvent(event.getId());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteEventNoEvent() throws InvalidNameException {

        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);
        ResponseEntity<String> response = eventController.deleteEvent(event.getId());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void testEditEvent() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Optional<Event> eventNow = mockEventRepository.findById(event.getId());
        if (eventNow.isPresent()) {
            Event eventReturned = eventNow.get();
            Assertions.assertEquals("testEvent", eventReturned.getName());
            Assertions.assertEquals(1, eventReturned.getType());
        }

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.editEvent(event.getId(), "changedName", LocalDateTime.now().toString(), LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Optional<Event> changedEvent = mockEventRepository.findById(event.getId());
        if (changedEvent.isPresent()) {
            Event eventReturned = changedEvent.get();
            Assertions.assertEquals("changedName", eventReturned.getName());
            Assertions.assertEquals(2, eventReturned.getType());
        }

    }


    @Test
    void testEditEventBadDates() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.editEvent(event.getId(), "changedName", "Cheese", LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Could not parse date(s)", response.getBody());

    }


    @Test
    void testEditEventDatesOutsideProject() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.editEvent(event.getId(), "changedName", LocalDateTime.now().minusYears(1).toString(), LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Date(s) exist outside of project dates", response.getBody());

    }


    @Test
    void testEditEventBadName() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.editEvent(event.getId(), "changed@Name", LocalDateTime.now().toString(), LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Event title can only contain letters, numbers and spaces and must not start with whitespace.", response.getBody());

    }


    @Test
    void testEditEventEventDoesNotExist() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        ResponseEntity<String> response = eventController.editEvent(UUID.randomUUID().toString(), "changedName", LocalDateTime.now().toString(), LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }


    @Test
    void testEditEventThrowsException() throws InvalidNameException {
        Event event = new Event(project, "testEvent", LocalDateTime.now(), LocalDate.now(), LocalTime.now(), 1);

        Mockito.when(mockEventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        Mockito.when(mockEventRepository.save(Mockito.any())).thenThrow(new RuntimeException());
        ResponseEntity<String> response = eventController.editEvent(event.getId(), "changedName", LocalDateTime.now().toString(), LocalDateTime.now().toString(), 2);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }


    @Test
    void testGetEventsList() throws InvalidNameException {
        ResponseEntity<Object> response = eventController.getEventsList(project.getId());
        List<Event> eventList = (List<Event>) response.getBody();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(0, eventList.size());


        Event event1 = new Event(project, "testEvent1", LocalDateTime.now(), LocalDate.now().plusDays(1), LocalTime.now(), 1);
        Event event2 = new Event(project, "testEvent2", LocalDateTime.now().plusDays(4), LocalDate.now().plusDays(5), LocalTime.now(), 1);
        Event event3 = new Event(project, "testEvent3", LocalDateTime.now().plusDays(2), LocalDate.now().plusDays(4), LocalTime.now(), 1);
        List<Event> returnList = new ArrayList<>();
        returnList.add(event1);
        returnList.add(event3);
        returnList.add(event2);

        Mockito.when(mockEventRepository.findAllByProjectIdOrderByStartDate(Mockito.anyLong())).thenReturn(returnList);

        response = eventController.getEventsList(project.getId());
        List<Event> eventList2 = (List<Event>) response.getBody();
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(3, eventList2.size());

        Assertions.assertEquals(eventList2.get(0).getName(), event1.getName());
        Assertions.assertEquals(eventList2.get(2).getName(), event2.getName());
        Assertions.assertEquals(eventList2.get(1).getName(), event3.getName());

    }


    @Test
    void testGetEventsListThrowsException() throws InvalidNameException {
        Mockito.when(mockEventRepository.findAllByProjectIdOrderByStartDate(Mockito.anyLong())).thenThrow(new RuntimeException());
        ResponseEntity<Object> response = eventController.getEventsList(project.getId());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }


    @Test
    void testGetEvent() throws InvalidNameException {
        Event event1 = new Event(project, "testEvent1", LocalDateTime.now(), LocalDate.now().plusDays(1), LocalTime.now(), 1);
        Mockito.when(mockEventRepository.findById(Mockito.any())).thenReturn(Optional.of(event1));
        ResponseEntity<Object> response = eventController.getEvent(event1.getId());


        Event returnEvent = (Event) response.getBody();


        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(event1, returnEvent);
    }


    @Test
    void testGetEventDoesNotExist() throws InvalidNameException {
        Event event1 = new Event(project, "testEvent1", LocalDateTime.now(), LocalDate.now().plusDays(1), LocalTime.now(), 1);
        Mockito.when(mockEventRepository.findById(Mockito.any())).thenThrow(new NoSuchElementException());
        ResponseEntity<Object> response = eventController.getEvent(event1.getId());

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }


}