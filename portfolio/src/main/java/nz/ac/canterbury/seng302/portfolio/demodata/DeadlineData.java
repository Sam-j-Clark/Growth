package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.deadlines.DeadlineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;

/**
 *The service to initialize the deadline data.
 */
@Service
public class DeadlineData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the project */
    private final ProjectRepository projectRepository;

    /** The repository containing the deadlines */
    private final DeadlineRepository deadlineRepository;

    /**
     * Adds the default deadlines.
     * @param projectRepository Repository containing the project.
     * @param deadlineRepository Repository containing the deadlines.
     */
    @Autowired
    public DeadlineData(ProjectRepository projectRepository, DeadlineRepository deadlineRepository){
        this.projectRepository = projectRepository;
        this.deadlineRepository = deadlineRepository;
    }

    /**
     * Adds in 4 default deadlines.
     */
    public void createDeadlineData(){
        try{
            logger.info("Creating default deadlines");
            Project project = projectRepository.findAll().iterator().next();
            Deadline deadline1 = new Deadline(project, "SENG 101 Assignment due", LocalDate.parse("2022-05-01"), LocalTime.parse("23:59:00"), 1);
            Deadline deadline2 = new Deadline(project, "Auckland Electoral Candidate", LocalDate.parse("2022-08-12"), LocalTime.parse("12:00:00"), 2);
            Deadline deadline3 = new Deadline(project, "NCEA level 3 Calculus exam", LocalDate.parse("2022-08-14"), LocalTime.parse("09:30:00"), 3);
            Deadline deadline4 = new Deadline(project, "NZ On Air Scripted General Audiences", LocalDate.parse("2022-09-29"), LocalTime.parse("16:00:00"), 4);
            deadlineRepository.save(deadline1);
            deadlineRepository.save(deadline2);
            deadlineRepository.save(deadline3);
            deadlineRepository.save(deadline4);
        } catch(NoSuchElementException | CheckException exception) {
            logger.error("Error occurred loading default deadlines");
            logger.error(exception.getMessage());
        }
    }
}
