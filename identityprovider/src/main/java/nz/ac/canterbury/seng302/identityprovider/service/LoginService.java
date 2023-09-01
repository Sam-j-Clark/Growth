package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Service class used to hash passwords so they are not stored in plain text.
 * Hashing details adapted from <a href="https://www.quickprogrammingtips.com/java/how-to-securely-store-passwords-in-java.html">www.quickprogrammingtips.com</a>
 */
public class LoginService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Enum to store different possible outcomes of attempting to log in
     */
    enum LoginStatus {
        VALID,
        USER_INVALID,
        PASSWORD_INVALID
    }

    /**
     * Processes a request to login
     * @param foundUser The user, retrieved from the database, to check against. If the user was not in the database
     *                  may be null
     * @param request The AuthenticateRequest sent by the client, containing the password entered on the website
     * @return One of LoginStatus.VALID, LoginStatus.USER_INVALID, or LoginStatus.PASSWORD_INVALID
     */
    public LoginStatus checkLogin(User foundUser, AuthenticateRequest request) {

        if (foundUser == null || !foundUser.getUsername().equals(request.getUsername())) {
            logger.info("Authentication failure - could not find user");
            return LoginStatus.USER_INVALID;

        } else {
            //User in database
            if (passwordMatches(request.getPassword(), foundUser)) { // Password matches stored hash
                logger.info("Authentication success - {}", foundUser.getUsername());
                return LoginStatus.VALID;

            } else { // Incorrect password
                logger.info("Authentication failure - incorrect password for {}", foundUser.getUsername());
                return LoginStatus.PASSWORD_INVALID;
            }
        }
    }


    /**
     * Checks that the given password matches with the given user
     * @param password The password (from the website)
     * @param user The user to check against
     */
    public boolean passwordMatches(String password, User user) {
        try {
            return getHash(password, user.getSalt()).equals(user.getPwhash());
        } catch (PasswordEncryptionException exception) {
            logger.error(exception.getMessage());
            return false;
        }
    }


    /**
     * Hashes the given password
     * @param password A string containing the password to be hashed.
     * @param salt A string containing random bits to be added to the password. Likely generated using getNewSalt(),
     *             and then stored in the database with the user.
     * @return Base64 encoded hash
     */
    public String getHash(String password, String salt) throws PasswordEncryptionException {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;

        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);

        byte[] encBytes;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            encBytes = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // This exception will only be thrown if the java algorithm specification changes
            logger.error("ERROR - failed to hash password");
            throw new PasswordEncryptionException("Could not hash password: " + e.getMessage());
        }


        return Base64.getEncoder().encodeToString(encBytes);
    }

    /**
     * Generates 8 random bytes to be used as salt
     * @return Base64 encoded salt
     */
    public String getNewSalt() throws PasswordEncryptionException {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            // This exception will only be thrown if the java algorithm specification changes
            logger.error("ERROR - failed to retrieve salt for password");
            throw new PasswordEncryptionException("Could not get salt: " + e.getMessage());
        }

        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
