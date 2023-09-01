package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for handling all the queries related to Skill objects.
 */
@Repository
public interface SkillRepository extends CrudRepository<Skill, Integer> {


    /**
     * Find a skill by its ID
     *
     * @param id the integer id of the skills
     * @return The skill object
     */
    @Query
    Skill findById(int id);


    /**
     * Finds a Skill object by its name.
     *
     * @param name the name of the skill being retrieved.
     * @return the skill object, if it exists.
     */
    @Query
    Optional<Skill> findByNameIgnoreCase(String name);


    /**
     * Find all skills by a user's Id, ignoring duplicate skills
     *
     * @param userId the integer id of the user who has the skill
     * @return The list of skill objects
     */
    @Query
    List<Skill> findDistinctByEvidenceUserId(@Param("userId") int userId);


    /**
     * Finds a unique skill by a user's Id and skill name.
     *
     * @param userId    The Id of the user whose skills are being searched.
     * @param skillName The case-insensitive target name of the skill .
     * @return The skill object, if it exists.
     */
    @Query
    Optional<Skill> findDistinctByEvidenceUserIdAndNameIgnoreCase(int userId, String skillName);


     /**
     * Finds a unique skill by a user Id and skill Id.
     *
     * @param userId The Id of the user whose skills are being searched.
     * @param id The id of the skill .
     * @return The skill object, if it exists.
     */
    @Query
    Optional<Skill> findDistinctByEvidenceUserIdAndId(int userId, Integer id);
}
