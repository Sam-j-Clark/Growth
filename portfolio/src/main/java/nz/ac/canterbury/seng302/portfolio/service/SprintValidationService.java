package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;

import java.time.LocalDate;
import java.util.List;

public class SprintValidationService {

    private final Project project;
    private final List<Sprint> sprintList;
    private final int sprintIndex;

    /**
     * Constructs a new SprintValidationService. We call the constructor ourselves instead of using spring so that we
     * can initialise the service with the sprint we want to check.
     *
     * @param sprintRepository A reference to the sprint repository
     * @param sprint           The sprint we wish to validate
     */
    public SprintValidationService(SprintRepository sprintRepository, Sprint sprint) {
        this.project = sprint.getProject();
        sprintList = sprintRepository.getAllByProjectOrderByStartDateAsc(project);
        sprintIndex = sprintList.indexOf(sprint);

    }


    /**
     * Gets the minimum allowed start date for a sprint. This will be either one day after the end date
     * of the previous sprint, or the start date of the project.
     *
     * @return A LocalDate representing the minimum allowed start date for a sprint
     */
    public LocalDate getMinSprintStartDate() {
        // Checks if the selected sprint is not the first on the list
        if (sprintIndex > 0) {
            // Limit the calendar to dates past the previous sprints end.
            return sprintList.get(sprintIndex - 1).getEndDate().plusDays(1);
        } else {
            // Else limit the calendar to be before the project start.
            return project.getStartDate();
        }
    }


    /**
     * Gets the maximum allowed end date for a sprint. This will either be one day before
     * the start date of the next sprint, or the end date of the project.
     *
     * @return A LocalDate representing the maximum allowed end date for a sprint
     */
    public LocalDate getMaxSprintEndDate() {
        // Checks if the selected sprint is not the last on the list
        if (sprintIndex < sprintList.size() - 1) {
            // Limit the calendar to dates before the next sprints starts.
            return sprintList.get(sprintIndex + 1).getStartDate().minusDays(1);
        } else {
            // Else limit the calendar to be before the project end.
            return project.getEndDate();
        }
    }


    /**
     * Ensures the given sprint modification is outside all current sprint dates
     *
     * @param sprintRequest The sprint request containing the proposed new sprint start and end dates.
     */
    public void checkNewSprintDateNotInsideOtherSprints(SprintRequest sprintRequest) {
        LocalDate startDate = LocalDate.parse(sprintRequest.getSprintStartDate());
        LocalDate endDate = LocalDate.parse(sprintRequest.getSprintEndDate());
        if (startDate.isBefore(getMinSprintStartDate())) {
            throw new CheckException("Start date is before previous sprints end date / project start date");
        }
        if (endDate.isAfter(getMaxSprintEndDate())) {
            throw new CheckException("End date is after next sprints start date / project end date");
        }
    }
}