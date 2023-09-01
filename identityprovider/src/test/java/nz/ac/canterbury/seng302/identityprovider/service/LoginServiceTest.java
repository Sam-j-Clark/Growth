package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.service.LoginService.LoginStatus;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    LoginService service;
    User user;

    @BeforeEach
    void setUp() throws PasswordEncryptionException {
        service = new LoginService();
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
    void testValidLogin() {
        LoginStatus status = service.checkLogin(user, AuthenticateRequest.newBuilder()
                .setUsername("test")
                .setPassword("password")
                .build());
        assertEquals(LoginStatus.VALID, status);
    }

    @Test
    void testInvalidUser() {
        LoginStatus status = service.checkLogin(null, AuthenticateRequest.newBuilder()
                .setUsername("test")
                .setPassword("password")
                .build());
        assertEquals(LoginStatus.USER_INVALID, status);
    }

    @Test
    void testInvalidPassword() {
        LoginStatus status = service.checkLogin(user, AuthenticateRequest.newBuilder()
                .setUsername("test")
                .setPassword("wrong-password")
                .build());
        assertEquals(LoginStatus.PASSWORD_INVALID, status);
    }




    @Test
    void testHashesSame() throws PasswordEncryptionException {

        String salt = service.getNewSalt();
        String hash1 = service.getHash("testpassword123", salt);
        String hash2 = service.getHash("testpassword123", salt);

        assertEquals(hash1, hash2);

    }

    @Test
    void testHashesDifferent() throws PasswordEncryptionException {

        String salt = service.getNewSalt();
        String hash1 = service.getHash("testpassword123", salt);
        String hash2 = service.getHash("differentpassword123", salt);

        assertNotEquals(hash1, hash2);

    }

    @Test
    void testSaltsRandom() throws PasswordEncryptionException {

        String salt1 = service.getNewSalt();
        String salt2 = service.getNewSalt();

        assertNotEquals(salt1, salt2);

    }

    @Test
    void testMatchingPasswords() throws PasswordEncryptionException {
        assertTrue(service.passwordMatches("password", user));
    }

    @Test
    void testNonMatchingPasswords() throws PasswordEncryptionException {
        assertFalse(service.passwordMatches("different_password", user));
    }
}