package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for handling all the queries related to WebLink objects.
 */
@Repository
public interface WebLinkRepository extends CrudRepository<WebLink, Integer> {

    /**
     * Finds an WebLink object by its id.
     */
    @Query
    WebLink findById(int id);

}
