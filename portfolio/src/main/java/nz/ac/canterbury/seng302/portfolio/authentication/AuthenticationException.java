package nz.ac.canterbury.seng302.portfolio.authentication;

/**
 * Authenticate exception to be thrown when an error occurs attempting to Authenticate (ie ipd connection errors)
 *
 * @author Sam Clark
 */
public class AuthenticationException extends Exception {

    /**
     * Authenticate exception to be thrown when an error occurs attempting to Authenticate (ie ipd connection errors)
     *
     * @param message - the error message.
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
