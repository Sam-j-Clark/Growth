package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

/**
 *The service to initialize the sprint data.
 */
@Service
public class SprintData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the project */
    private final ProjectRepository projectRepository;

    /** The repository containing the sprints */
    private final SprintRepository sprintRepository;


    /**
     * Adds the default sprints.
     *
     * @param projectRepository Repository containing the project.
     * @param sprintRepository Repository containing the sprints.
     */
    @Autowired
    public SprintData(ProjectRepository projectRepository, SprintRepository sprintRepository){
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
    }


    /**
     * Adds in 7 default sprints.
     */
    public void createSprintData(){
        try {
            logger.info("Creating default sprints");
            Project project = projectRepository.findAll().iterator().next();
            Sprint sprint1 = new Sprint(project, "Sprint 1", LocalDate.parse("2022-02-28"), LocalDate.parse("2022-03-09"), "Sprint 1", "#0066cc");
            Sprint sprint2 = new Sprint(project, "Sprint 2", LocalDate.parse("2022-03-14"), LocalDate.parse("2022-03-30"), "Sprint 2", "#ffcc00");
            Sprint sprint3 = new Sprint(project, "Sprint 3", LocalDate.parse("2022-04-04"), LocalDate.parse("2022-05-11"), "Sprint 3", "#f48c06");
            Sprint sprint4 = new Sprint(project, "Sprint 4", LocalDate.parse("2022-05-16"), LocalDate.parse("2022-07-20"), "Sprint 4", "#118ab2");
            Sprint sprint5 = new Sprint(project, "Sprint 5", LocalDate.parse("2022-07-25"), LocalDate.parse("2022-08-10"), "Sprint 5", "#219ebc");
            Sprint sprint6 = new Sprint(project, "Sprint 6", LocalDate.parse("2022-08-15"), LocalDate.parse("2022-09-14"), "Sprint 6", "#f48c06");
            Sprint sprint7 = new Sprint(project, "Sprint 7", LocalDate.parse("2022-09-19"), LocalDate.parse("2022-09-30"), "Sprint 7", "#f48c06");
            sprintRepository.save(sprint1);
            sprintRepository.save(sprint2);
            sprintRepository.save(sprint3);
            sprintRepository.save(sprint4);
            sprintRepository.save(sprint5);
            sprintRepository.save(sprint6);
            sprintRepository.save(sprint7);
        } catch (NoSuchElementException exception) {
            logger.error("Error occurred loading default sprints");
            logger.error(exception.getMessage());
        }
    }
}
