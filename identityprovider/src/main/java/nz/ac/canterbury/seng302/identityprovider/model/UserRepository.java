package nz.ac.canterbury.seng302.identityprovider.model;

import org.springframework.data.repository.CrudRepository;

/**
 * Interface that defines how to interact with the database. Spring boot does the hard work under the hood
 * to actually implement these functions.
 *
 * @see  <a href="https://spring.io/guides/gs/accessing-data-jpa">https://spring.io/guides/gs/accessing-data-jpa/</a>
 */
public interface UserRepository extends CrudRepository <User, Integer> {
    /**
     * Gets a user object from the database using the ID.
     *
     * @param id The ID of the user being retrieved.
     * @return A user object, or null if none exist with the given ID.
     */
    User findById(int id);

    /**
     * Gets a user object from the database using the username.
     *
     * @param username The username of the user being retrieved.
     * @return A user object, or null if none exist with that username.
     */
    User findByUsername(String username);
}
