package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PortfolioController {

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_PAGE_LOCATION = "errorPage";
    private final UserAccountsClientService userAccountsClientService;
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final RegexService regexService;
    private final DateTimeService dateTimeService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Autowired constructor for PortfolioController to inject the required beans
     *
     * @param sprintRepository          repository
     * @param projectRepository         repository
     * @param userAccountsClientService The bean used to get user information
     */
    @Autowired
    public PortfolioController(
            SprintRepository sprintRepository,
            ProjectRepository projectRepository,
            UserAccountsClientService userAccountsClientService,
            RegexService regexService,
            ProjectService projectService,
            DateTimeService dateTimeService
    ) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userAccountsClientService = userAccountsClientService;
        this.regexService = regexService;
        this.projectService = projectService;
        this.dateTimeService = dateTimeService;
    }


    /**
     * Get mapping for /portfolio endpoint. Adds information for projects and occasions to the model.
     *
     * @param principal The Authentication principal of the user making the request, for authentication.
     * @param projectId The ID of the project to display.
     * @return returns the portfolio view, or error-page
     */
    @GetMapping("/portfolio")
    public ModelAndView getPortfolio(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "projectId") long projectId) {
        try {
            logger.info("GET REQUEST /portfolio: Getting page");
            UserResponse user =
                    PrincipalAttributes.getUserFromPrincipal(
                            principal.getAuthState(), userAccountsClientService);

            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isEmpty()) {
                throw new EntityNotFoundException("Project not found");
            }
            Project project = projectOptional.get();

            ModelAndView modelAndView = new ModelAndView("portfolio");
            // Checks what role the user has. Adds boolean object to the view so that displays can be
            // changed on the frontend.
            List<UserRole> roles = user.getRolesList();
            modelAndView.addObject(
                    "userCanEdit",
                    (roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR)));

            Pair<LocalDateTime, LocalDateTime> defaultDates = dateTimeService.retrieveDefaultOccasionDates(project);

            modelAndView.addObject("project", project);
            modelAndView.addObject("sprints", sprintRepository.findAllByProjectId(project.getId()));
            modelAndView.addObject("eventNameLengthRestriction", Milestone.getNameLengthRestriction());
            modelAndView.addObject("defaultOccasionStart", defaultDates.getFirst());
            modelAndView.addObject("defaultOccasionEnd", defaultDates.getSecond());
            modelAndView.addObject("defaultMilestoneStart", defaultDates.getFirst().toLocalDate());
            modelAndView.addObject("user", user);
            modelAndView.addObject("projectId", projectId);
            modelAndView.addObject("titleRegex", RegexPattern.OCCASION_TITLE);

            return modelAndView;
        } catch (EntityNotFoundException err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView(ERROR_PAGE_LOCATION).addObject(ERROR_MESSAGE, err.getMessage());
        } catch (Exception err) {
            logger.error("GET REQUEST /portfolio", err);
            return new ModelAndView(ERROR_PAGE_LOCATION).addObject(ERROR_MESSAGE, err);
        }
    }


    /**
     * Request mapping for /editProject
     *
     * @param principal - The Authentication of the user making the request, for authentication
     * @param projectId The project to edit
     * @return Returns the project edit page or the error page
     */
    @RequestMapping("/editProject")
    public ModelAndView edit(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "projectId") Long projectId
    ) {
        String methodLoggingTemplate = "GET REQUEST /editProject {}";
        logger.info(methodLoggingTemplate, "Called");
        try {

            // Get user from server
            UserResponse user =
                    PrincipalAttributes.getUserFromPrincipal(
                            principal.getAuthState(), userAccountsClientService);

            // Gets the project that the request is referring to.
            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isEmpty()) {
                throw new EntityNotFoundException("Project not found");
            }
            Project project = projectOptional.get();

            // The view we are going to return.
            ModelAndView modelAndView = new ModelAndView("projectEdit");

            // Adds the project object to the view for use.
            modelAndView.addObject("project", project);

            // Values to set the max and min of datepicker inputs
            LocalDate minProjectStartDate = projectService.getMinProjectStartDate(project);
            LocalDate maxProjectStartDate = projectService.getMaxProjectStartDate(project);
            LocalDate minProjectEndDate = projectService.getMinProjectEndDate(project);
            modelAndView.addObject("minStartDate", minProjectStartDate);
            modelAndView.addObject("maxStartDate", maxProjectStartDate);
            modelAndView.addObject("minEndDate", minProjectEndDate);

            // Adds the username and profile photo to the view for use.
            modelAndView.addObject("user", user);

            //Validation/regex
            modelAndView.addObject("generalUnicodeRegex", RegexPattern.GENERAL_UNICODE);

            return modelAndView;

        } catch (EntityNotFoundException err) {
            logger.error(methodLoggingTemplate, "Error - " + err.getMessage());
            return new ModelAndView(ERROR_PAGE_LOCATION).addObject(ERROR_MESSAGE, err);
        } catch (Exception err) {
            logger.error(methodLoggingTemplate, "Error - " + err.getMessage());
            return new ModelAndView(ERROR_PAGE_LOCATION);
        }
    }


    /**
     * Post mapping for /projectEdit, this is called when user submits there project changes.
     *
     * @param editInfo A DTO of project from the inputs on the edit page.
     * @return Returns to the portfolio page.
     */
    @PostMapping("/projectEdit")
    public ResponseEntity<Object> editDetails(
            @ModelAttribute(name = "editProjectForm") ProjectRequest editInfo
    ) {
        try {
            logger.info("POST REQUEST /projectEdit: user is editing project - {}", editInfo.getProjectId());

            LocalDate projectStart = LocalDate.parse(editInfo.getProjectStartDate());
            LocalDate projectEnd = LocalDate.parse(editInfo.getProjectEndDate());


            Project project = projectRepository
                    .findById(Long.parseLong(editInfo.getProjectId()))
                    .orElseThrow(() -> new EntityNotFoundException(
                                    "Project with id " + editInfo.getProjectId() + "was not found"
                            )
                    );
            String projectName = editInfo.getProjectName();
            String projectDescription = editInfo.getProjectDescription();
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, projectName, 1, 50, "Project name");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, projectDescription, 0, 200, "Project description");
            dateTimeService.checkProjectAndItsSprintDates(sprintRepository, project, editInfo);

            if (projectStart.isBefore(projectService.getMinProjectStartDate(project))) {
                return new ResponseEntity<>("Project cannot start more than a year before today", HttpStatus.BAD_REQUEST);
            }
            if (projectStart.isAfter(projectService.getMaxProjectStartDate(project))) {
                return new ResponseEntity<>("There is a sprint that starts before that date", HttpStatus.BAD_REQUEST);
            }
            if (projectEnd.isBefore(projectService.getMinProjectEndDate(project))) {
                return new ResponseEntity<>("There is a sprint that extends after that date", HttpStatus.BAD_REQUEST);
            }
            if (projectEnd.isBefore(projectStart)) {
                return new ResponseEntity<>("End date cannot be before start date", HttpStatus.BAD_REQUEST);
            }

            //Updates the project's details
            project.setName(editInfo.getProjectName());
            project.setStartDate(projectStart);
            project.setEndDate(projectEnd);
            project.setDescription(editInfo.getProjectDescription());
            projectRepository.save(project);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException | CheckException err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(err.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /projectEdit", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get mapping for portfolio/addSprint This is called when user wants to add a sprint.
     *
     * @param projectId Project to add the sprint to.
     * @return a response entity response
     */
    @GetMapping("/portfolio/addSprint")
    public ResponseEntity<Object> addSprint(@RequestParam(value = "projectId") Long projectId) {
        try {
            logger.info("GET REQUEST /portfolio/addSprint");
            Project project =
                    projectRepository
                            .findById(projectId)
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "Project with id " + projectId + " was not found"));
            // Gets the amount of sprints belonging to the project
            int amountOfSprints = sprintRepository.findAllByProjectId(projectId).size() + 1;
            String sprintName = "Sprint " + amountOfSprints;
            LocalDate startDate = dateTimeService.checkProjectHasRoomForSprints(sprintRepository, project);
            Sprint sprint;
            if (startDate.plusWeeks(3).isAfter(project.getEndDate())) {
                sprint = sprintRepository.save(
                        new Sprint(project, sprintName, startDate, project.getEndDate()));
            } else {
                sprint = sprintRepository.save(new Sprint(project, sprintName, startDate));
            }
            return new ResponseEntity<>(sprint, HttpStatus.OK);
        } catch (CheckException checkException) {
            logger.warn(checkException.getMessage());
            return new ResponseEntity<>(checkException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("GET REQUEST /portfolio/addSprint", err);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for /sprintEdit. Looks for a sprint that matches the id and then populates the form.
     *
     * @param principal The authentication state
     * @param sprintId  The sprint id
     * @return Thymeleaf template
     */
    @RequestMapping("/sprintEdit")
    public ModelAndView sprintEdit(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "sprintId") String sprintId,
            @RequestParam(value = "projectId") Long projectId,
            RedirectAttributes attributes) {
        try {
            logger.info("GET REQUEST /sprintEdit");
            ModelAndView modelAndView = new ModelAndView("sprintEdit");
            UserResponse user =
                    PrincipalAttributes.getUserFromPrincipal(
                            principal.getAuthState(), userAccountsClientService);

            Sprint sprint =
                    sprintRepository
                            .findById(String.valueOf(sprintId))
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "Sprint with id " + projectId.toString() + " was not found"));

            Project project = projectRepository.getProjectById(projectId);
            SprintValidationService sprintValidator = new SprintValidationService(sprintRepository, sprint);

            LocalDate minSprintStartDate = sprintValidator.getMinSprintStartDate();
            LocalDate maxSprintEndDate = sprintValidator.getMaxSprintEndDate();

            LocalDate minSprintEndDate = minSprintStartDate.plusDays(1);
            LocalDate maxSprintStartDate = maxSprintEndDate.minusDays(1);

            modelAndView.addObject("minSprintStartDate", minSprintStartDate);
            modelAndView.addObject("maxSprintStartDate", maxSprintStartDate);
            modelAndView.addObject("minSprintEndDate", minSprintEndDate);
            modelAndView.addObject("maxSprintEndDate", maxSprintEndDate);

            //Validation regex
            modelAndView.addObject("generalUnicodeRegex", RegexPattern.GENERAL_UNICODE);

            String textForPreviousSprint;
            if (minSprintStartDate.equals(project.getStartDate())) {
                textForPreviousSprint =
                        "No previous sprints, project starts on " + minSprintStartDate.format(DateTimeService.dayMonthYear());
            } else {
                String formattedPreviousDate = minSprintStartDate.minusDays(1).format(DateTimeService.dayMonthYear());
                textForPreviousSprint =
                        "Previous sprint ends on " + formattedPreviousDate;
            }
            modelAndView.addObject("textForPrevSprint", textForPreviousSprint);

            String textForNextSprint;
            if (maxSprintEndDate.equals(project.getEndDate())) {
                textForNextSprint =
                        "No next sprint, project ends on  " + maxSprintEndDate.format(DateTimeService.dayMonthYear());
            } else {
                String formattedNextDate = maxSprintEndDate.plusDays(1).format(DateTimeService.dayMonthYear());
                textForNextSprint = "Next sprint starts on " + formattedNextDate;
            }
            modelAndView.addObject("textForNextSprint", textForNextSprint);

            // Adds the username to the view for use.
            modelAndView.addObject("user", user);
            // Add the sprint to the view for use.
            modelAndView.addObject("sprint", sprint);
            return modelAndView;
        } catch (Exception err) {
            logger.error("GET REQUEST /sprintEdit", err);
            attributes.addFlashAttribute(ERROR_MESSAGE, err);
            return new ModelAndView("redirect:/portfolio?projectId=" + projectId);
        }
    }


    /**
     * Get a list of all the sprints in a project by the project ID.
     *
     * @param projectId - The project that contains the sprints
     * @return A response entity containing the sprints and the HTTP status
     */
    @GetMapping("/getSprintList")
    public ResponseEntity<Object> getSprintList(@RequestParam(value = "projectId") Long projectId) {
        List<Sprint> sprintList = sprintRepository.findAllByProjectId(projectId);
        sprintList.sort(Comparator.comparing(Sprint::getStartDate));
        return new ResponseEntity<>(sprintList, HttpStatus.OK);
    }


    /**
     * Takes the request to update the sprint. Tries to update the sprint then redirects user.
     *
     * @param sprintRequest the thymeleaf-created form object
     * @return redirect to portfolio
     */
    @PostMapping("/sprintSubmit")
    public ResponseEntity<Object> updateSprint(
            @ModelAttribute(name = "sprintEditForm") SprintRequest sprintRequest) {

        try {
            logger.info("POST REQUEST /sprintSubmit");
            // Checks that the sprint request is acceptable
            projectService.checkSprintRequest(sprintRequest);

            LocalDate startDate = LocalDate.parse(sprintRequest.getSprintStartDate());
            LocalDate endDate = LocalDate.parse(sprintRequest.getSprintEndDate());
            Optional<Sprint> sprintOptional = sprintRepository.findById(sprintRequest.getSprintId());
            if (sprintOptional.isEmpty()) {
                throw new CheckException("Sprint id doesn't correspond to existing sprint");
            }
            Sprint sprint = sprintOptional.get();

            SprintValidationService sprintValidator = new SprintValidationService(sprintRepository, sprint);
            sprintValidator.checkNewSprintDateNotInsideOtherSprints(sprintRequest);

            sprint.setName(sprintRequest.getSprintName());
            sprint.setStartDate(startDate);
            sprint.setEndDate(endDate);
            sprint.setDescription(sprintRequest.getSprintDescription());
            sprint.setColour(sprintRequest.getSprintColour());
            sprintRepository.save(sprint);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CheckException checkException) {
            logger.warn("/sprintSubmit issue with SprintRequest: {}", checkException.getMessage());
            return new ResponseEntity<>(checkException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("POST REQUEST /sprintSubmit {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Mapping for delete request "deleteSprint"
     *
     * @param id UUID of sprint to delete
     * @return Confirmation of delete
     */
    @DeleteMapping("deleteSprint")
    public ResponseEntity<String> deleteSprint(@RequestParam(value = "sprintId") UUID id) {
        logger.info("DELETE REQUEST /deleteSprint");
        sprintRepository.deleteById(String.valueOf(id));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
