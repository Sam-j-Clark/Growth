package nz.ac.canterbury.seng302.portfolio.model.domain.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA repository which stores groups GitRepository information.
 * @see GitRepository
 */
@Repository
public interface GitRepoRepository extends CrudRepository<GitRepository, Integer> {

    /**
     * Finds and returns a list of all the Git repositories that belong to a specific group.
     * This is a list as the intention for repositories is that they are eventually many-to-many
     * with groups.
     *
     * @param groupId - The groupId of the group that owns the requested GitRepositories
     * @return A list of all the GitRepository objects (an empty list if none exist)
     */
    @Query
    List<GitRepository> findAllByGroupId(Integer groupId);
}
