package nz.ac.canterbury.seng302.portfolio.notifications;

/**
 * A Data Transfer Object (DTO) for sending notifications from the server to each client
 */
public class OutgoingNotification {

    /**
     * The ID of the person making the edit
     */
    private final String editorId;

    /**
     * The name of the editor (firstname lastname)
     */
    private final String editorName;

    /**
     * The subtype of occasion. One of 'event', 'milestone', 'deadline', 'addRole', or 'deleteRole'
     */
    private final String data;

    /**
     * The ID of the edited occasion or role
     */
    private final String id;

    /**
     * The type of message
     */
    private final String action;


    /**
     * Constructor for OutgoingNotifications
     *
     * @param editorId     The id of the user making changes
     * @param editorName   The name of the user making changes
     * @param data The type of occasion we are editing
     * @param id   The ID of that occasion
     * @param action       The action that has been performed. One of create, delete, edit, or stop.
     */
    public OutgoingNotification(String editorId, String editorName, String data, String id, String action) {
        this.editorId = editorId;
        this.editorName = editorName;
        this.data = data;
        this.id = id;
        this.action = action;
    }


    public String getEditorId() {
        return editorId;
    }


    public String getEditorName() {
        return editorName;
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
