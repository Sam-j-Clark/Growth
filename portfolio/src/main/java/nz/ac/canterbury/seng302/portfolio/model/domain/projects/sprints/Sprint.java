package nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints;

import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

/**
 * The entity for representing the sprints of a project.
 */
@Entity
public class Sprint {

    @Id
    private String id; // @Id lets JPA know it's the objects ID

    @ManyToOne
    private Project project;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String colour;

    protected Sprint() {/* Required constructor for JPA */}

    /**
     * Constructor for Sprint
     *
     * @param project     Project the sprint belongs to.
     * @param name        Name of sprint.
     * @param startDate   Start date of sprint.
     * @param endDate     End date of sprint.
     * @param description description of sprint.
     * @param colour      colour of sprint.
     */
    public Sprint(Project project, String name, LocalDate startDate, LocalDate endDate, String description, String colour) {
        this.id = UUID.randomUUID().toString();
        this.project = project;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.colour = colour;
    }


    /**
     * Constructor used when a sprint is added that can't go for 3 weeks.
     *
     * @param project     Project the sprint belongs to.
     * @param name        Name of sprint.
     * @param startDate   Start date of sprint.
     * @param endDate     End date of sprint (and project end date).
     */
    public Sprint(Project project, String name, LocalDate startDate, LocalDate endDate) {
        this(project, name, startDate, endDate, "No Description", String.format("#%06x", new Random().nextInt(0xffffff + 1)));
    }


    /**
     * Default Constructor for Sprint
     *
     * @param project Project the sprint belongs too.
     * @param name    Name of the Sprint.
     * @param startDate The date the sprint starts.
     */
    public Sprint(Project project, String name, LocalDate startDate) {
        this(project, name, startDate, startDate.plusWeeks(3));
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Helper method for thymeleaf to format the date nicely.
     * (Ignore unused warning)
     *
     * @return A formatted string with the sprint start time.
     */
    public String getStartDateFormatted() {
        return startDate.format(DateTimeService.dayDateMonthYear());
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Helper method for thymeleaf to format the date nicely.
     * (Ignore unused warning)
     *
     * @return A formatted string with the sprint end time.
     */
    public String getEndDateFormatted() {
        return endDate.format(DateTimeService.dayDateMonthYear());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}