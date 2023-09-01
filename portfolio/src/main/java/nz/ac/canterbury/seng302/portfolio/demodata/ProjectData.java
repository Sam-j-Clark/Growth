package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProjectData {

    /** The repository containing the project */
    private final ProjectRepository projectRepository;

    private boolean useExampleProject = true;

    @Autowired
    public ProjectData(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    public ProjectData(ProjectRepository projectRepository, boolean useExampleProject){
        this.projectRepository = projectRepository;
        this.useExampleProject = useExampleProject;
    }

    /**
     * Creates the default project
     */
    public void createDefaultProject() {
        if (useExampleProject) {
            projectRepository.save(new Project("Project Seng302",
                    LocalDate.parse("2022-02-25"),
                    LocalDate.parse("2022-12-30"),
                    "SENG302 is all about putting all that you have learnt in" +
                            " other courses into a systematic development process to" +
                            " create software as a team."));
        } else {
            projectRepository.save(new Project("Project %s".formatted(LocalDate.now().getYear())));
        }
    }
}
