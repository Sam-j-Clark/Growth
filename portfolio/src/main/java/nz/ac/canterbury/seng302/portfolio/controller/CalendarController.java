package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.CalendarService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CalendarController {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final EventRepository eventRepository;
    private final DeadlineRepository deadlineRepository;
    private final MilestoneRepository milestoneRepository;
    private final CalendarService calendarService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int TOOLTIP_LENGTH = 20;


    @Autowired
    private UserAccountsClientService userAccountsClientService;

    public CalendarController(ProjectRepository projectRepository, SprintRepository sprintRepository, EventRepository eventRepository, DeadlineRepository deadlineRepository, MilestoneRepository milestoneRepository, CalendarService calendarService) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.eventRepository = eventRepository;
        this.deadlineRepository = deadlineRepository;
        this.milestoneRepository = milestoneRepository;
        this.calendarService = calendarService;
    }


    /**
     * Get mapping for /calendar. Returns the calendar view.
     *
     * @param principal principal
     * @param projectId id of the project that the calendar will display
     * @return the calendar view
     */
    @GetMapping("/calendar")
    public ModelAndView getCalendar(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "projectId") Long projectId
    ) {
        try {
            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Event with id " + projectId + " was not found"
            ));

            ModelAndView model = new ModelAndView("monthlyCalendar");
            model.addObject("project", project);
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
            List<UserRole> roles = user.getRolesList();

            model.addObject(roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR));
            model.addObject("user", user);
            return model;

        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /calendar", err);
            return new ModelAndView("errorPage").addObject("errorMessage", err.getMessage());
        }
    }


    /**
     * Returns the sprints as in a json format, only finds the sprints that are within the start and end dates
     *
     * @param projectId the project to look for the sprints in
     * @param startDate start date to look for
     * @param endDate   end date to look for
     * @return ResponseEntity with status, and List of hashmaps.
     */
    @GetMapping("/getProjectSprintsWithDatesAsFeed")
    public ResponseEntity<Object> getProjectSprintsWithDates(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "start") String startDate,
            @RequestParam(value = "end") String endDate) {
        try {
            logger.info("GET REQUEST /getProjectSprintsWithDatesAsFeed");


            // It receives the startDate and endDate in a ZonedDateTime format.
            ZonedDateTime startDateLocalDateTime = ZonedDateTime.parse(startDate);
            ZonedDateTime endDateDateLocalDateTime = ZonedDateTime.parse(endDate);

            // To check against the sprints we need to convert from ZonedDateTime to LocalDate
            LocalDate sprintStartDate = LocalDate.from(startDateLocalDateTime);
            LocalDate sprintEndDate = LocalDate.from(endDateDateLocalDateTime);


            List<Sprint> sprints = sprintRepository.findAllByProjectId(projectId);
            List<HashMap<String, Object>> sprintsToSend = new ArrayList<>();

            for (Sprint sprint : sprints) {
                if (sprint.getStartDate().equals(sprintStartDate)
                        || sprint.getStartDate().isAfter(sprintStartDate) && sprint.getStartDate().isBefore(sprintEndDate)
                        || sprint.getEndDate().equals(sprintEndDate)
                        || sprint.getEndDate().isBefore(sprintEndDate)
                        || sprint.getStartDate().isBefore(sprintStartDate) && sprint.getEndDate().isAfter(sprintEndDate)) {
                    HashMap<String, Object> jsonedSprint = new HashMap<>();
                    jsonedSprint.put("title", sprint.getName());
                    jsonedSprint.put("id", sprint.getId());
                    jsonedSprint.put("start", (LocalDateTime.from(sprint.getStartDate().atStartOfDay())).toString());
                    jsonedSprint.put("end", (LocalDateTime.from(sprint.getEndDate().atStartOfDay().plusHours(24))).toString());
                    jsonedSprint.put("description", sprint.getDescription());
                    jsonedSprint.put("backgroundColor", sprint.getColour());
                    jsonedSprint.put("defaultColor", sprint.getColour());
                    jsonedSprint.put("allDay", true);
                    jsonedSprint.put("isSprint", true);
                    jsonedSprint.put("selected", false);
                    sprintsToSend.add(jsonedSprint);
                }
            }

            return new ResponseEntity<>(sprintsToSend, HttpStatus.OK);
        } catch (DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("GET REQUEST /getProjectSprintsWithDatesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets project in a json feed format
     *
     * @param projectId project to get
     * @return ResponseEntity with status, and List of hashmaps.
     */
    @GetMapping("/getProjectAsFeed")
    public ResponseEntity<Object> getProject(
            @RequestParam(value = "projectId") Long projectId) {
        try {
            logger.info("GET REQUEST /getProjectAsFeed");

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));

            List<HashMap<String, String>> projectToSend = new ArrayList<>();

            HashMap<String, String> jsonedProject = new HashMap<>();
            jsonedProject.put("title", project.getName());
            jsonedProject.put("start", project.getStartDate().toString());
            jsonedProject.put("end", project.getEndDate().plusDays(1).toString());
            jsonedProject.put("backgroundColor", "grey");

            projectToSend.add(jsonedProject);

            return new ResponseEntity<>(projectToSend, HttpStatus.OK);

        } catch (EntityNotFoundException err) {
            logger.warn(err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.error("GET REQUEST /getProjectAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets the project details
     *
     * @param projectId project to get
     * @return response entity with project, or error message
     */
    @GetMapping("/getProjectDetails")
    public ResponseEntity<Object> getProject(
            @RequestParam(value = "projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getProjectDetails");

            // Gets the project that the request is referring to.
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + " was not found"
            ));
            return new ResponseEntity<>(project, HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /getProjectDetails", err);
            return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Retrieves all events in the given project as a JSON feed. The feed contains the events' titles, start
     * dates, end dates, the calendar date on which they are being displayed, and the className eventsCalendar.
     *
     * The time 00:00 is considered the start of the new day, in alignment with the most common interpretation.
     *
     * @param projectId The project to which the events belong
     * @return The JSON feed for events in the given project
     */
    @GetMapping("getEventsAsFeed")
    public ResponseEntity<Object> getEventsAsFeed(
            @RequestParam(value = "projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getEventsAsFeed");
            List<HashMap<String, String>> eventsList = new ArrayList<>();
            HashMap<LocalDate, Integer> eventsCount = new HashMap<>();
            HashMap<LocalDate, String> eventsNames = new HashMap<>();
            List<Event> allEvents = eventRepository.findAllByProjectIdOrderByStartDate(projectId);

            for (Event event : allEvents) {
                List<LocalDate> dates = new ArrayList<>();
                LocalDate current = event.getStartDate().toLocalDate();
                while (event.getEndDate().isAfter(current.minusDays(1))) {
                    dates.add(current);
                    current = current.plusDays(1);
                }

                for (LocalDate date : dates) {
                    Integer countByDate = eventsCount.get(date);
                    String namesByDate = eventsNames.get(date);
                    String lineEnd = "\r";
                    if (event.getName().length() > TOOLTIP_LENGTH) {
                        lineEnd = "...\r";
                    }
                    if (countByDate == null) {
                        eventsCount.put(date, 1); //add date to map as key
                        eventsNames.put(date, event.getName().substring(0, Math.min(event.getName().length(), TOOLTIP_LENGTH)) + lineEnd);
                    } else {
                        countByDate++;
                        namesByDate += (event.getName().substring(0, Math.min(event.getName().length(), TOOLTIP_LENGTH)) + lineEnd);
                        eventsNames.replace(date, namesByDate);
                        eventsCount.replace(date, countByDate);
                    }
                }
            }

            for (Map.Entry<LocalDate, Integer> entry : eventsCount.entrySet()) {
                HashMap<String, String> jsonedEvent = new HashMap<>();
                jsonedEvent.put("title", String.valueOf(entry.getValue()));
                jsonedEvent.put("occasionTitles", eventsNames.get(entry.getKey()));
                jsonedEvent.put("classNames", "eventCalendar");
                jsonedEvent.put("content", "");
                jsonedEvent.put("start", entry.getKey().toString());
                jsonedEvent.put("end", entry.getKey().toString());
                eventsList.add(jsonedEvent);
            }

            return new ResponseEntity<>(eventsList, HttpStatus.OK);
        } catch (DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("GET REQUEST /getEventsAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Retrieves all deadlines in the given project as a JSON feed. The feed contains the deadlines' titles, start
     * dates, end dates, the calendar date on which they are being displayed, and the className deadlinesCalendar.
     *
     * The time 00:00 is considered the start of the new day, in alignment with the most common interpretation.
     *
     * @param projectId The project to which the deadlines belong
     * @return The JSON feed for deadlines in the given project
     */
    @GetMapping("getDeadlinesAsFeed")
    public ResponseEntity<Object> getDeadlinesAsFeed(
            @RequestParam(value = "projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getDeadlinesAsFeed");

            List<HashMap<String, String>> deadlinesList = calendarService.getOccasionsAsFeed(projectId, "deadline");

            return new ResponseEntity<>(deadlinesList, HttpStatus.OK);
        } catch (DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("GET REQUEST /getDeadlinesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Retrieves all milestones in the given project as a JSON feed. The feed contains the milestones' titles, start
     * dates, end dates, the calendar date on which they are being displayed, and the className milestoneCalendar.
     *
     * @param projectId The project to which the milestones belong
     * @return The JSON feed for milestones in the given project
     */
    @GetMapping("getMilestonesAsFeed")
    public ResponseEntity<Object> getMilestonesAsFeed(
            @RequestParam(value = "projectId") long projectId) {
        try {
            logger.info("GET REQUEST /getMilestonesAsFeed");

            List<HashMap<String, String>> milestonesList = calendarService.getOccasionsAsFeed(projectId, "milestone");

            return new ResponseEntity<>(milestonesList, HttpStatus.OK);
        } catch (DateTimeParseException err) {
            logger.warn("Date parameter(s) are not parsable {}", err.getMessage());
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("GET REQUEST /getMilestonesAsFeed", err);
            return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * For testing
     *
     * @param service service
     */
    public void setUserAccountsClientService(UserAccountsClientService service) {
        this.userAccountsClientService = service;
    }
}

