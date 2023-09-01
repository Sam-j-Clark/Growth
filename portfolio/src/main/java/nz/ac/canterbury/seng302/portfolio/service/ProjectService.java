package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Service that helps with validation on the project and project edit page
 */
@Service
public class ProjectService {


    private final SprintRepository sprintRepository;

    RegexService regexService = new RegexService();

    @Autowired
    public ProjectService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }


    /**
     * Checks the SprintRequest DTO is all good and correct
     *
     * @param sprintRequest the SprintRequest to check
     */
    public void checkSprintRequest(SprintRequest sprintRequest) throws CheckException {
        try {
            String sprintName = sprintRequest.getSprintName();
            LocalDate sprintStartDate = LocalDate.parse(sprintRequest.getSprintStartDate());
            LocalDate sprintEndDate = LocalDate.parse(sprintRequest.getSprintEndDate());
            String sprintDescription = sprintRequest.getSprintDescription();
            String sprintColour = sprintRequest.getSprintColour();
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, sprintName, 1, 50, "Sprint name");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, sprintDescription, 0, 200, "Sprint description");
            regexService.checkInput(RegexPattern.HEX_COLOUR, sprintColour, 4, 7, "Sprint colour");
            if (sprintEndDate.isBefore(sprintStartDate)) {
                throw new CheckException("Sprint end date is before sprint start date");
            }
        } catch (DateTimeParseException err) {
            throw new CheckException("Date(s) is in incorrect format");
        }
    }


    /**
     * Gets the minimum start date for a project.
     * If the current start date is more than a year ago, returns that, otherwise returns exactly a year before today.
     *
     * @return LocalDate set a year in the past.
     */
    public LocalDate getMinProjectStartDate(Project project) {
        if (project.getStartDate().isBefore(LocalDate.now().minusYears(1))) {
            return project.getStartDate();
        } else {
            return LocalDate.now().minusYears(1);
        }
    }


    /**
     * Gets the max start date for a project. This will be the start date of the first sprint, or LocalDate.MAX if the
     * project has no sprints.
     *
     * @return A LocalDate to be compared against
     */
    public LocalDate getMaxProjectStartDate(Project project) {
        List<Sprint> sprintListStartDates = sprintRepository.getAllByProjectOrderByStartDateAsc(project);
        if (!sprintListStartDates.isEmpty()) {
            Sprint sprint = sprintListStartDates.get(0);
            return sprint.getStartDate();
        } else {
            return LocalDate.MAX;
        }
    }


    /**
     * Gets the minimum date that the project end date can be set to. Will either be the end date of the last sprint,
     * or if no sprints the project start date.
     *
     * @return A LocalDate to be compared against
     */
    public LocalDate getMinProjectEndDate(Project project) {
        List<Sprint> sprintListEndDates = sprintRepository.getAllByProjectOrderByEndDateDesc(project);
        if (!sprintListEndDates.isEmpty()) {
            Sprint sprint = sprintListEndDates.get(0);
            return sprint.getEndDate();
        } else {
            return project.getStartDate();
        }
    }
}
