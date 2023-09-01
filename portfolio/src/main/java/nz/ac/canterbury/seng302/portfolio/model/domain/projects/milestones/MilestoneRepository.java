package nz.ac.canterbury.seng302.portfolio.model.domain.projects.milestones;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface MilestoneRepository extends CrudRepository<Milestone, String> {
    @Query("select m from #{#entityName} as m where m.startDate IS NULL and m.dateTime IS NULL and m.project.id = ?1 order by m.endDate")
    List<Milestone> findAllByProjectIdOrderByEndDate(Long projectId);

    @Query("select count(m) from #{#entityName} as m where m.startDate IS NULL and m.dateTime IS NULL and m.project.id = ?1")
    Long countMilestoneByProjectId(Long projectId);

    @Query
    Milestone getById(String eventId);

    @Query("select m from #{#entityName} as m where m.startDate IS NULL and m.dateTime IS NULL and m.project.id = ?1 and m.endDate = ?2")
    List<Milestone> findAllByProjectIdAndEndDate(Long projectId, LocalDate endDate);
}
