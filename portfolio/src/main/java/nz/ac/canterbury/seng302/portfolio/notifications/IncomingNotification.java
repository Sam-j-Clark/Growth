package nz.ac.canterbury.seng302.portfolio.notifications;

/**
 * A Data Transfer Object (DTO) for clients to send messages about what they are doing to the server
 */
public class IncomingNotification {

    /**
     * Some additional data sent along with the request. This will either be the subtype of occasion or role being changed.
     */
    private final String data;

    /**
     * The ID of the edited occasion or user whose roles have been updated
     */
    private final String id;

    /**
     * The action that has been performed. One of create, delete, edit, stop, add role, or delete role.
     */
    private final String action;


    /**
     * Constructor for IncomingNotifications
     *
     * @param data The type of occasion we are editing
     * @param id   The ID of that occasion
     * @param action       The action that has been performed. One of create, delete, edit, stop, or roleChange.
     */
    public IncomingNotification(String data, String id, String action) {
        this.data = data;
        this.id = id;
        this.action = action;
    }


    public String getData() {
        return data;
    }


    public String getId() {
        return id;
    }


    public String getAction() {
        return action;
    }

}
