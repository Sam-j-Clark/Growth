package nz.ac.canterbury.seng302.portfolio.service;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.ProjectRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.SprintRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Provides service methods for dates and times. Compares and formats dates.
 */
@Service
public class DateTimeService {

    SprintRepository sprintRepository;


    @Autowired
    public DateTimeService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }


    /**
     * Checks that the end date occurs between the project's start and end dates.
     *
     * @param project The project defining the earliest and latest dates the end date can be.
     * @param date    The end date being validated.
     * @throws DateTimeException If the end date is before the project start or after the project end.
     */
    public static void checkDateInProject(Project project, LocalDate date) throws DateTimeException {
        if (date.isAfter(project.getEndDate()) || date.isBefore(project.getStartDate())) {
            throw new DateTimeException("Date(s) must occur during the project");
        }
    }


    /**
     * Gets a readable form of a date from a protobuf timestamp, in the form "dd MMM yyyy".
     *
     * @param timestamp The Timestamp object to be formatted as a human-readable time.
     * @return The timestamp, formatted in "dd MMM yyyy" form.
     */
    public static String getReadableDate(Timestamp timestamp) {
        Date date = new Date(timestamp.getSeconds() * 1000); // Date needs milliseconds
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        return df.format(date);
    }


    /**
     * Gets a readable form of a date from a protobuf timestamp, in months and years.
     *
     * @param timestamp The Timestamp object to be formatted as a human-readable time.
     * @return The timestamp, formatted in months and years form.
     */
    public static String getReadableTimeSince(Timestamp timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp.getSeconds(), 0, ZoneOffset.UTC);
        LocalDateTime now = LocalDateTime.now();

        long years = ChronoUnit.YEARS.between(dateTime, now);
        long months = ChronoUnit.MONTHS.between(dateTime, now) % 12;
        if (years > 0) {
            return years + " years, " + months + " months";
        } else {
            return months + " months";
        }
    }


    /**
     * Formats a date into E d MMMM y format.
     *
     * @return the formatted date.
     */
    public static DateTimeFormatter dayDateMonthYear() {
        return DateTimeFormatter.ofPattern("E d MMMM y");
    }


    /**
     * Formats a date into hh:mma E d MMMM y format.
     *
     * @return the formatted date.
     */
    public static DateTimeFormatter timeDateMonthYear() {
        return DateTimeFormatter.ofPattern("hh:mma E d MMMM y");
    }


    /**
     * Formats a date into yyyy-MM-dd format.
     *
     * @return the formatted date.
     */
    public static DateTimeFormatter yearMonthDay() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }


    /**
     * Formats a date into dd/MM/yyyy format.
     *
     * @return the formatted date.
     */
    public static DateTimeFormatter dayMonthYear() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }


    /**
     * Gets default occasion dates as LocalDateTime objects, based on the current date and the given project's dates.
     * <p>
     * The default start date is either today, if we are in a project, or the start of the project.
     * The default end date is either the day after the start date, or the end of the project if there is less than 1
     * day remaining in the project.
     *
     * @param project The project the occasions must occur within.
     * @return An object containing the start and end dates at indices 0 and 1, respectively.
     */
    public Pair<LocalDateTime, LocalDateTime> retrieveDefaultOccasionDates(Project project) {
        LocalDate projectEndDate = project.getEndDate();
        LocalDate projectStartDate = project.getStartDate();
        LocalDateTime defaultOccasionStart = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime defaultOccasionEnd;

        if (LocalDate.now().isBefore(projectStartDate) || LocalDate.now().isAfter(projectEndDate)) {
            defaultOccasionStart = project.getStartDate().atStartOfDay();
        }
        if (defaultOccasionStart.isBefore(projectEndDate.atStartOfDay().minusDays(1))) {
            defaultOccasionEnd = defaultOccasionStart.plusDays(1);
        } else {
            defaultOccasionEnd = projectEndDate.atStartOfDay();
        }

        return Pair.of(defaultOccasionStart, defaultOccasionEnd);
    }


    /**
     * Checks if the given date occurs during a sprint in the given project.
     *
     * @param dateToCheck The date to be checked.
     * @param project     The project containing the sprints being used as date ranges.
     * @return Whether the date is contained in any of the sprints for the given project.
     */
    public boolean dateIsInSprint(LocalDate dateToCheck, Project project) {
        for (Sprint sprint : sprintRepository.getAllByProjectOrderByStartDateAsc(project)) {
            if ((sprint.getStartDate().minusDays(1L).isBefore(dateToCheck)) && sprint.getEndDate().plusDays(1L).isAfter(dateToCheck)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks that the proposed new dates for the project don't fall inside existing sprints dates.
     * Also checks that the project's new date doesn't fall more than a year before the original start date.
     *
     * @param sprintRepository The repository that stores the sprints.
     * @param project          The project in question.
     * @param projectRequest   The project request that contains all the proposed changes.
     */
    public void checkProjectAndItsSprintDates(SprintRepository sprintRepository, Project project, ProjectRequest projectRequest) {
        List<Sprint> sprints = sprintRepository.findAllByProjectId(project.getId());
        sprints.sort((Comparator.comparing(Sprint::getStartDate)));
        LocalDate newProjectStart = LocalDate.parse(projectRequest.getProjectStartDate());
        LocalDate newProjectEnd = LocalDate.parse(projectRequest.getProjectEndDate());
        if (newProjectStart.isBefore(project.getStartDate().minusYears(1))) {
            throw new CheckException("Project cannot start more than a year before its original date");
        }
        if (newProjectStart.isAfter(newProjectEnd)) {
            throw new CheckException("End date cannot be before start date");
        }
        if (!sprints.isEmpty()) {
            Sprint firstSprint = sprints.get(0);
            if (firstSprint.getEndDate().isAfter(newProjectEnd) || firstSprint.getStartDate().isAfter(newProjectEnd)) {
                throw new CheckException("There is a sprint that falls after these new dates");
            }
            Sprint lastSprint = sprints.get(sprints.size() - 1);
            if (lastSprint.getStartDate().isBefore(newProjectStart) || lastSprint.getEndDate().isBefore(newProjectStart)) {
                throw new CheckException("There is a sprint that falls before these new dates");
            }
        }
    }


    /**
     * Checks that the project has room to add more sprints in.
     * Essentially checks that the new sprint start/end dates don't go past the end of the sprint.
     *
     * @param sprintRepository the repository for sprints
     * @param project          the project in question
     */
    public LocalDate checkProjectHasRoomForSprints(SprintRepository sprintRepository, Project project) {
        LocalDate startDate = project.getStartDate();
        List<Sprint> sprints = sprintRepository.findAllByProjectId(project.getId());
        sprints.sort((Comparator.comparing(Sprint::getStartDate)));
        if (!sprints.isEmpty()) {
            startDate = sprints.get(sprints.size() - 1).getEndDate().plusDays(1);
        }
        if (startDate.isAfter(project.getEndDate())) {
            throw new CheckException("No more room to add sprints within project dates!");
        }
        return startDate;
    }



    /**
     * Ensures the given sprint date is outside all other sprint dates.
     *
     * @param previousDateLimit The end date of the previous sprint.
     * @param nextDateLimit     The start date of the next sprint.
     * @param sprintRequest     The sprint request containing the proposed sprint start and end dates.
     */
    public void checkNewSprintDateNotInsideOtherSprints(LocalDate previousDateLimit, LocalDate nextDateLimit, SprintRequest sprintRequest) {
        LocalDate startDate = LocalDate.parse(sprintRequest.getSprintStartDate());
        LocalDate endDate = LocalDate.parse(sprintRequest.getSprintEndDate());
        if (startDate.isBefore(previousDateLimit)) {
            throw new CheckException("Start date is before previous sprints end date / project start date");
        }
        if (endDate.isAfter(nextDateLimit)) {
            throw new CheckException("End date is after next sprints start date / project end date");
        }
    }
}