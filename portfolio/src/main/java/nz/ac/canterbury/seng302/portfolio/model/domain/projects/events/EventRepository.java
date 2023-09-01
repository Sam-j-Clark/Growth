package nz.ac.canterbury.seng302.portfolio.model.domain.projects.events;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, String> {
    @Query
    List<Event> findAllByProjectIdOrderByStartDate(Long projectId);

    @Query
    Event getById(String eventId);

    @Query
    Long countEventByProjectId(long projectId);
}
