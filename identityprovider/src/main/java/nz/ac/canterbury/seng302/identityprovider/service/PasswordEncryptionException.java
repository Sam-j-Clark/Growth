package nz.ac.canterbury.seng302.identityprovider.service;

/**
 * Exception to throw when errors occur related to password encryption
 */
public class PasswordEncryptionException extends Exception {

    public PasswordEncryptionException(String errorMessage) {
        super(errorMessage);
    }
}
