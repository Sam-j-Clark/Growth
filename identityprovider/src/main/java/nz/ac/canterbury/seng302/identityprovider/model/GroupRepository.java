package nz.ac.canterbury.seng302.identityprovider.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Represents the repository which stores the groups' data.
 */
public interface GroupRepository extends CrudRepository<Group, Integer> {

    /**
     * Gets a group by its short name, if it exists.
     *
     * @param shortName The short name of the group to be retrieved.
     * @return The optional of the group. The optional is empty if the group doesn't exist.
     */
    @Query
    Optional<Group> findByShortName(String shortName);

    /**
     * Gets a group by its long name, if it exists.
     *
     * @param longName The long name of the group to be retrieved.
     * @return The optional of the group. The optional is empty if the group doesn't exist.
     */
    @Query
    Optional<Group> findByLongName(String longName);

    /**
     * Gets a group by its ID.
     *
     * @param groupId The ID of hte group to be retrieved.
     * @return The group with the given ID.
     */
    @Query
    Group getGroupById(Integer groupId);
}
