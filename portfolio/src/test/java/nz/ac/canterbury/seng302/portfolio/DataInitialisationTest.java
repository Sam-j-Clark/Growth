package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.demodata.ProjectData;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.mockito.Mockito.times;

class DataInitialisationTest {

    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
    }

    @Test
    void testDefaultProjectHasCorrectName() {
        ProjectData projectData = new ProjectData(projectRepository, false);
        projectData.createDefaultProject();
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        Mockito.verify(projectRepository, times(1)).save(projectCaptor.capture());
        Project actual = projectCaptor.getValue();
        Assertions.assertEquals("Project %s".formatted(LocalDate.now().getYear()), actual.getName());

    }


}
