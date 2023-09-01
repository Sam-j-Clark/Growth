package nz.ac.canterbury.seng302.portfolio.model.domain.projects;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    @Query
    Project getProjectById(Long projectId);

    @Query
    Project getProjectByName(String projectName);

}
