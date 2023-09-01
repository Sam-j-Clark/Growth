package nz.ac.canterbury.seng302.portfolio.demodata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataInitialisationManagerPortfolio {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** To add in the default project */
    private final ProjectData projectData;

    /** To add in the default sprints */
    private final SprintData sprintData;

    /** To add in the default events */
    private final EventData eventData;

    /** To add in the default deadlines */
    private final DeadlineData deadlineData;

    /** To add in the default milestones */
    private final MilestoneData milestoneData;

    /** To add in the default milestones */
    private final GitRepositoryData gitRepositoryData;

    /** To add in the default evidence, weblinks and skills */
    private final EvidenceData evidenceData;


    @Autowired
    public DataInitialisationManagerPortfolio(
            ProjectData projectData,
            SprintData sprintData,
            EventData eventData,
            DeadlineData deadlineData,
            MilestoneData milestoneData,
            GitRepositoryData gitRepositoryData,
            EvidenceData evidenceData){
        this.projectData = projectData;
        this.sprintData = sprintData;
        this.eventData = eventData;
        this.deadlineData = deadlineData;
        this.milestoneData = milestoneData;
        this.gitRepositoryData = gitRepositoryData;
        this.evidenceData = evidenceData;
    }


    /**
     * Delegates the adding of test data, where data is required.
     */
    public void initialiseData() {
        try{
            projectData.createDefaultProject();
            sprintData.createSprintData();
            eventData.createEventData();
            deadlineData.createDeadlineData();
            milestoneData.createMilestoneData();
            gitRepositoryData.createGitRepositoriesData();
            evidenceData.createEvidenceData();
        } catch(Exception e) {
            logger.error("ERROR - Could not setup initial data");
            logger.error(e.getMessage());
        }
    }
}
