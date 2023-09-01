package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.Event;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.events.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

/**
 * The service to initialize the event data.
 */
@Service
public class EventData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the project */
    private final ProjectRepository projectRepository;

    /** The repository containing events */
    private final EventRepository eventRepository;

    /**
     * Adds the default events.
     * @param projectRepository Repository containing the project
     * @param eventRepository Repository containing the events
     */
    @Autowired
    public EventData(ProjectRepository projectRepository, EventRepository eventRepository){
        this.projectRepository = projectRepository;
        this.eventRepository = eventRepository;
    }


    /**
     * Adds in 4 default events
     */
    public void createEventData(){
        try{
            logger.info("Creating default events");
            Project project = projectRepository.findAll().iterator().next();
            Event event1 = new Event(project, "Term Break", LocalDateTime.parse("2022-04-11T08:00:00"), LocalDate.parse("2022-05-01"), LocalTime.parse("08:30:00"), 1);
            Event event2 = new Event(project, "Melbourne Grand Prix", LocalDateTime.parse("2022-04-10T17:00:00"), LocalDate.parse("2022-04-10"), LocalTime.parse("20:30:00"), 5);
            Event event3 = new Event(project, "Workshop Code Review", LocalDateTime.parse("2022-05-18T15:00:00"), LocalDate.parse("2022-05-18"), LocalTime.now(), 4);
            Event event4 = new Event(project, "Semester 2", LocalDateTime.parse("2022-07-18T15:00:00"), LocalDate.parse("2022-09-30"), LocalTime.now(), 6);
            eventRepository.save(event1);
            eventRepository.save(event2);
            eventRepository.save(event3);
            eventRepository.save(event4);
        } catch (NoSuchElementException | InvalidNameException exception) {
            logger.error("Error creating default Events");
            logger.error(exception.getMessage());
        }

    }
}
