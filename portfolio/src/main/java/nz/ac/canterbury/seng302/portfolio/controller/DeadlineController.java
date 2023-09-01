package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The controller for managing requests to access and edit deadlines.
 */
@RestController
public class DeadlineController {

    /** For logging the requests related to milestones. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** To retrieve and edit information about projects */
    private final ProjectRepository projectRepository;

    /** To retrieve and edit information about deadlines */
    private final DeadlineRepository deadlineRepository;

    /** For checking the inputs against the regex */
    private final RegexService regexService;


    /* Reused Log statements */
    private static final String NOT_FOUND_MESSAGE = " was not found";


    /**
     * Autowired constructor to inject the required dependencies into the controller.
     *
     * @param projectRepository - To retrieve and edit information about projects
     * @param deadlineRepository - To retrieve and edit information about deadlines
     * @param regexService - For checking the inputs against the regex
     */
    @Autowired
    public DeadlineController(ProjectRepository projectRepository,
                              DeadlineRepository deadlineRepository,
                              RegexService regexService) {
        this.projectRepository = projectRepository;
        this.deadlineRepository = deadlineRepository;
        this.regexService = regexService;
    }


    /**
     * Mapping for a put request to add a deadline.
     * The method first parses a date and time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format and a LocalTime format
     *
     * The project is then grabbed from the repository by its ID.
     * If the project can't be found, it throws an EntityNotFoundException
     *
     * The deadline is then created with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param projectId id of project to add deadline to.
     * @param name      Name of milestone.
     * @param end       end of the deadline
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PutMapping("/addDeadline")
    public ResponseEntity<Object> addDeadline(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineEnd") String end,
            @RequestParam(defaultValue = "1", value = "typeOfOccasion") int typeOfOccasion
    ) {
        String methodLoggingTemplate = "PUT /addDeadline: {}";
        logger.info(methodLoggingTemplate, "Called");
        try {
            Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException(
                    "Project with id " + projectId + NOT_FOUND_MESSAGE
            ));
            regexService.checkInput(RegexPattern.OCCASION_TITLE, name, 1, 50, "Deadline title");
            LocalDateTime deadlineEnd = LocalDateTime.parse(end, DateTimeFormatter.ISO_DATE_TIME);

            Deadline deadline = new Deadline(project, name, deadlineEnd.toLocalDate(), deadlineEnd.toLocalTime(), typeOfOccasion);
            Deadline deadlineReturn = deadlineRepository.save(deadline);

            logger.info("PUT /addDeadline: Success");
            return new ResponseEntity<>(deadlineReturn, HttpStatus.OK);
        } catch (CheckException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException | DateTimeException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for a post request to edit a deadline.
     * The method first gets the deadline from the repository. If the deadline cannot be retrieved, it throws an EntityNotFound exception.
     *
     * The method then parses a date string and a time string that is passed as a request parameter.
     * The parser converts it to the standard LocalDate format.
     *
     * The deadline is then edited with the parameters passed, and saved to the deadline repository.
     * If all went successful, it returns OK, otherwise one of the errors is returned.
     *
     * @param deadlineId     the ID of the deadline being edited.
     * @param name           the new name of the deadline.
     * @param dateEnd        the new date of the deadline.
     * @param timeEnd        the new time of the deadline
     * @param typeOfOccasion the new type of the deadline.
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @PostMapping("/editDeadline")
    public ResponseEntity<String> editDeadline(
            @RequestParam(value = "deadlineId") String deadlineId,
            @RequestParam(value = "deadlineName") String name,
            @RequestParam(value = "deadlineDate") String dateEnd,
            @RequestParam(value = "deadlineTime") String timeEnd,
            @RequestParam(value = "typeOfOccasion") Integer typeOfOccasion
    ) {
        String methodLoggingTemplate = "PUT /editDeadline: {}";
        logger.info(methodLoggingTemplate, "Called");
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + NOT_FOUND_MESSAGE
            ));

            regexService.checkInput(RegexPattern.OCCASION_TITLE, name, 1, 50, "Deadline title");

            deadline.setName(name);
            deadline.setDateTime(LocalDateTime.of(LocalDate.parse(dateEnd), LocalTime.parse(timeEnd)));
            deadline.setType(typeOfOccasion);

            deadlineRepository.save(deadline);
            logger.info("PUT /deleteDeadline: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CheckException exception) {
            logger.warn(methodLoggingTemplate, exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (EntityNotFoundException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException | DateTimeException err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.warn(methodLoggingTemplate, err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for deleting an existing deadline.
     * The method attempts to get the deadline from the repository and if it cannot it will throw an EntityNotFoundException
     * Otherwise it will delete the deadline from the repository.
     *
     * @param deadlineId The UUID of the deadline to be deleted
     * @return A response indicating either success, or an error-code as to why it failed.
     */
    @DeleteMapping("/deleteDeadline")
    public ResponseEntity<Object> deleteDeadline(
            @RequestParam(value = "deadlineId") String deadlineId) {
        logger.info("PUT /deleteDeadline");
        try {
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow(() -> new EntityNotFoundException(
                    "Deadline with id " + deadlineId + NOT_FOUND_MESSAGE
            ));
            deadlineRepository.delete(deadline);
            logger.info("PUT /deleteDeadline: Success");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException err) {
            logger.warn("PUT /deleteDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.warn("PUT /deleteDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets the list of deadlines in a project and returns it.
     *
     * @param projectId The projectId to get the deadlines from this project
     * @return A ResponseEntity with the deadlines or an error
     */
    @GetMapping("/getDeadlinesList")
    public ResponseEntity<Object> getDeadlinesList(
            @RequestParam(value = "projectId") Long projectId
    ) {
        try {
            logger.info("GET /getDeadlinesList");
            List<Deadline> deadlineList = deadlineRepository.findAllByProjectId(projectId);
            deadlineList.sort(Comparator.comparing(Deadline::getDateTime));
            return new ResponseEntity<>(deadlineList, HttpStatus.OK);
        } catch (Exception err) {
            logger.error("GET /getDeadlineList: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns a single deadline from the id that was given
     *
     * @param deadlineId The deadline id
     * @return a single deadline
     */
    @GetMapping("/getDeadline")
    public ResponseEntity<Object> getDeadline(
            @RequestParam(value = "deadlineId") String deadlineId
    ) {
        try {
            logger.info("GET /getDeadline");
            Deadline deadline = deadlineRepository.findById(deadlineId).orElseThrow();
            return new ResponseEntity<>(deadline, HttpStatus.OK);
        } catch (NoSuchElementException err) {
            logger.error("GET /getDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception err) {
            logger.error("GET /getDeadline: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
