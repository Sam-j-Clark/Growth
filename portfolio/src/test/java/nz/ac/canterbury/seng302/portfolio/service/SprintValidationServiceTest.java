package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints.SprintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SprintValidationServiceTest {

    private Project project;
    private SprintValidationService service;
    private SprintRepository repository;
    private Sprint sprint;
    List<Sprint> sprintList;

    @BeforeEach
    void setUp() {
        project = new Project("Project Seng302",
                LocalDate.parse("2022-02-25"),
                LocalDate.parse("2022-09-30"),
                "SENG302 is all about putting all that you have learnt in" +
                        " other courses into a systematic development process to" +
                        " create software as a team.");

        sprintList = new ArrayList<>();
        repository = mock(SprintRepository.class);
        when(repository.getAllByProjectOrderByStartDateAsc(project)).thenReturn(sprintList);
    }


    @Test
    void testDatesWhenOnlyOneSprint() {
        sprint = new Sprint(project, "test", LocalDate.parse("2022-02-27"));
        sprintList.add(sprint);
        service = new SprintValidationService(repository, sprint);

        assertEquals(project.getStartDate(), service.getMinSprintStartDate());
        assertEquals(project.getEndDate(), service.getMaxSprintEndDate());
    }

    @Test
    void testDatesWhenFirstOfTwoSprints() {
        initTwoSprints();
        sprint = sprintList.get(0);
        service = new SprintValidationService(repository, sprint);

        assertEquals(project.getStartDate(), service.getMinSprintStartDate());
        assertEquals(sprintList.get(1).getStartDate().minusDays(1), service.getMaxSprintEndDate());
    }

    @Test
    void testDatesWhenSecondOfTwoSprints() {
        initTwoSprints();
        sprint = sprintList.get(1);
        service = new SprintValidationService(repository, sprint);

        assertEquals(sprintList.get(0).getEndDate().plusDays(1), service.getMinSprintStartDate());
        assertEquals(project.getEndDate(), service.getMaxSprintEndDate());

    }

    @Test
    void testSprintBetweenTwoOtherSprints() {
        initThreeSprints();
        sprint = sprintList.get(1);
        service = new SprintValidationService(repository, sprint);

        assertEquals(sprintList.get(0).getEndDate().plusDays(1), service.getMinSprintStartDate());
        assertEquals(sprintList.get(2).getStartDate().minusDays(1), service.getMaxSprintEndDate());
    }

    void initTwoSprints() {
        Sprint sprint1 = new Sprint(project, "test", LocalDate.parse("2022-02-27"));
        Sprint sprint2 = new Sprint(project, "test", LocalDate.parse("2022-03-27"));
        sprintList.add(sprint1);
        sprintList.add(sprint2);
    }

    void initThreeSprints() {
        initTwoSprints();
        Sprint sprint3 = new Sprint(project, "test", LocalDate.parse("2022-04-27"));
        sprintList.add(sprint3);

    }

}