package nz.ac.canterbury.seng302.portfolio.demodata;


import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones.MilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * The service to initialize the milestone data.
 */
@Service
public class MilestoneData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the project */
    private final ProjectRepository projectRepository;

    /** The repository containing the milestones */
    private final MilestoneRepository milestoneRepository;

    /**
     * Adds in the default milestones.
     * @param projectRepository The repository containing the project.
     * @param milestoneRepository The repository containing the milestones.
     */
    @Autowired
    public MilestoneData(ProjectRepository projectRepository, MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
    }

    /**
     * Adds in 3 default milestones.
     */
    public void createMilestoneData(){
        logger.info("Creating default Milestones");
        Project project = projectRepository.findAll().iterator().next();
        Milestone milestone1 = new Milestone(project, "Finished the project!", LocalDate.parse("2022-05-01"), 1);
        Milestone milestone2 = new Milestone(project, "Lost all the money", LocalDate.parse("2022-06-01"), 2);
        Milestone milestone3 = new Milestone(project, "Wow look at that flying dog", LocalDate.parse("2022-07-01"), 3);

        milestoneRepository.save(milestone1);
        milestoneRepository.save(milestone2);
        milestoneRepository.save(milestone3);
    }
}
