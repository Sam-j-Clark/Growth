package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptionException;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {
    User user;

    @BeforeEach
    void setup() throws PasswordEncryptionException {
        user = new User(
                "test",
                "password",
                "test",
                "test",
                "test",
                "test",
                "test",
                "test/test",
                "test@example.com"
        );
    }


    @Test
    void testRemoveRole() {
        user.addRole(UserRole.STUDENT);
        user.addRole(UserRole.TEACHER);
        user.deleteRole(UserRole.TEACHER);

        assertEquals(1, user.getRoles().size());
    }
}