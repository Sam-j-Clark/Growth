package nz.ac.canterbury.seng302.portfolio.model.domain.projects.sprints;

import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SprintRepository extends CrudRepository<Sprint, String> {

    @Query
    List<Sprint> findAllByProjectId(Long projectId);

    @Query
    List<Sprint> findAllByIdNot(String id);

    @Query
    Sprint getSprintById(String id);

    @Query
    List<Sprint> getAllByProjectOrderByEndDateDesc(Project project);


    @Query
    List<Sprint> getAllByProjectOrderByStartDateAsc(Project project);


}
