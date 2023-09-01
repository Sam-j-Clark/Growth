package nz.ac.canterbury.seng302.portfolio.model.domain.projects.events;


import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;

import javax.naming.InvalidNameException;
import javax.persistence.Entity;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an Event entity.
 */
@Entity
public class Event extends Deadline {

    private LocalDateTime startDate;

    /**
     * Default JPA event constructor.
     */
    public Event() {
    }

    /**
     * Constructs an instance of the event object.
     *
     * @param project   The project in which the event occurs.
     * @param name      The name of the event.
     * @param startDate The start date and time of the event
     * @param endDate   The end date of the event.
     * @param endTime   The end time of the event.
     * @param type      The type of the event.
     * @throws DateTimeException    If the event's date does not occur between the project's start and end dates.
     * @throws InvalidNameException If the event's name is null or has length greater than fifty characters.
     */
    public Event(Project project, String name, LocalDateTime startDate, LocalDate endDate, LocalTime endTime, int type) throws DateTimeException, InvalidNameException {
        super(project, name, endDate, endTime, type);
        DateTimeService.checkDateInProject(project, startDate.toLocalDate());
        this.startDate = startDate;
    }

    /* Ignore the unused method warning, this method is used by the frontend to format the dates */
    public String getStartDateFormatted() {
        return getStartDate().format(DateTimeService.timeDateMonthYear());
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) throws DateTimeException {
        DateTimeService.checkDateInProject(this.getProject(), startDate.toLocalDate());
        this.startDate = startDate;
    }
}
