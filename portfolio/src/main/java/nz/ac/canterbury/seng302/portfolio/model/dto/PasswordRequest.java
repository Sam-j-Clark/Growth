package nz.ac.canterbury.seng302.portfolio.model.dto;

/**
 * A basic entity we create and populate
 * in order to form a password request
 */
public class PasswordRequest {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public PasswordRequest() {
        super();
    }

    public PasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
        super();
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
