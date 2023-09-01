package nz.ac.canterbury.seng302.portfolio;

/**
 * An exception to throw when a check doesn't pass.
 * It takes a string/message as an argument
 */
public class CheckException extends RuntimeException {
    public CheckException(String errorMessage) {
        super(errorMessage);
    }
}
