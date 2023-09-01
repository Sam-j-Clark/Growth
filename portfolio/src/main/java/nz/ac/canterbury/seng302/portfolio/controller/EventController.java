package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The controller for managing requests to access and edit deadlines.
 */
@RestController
public class EventController {

    /** For logging the processes of this controller */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** To retrieve and edit information about projects */
    private final ProjectRepository projectRepository;

    /** To retrieve and edit information about events */
    private final EventRepository eventRepository;

    /** For checking the inputs against the regex */
    private final RegexService regexService;


    /* Reused Log statements */
    private static final String CALLED_LOG = "Called";
    private static final String SUCCESS_LOG = "Success";
    private static final String NOT_FOUND_MESSAGE = " was not found";


    /**
     * Autowired constructor to inject the required dependencies.
     *
     * @param projectRepository To retrieve and edit information about projects
     * @param eventRepository To retrieve and edit information about events
     * @param regexService For checking the inputs against the regex
     */
    @Autowired
    public EventController(ProjectRepository projectRepository, EventRepository eventRepository, RegexService regexService) {
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
        this.regexService = regexService;
    }


    /**
     * Mapping for a put request to add event.
     * The method first parses the two date strings that are passed as request parameters.
     * They are being passed in, in a format called ISO_DATE_TIME, the parsers converts them from that to the standard
     * LocalDateTime format that we use.
     *
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     *
     * The Event is then created with the parameters passed, and saved to the event repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add event to.
     * @param name      Name of event.
     * @param start     date of the start of the event
     * @param end       date of the end of the event.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addEvent")
    public ResponseEntity<Object> addEvent(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart") String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfEvent") int typeOfEvent
    ) {
        String methodLoggingTemplate = "PUT /addEvent: {}";
        try {
            logger.info(methodLoggingTemplate, CALLED_LOG);

            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);

            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + NOT_FOUND_MESSAGE
            ));

            regexService.checkInput(RegexPattern.OCCASION_TITLE, name, 1, 50, "Event title");

            if (project.getStartDate().isAfter(LocalDate.from(eventStart)) || project.getEndDate().isBefore(LocalDate.from(eventEnd))) {
                String returnMessage = "Date(s) exist outside of project dates";
                logger.warn(methodLoggingTemplate, returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            if (eventStart.isAfter(eventEnd)) {
                String returnMessage = "Start date cannot be before end date";
                logger.warn(methodLoggingTemplate, returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            Event event = new Event(project, name, eventStart, eventEnd.toLocalDate(), eventEnd.toLocalTime(), typeOfEvent);
            Event eventReturn = eventRepository.save(event);
            logger.info(methodLoggingTemplate, SUCCESS_LOG);
            return new ResponseEntity<>(eventReturn, HttpStatus.OK);
        } catch (CheckException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeParseException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            logger.error(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for a delete request for event.
     * Trys to find the event with the Id given.
     * If it can't find the event an exception is thrown and then caught, with the error being returned.
     * If it can find the event, it tries to delete the event and if successful returns OK.
     *
     * @param eventId Id of event to be deleted.
     * @return A status code indicating request was successful, or failed.
     */
    @DeleteMapping("/deleteEvent")
    public ResponseEntity<String> deleteEvent(
            @RequestParam(value = "eventId") String eventId
    ) {
        String methodLoggingTemplate = "DELETE /deleteEvent: {}";
        try {
            logger.info(methodLoggingTemplate, CALLED_LOG);
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + NOT_FOUND_MESSAGE
            ));
            eventRepository.delete(event);
            logger.info(methodLoggingTemplate, SUCCESS_LOG);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (EntityNotFoundException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.error(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for a post request to edit an event.
     * The method first gets the event from the repository. If the event cannot be retrieved, it throws an EntityNotFound exception.
     * <p>
     * The method then parses the date strings that is passed as a request parameter.
     * The parsers convert the dates to the standard LocalDateTime format.
     * <p>
     * The Event is then edited with the parameters passed, and saved to the event repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param eventId     the ID of the event to be edited.
     * @param name        the new name of the event.
     * @param start       the new start date and time of the event.
     * @param end         the new end date and time of the event.
     * @param typeOfEvent the new type of the event.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editEvent")
    public ResponseEntity<String> editEvent(
            @RequestParam(value = "eventId") String eventId,
            @RequestParam(value = "eventName") String name,
            @RequestParam(value = "eventStart") String start,
            @RequestParam(value = "eventEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfEvent") int typeOfEvent
    ) {
        String methodLoggingTemplate = "POST /editEvent: {}";
        try {
            logger.info(methodLoggingTemplate, CALLED_LOG);
            Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + eventId + NOT_FOUND_MESSAGE
            ));

            // eventStart and eventEnd return a string in the format "1986-01-28T11:38:00.01"
            // DateTimeFormatter.ISO_DATE_TIME helps parse that string by declaring its format.
            LocalDateTime eventStart = LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime eventEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);

            regexService.checkInput(RegexPattern.OCCASION_TITLE, name, 1, 50, "Event title");

            Project project = event.getProject();
            if (project.getStartDate().isAfter(LocalDate.from(eventStart)) || project.getEndDate().isBefore(LocalDate.from(eventEnd))) {
                String returnMessage = "Date(s) exist outside of project dates";
                logger.warn(methodLoggingTemplate, returnMessage);
                return new ResponseEntity<>(returnMessage, HttpStatus.BAD_REQUEST);
            }

            event.setName(name);
            event.setStartDate(eventStart);
            event.setDateTime(eventEnd);
            event.setType(typeOfEvent);
            eventRepository.save(event);
            logger.info(methodLoggingTemplate, SUCCESS_LOG);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CheckException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DateTimeException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>("Could not parse date(s)", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            logger.error(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets the list of events in a project and returns it.
     *
     * @param projectId The projectId to get the events from this project
     * @return A ResponseEntity with the events or an error
     */
    @GetMapping("/getEventsList")
    public ResponseEntity<Object> getEventsList(
            @RequestParam(value = "projectId") Long projectId
    ) {
        String methodLoggingTemplate = "GET /getEventsList: {}";
        try {
            logger.info(methodLoggingTemplate, CALLED_LOG);
            List<Event> eventList = eventRepository.findAllByProjectIdOrderByStartDate(projectId);
            eventList.sort(Comparator.comparing(Event::getStartDate));
            logger.info(methodLoggingTemplate, SUCCESS_LOG);
            return new ResponseEntity<>(eventList, HttpStatus.OK);
        } catch (Exception err) {
            logger.error(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns a single event from the id that was given
     *
     * @param eventId the event id
     * @return a single event
     */
    @GetMapping("/getEvent")
    public ResponseEntity<Object> getEvent(
            @RequestParam(value = "eventId") String eventId
    ) {
        String methodLoggingTemplate = "GET /getEvent: {}";
        try {
            logger.info(methodLoggingTemplate, CALLED_LOG);
            Event event = eventRepository.findById(eventId).orElseThrow();
            logger.info(methodLoggingTemplate, SUCCESS_LOG);
            return new ResponseEntity<>(event, HttpStatus.OK);
        } catch (NoSuchElementException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.error(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
