package nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;

import javax.persistence.Entity;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a Deadline entity.
 */
@Entity
public class Deadline extends Milestone {

    private LocalTime endTime;
    private LocalDateTime dateTime;


    /**
     * Default JPA deadline constructor.
     */
    public Deadline() {
    }


    /**
     * Constructs an instance of the deadline object.
     *
     * @param project The project in which the deadline occurs.
     * @param name    The name of the deadline.
     * @param endDate The end date of the deadline.
     * @param endTime The end time of the deadline.
     * @param type    The type of the deadline.
     * @throws DateTimeException    If the deadline's date does not occur between the project's start and end dates.
     */
    public Deadline(Project project, String name, LocalDate endDate, LocalTime endTime, int type) throws CheckException, DateTimeException {
        super(project, name, endDate, type);
        this.endTime = endTime;
        this.dateTime = LocalDateTime.of(endDate, endTime);
    }


    /**
     * Formats the object's date to the form "hh:mma E d MMMM y"
     *
     * @return The formatted date.
     */
    @Override
    public String getEndDateFormatted() {
        return LocalDateTime.of(this.getEndDate(), this.endTime).format(DateTimeService.timeDateMonthYear());
    }


    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime eventEnd) throws DateTimeException {
        this.dateTime = eventEnd;
        DateTimeService.checkDateInProject(this.getProject(), eventEnd.toLocalDate());
        setEndTime(eventEnd.toLocalTime());
        setEndDate(eventEnd.toLocalDate());
    }
}
