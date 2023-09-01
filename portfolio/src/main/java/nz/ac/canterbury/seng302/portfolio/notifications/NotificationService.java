package nz.ac.canterbury.seng302.portfolio.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Notification service provides the logic for sending event edit notifications to subscribed users.
 */
@Service
public class NotificationService {

    /**
     * For logging
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Hashmap which stores the currently active edit notifications
     * Keys should be stored as a string of the format:
     * occasionType + ":" + occasionId
     * where the type and id are taken from the notification stored.
     */
    private final HashMap<String, OutgoingNotification> activeEditNotifications = new HashMap<>();

    /**
     * An index on activeEditNotifications
     * that maps editor ids to the keys for
     * the notifications they have made.
     * I.E. the set contains the keys for every notification in activeEditNotifications
     * where the editorId is the same as the key for this index
     * <p>
     * Keys are the editorIds with no formatting.
     */
    private final HashMap<String, HashSet<String>> editorIdIndex = new HashMap<>();


    /**
     * Stores the outgoing notification, to be later sent to other users.
     * This should only be done for actions where we need to handle its 'in-progress' state
     * e.g. an edit action.
     * Something like a delete action would NOT need to be stored, because its effects
     * happen (more or less) instantaneously.
     *
     * @param notification The notification to be stored. Must have a type and ID.
     */
    public void storeOutgoingNotification(OutgoingNotification notification) {
        String key = notification.getData() + ":" + notification.getId();
        logger.info("SERVICE - Storing notification: {}", key);
        activeEditNotifications.put(key, notification);
        //Update our index for editor ids
        HashSet<String> keychain = editorIdIndex.getOrDefault(notification.getEditorId(), new HashSet<>());
        keychain.add(key);
        editorIdIndex.put(notification.getEditorId(), keychain);
    }


    /**
     * Removes the outgoing notification from storage. If the notification exists in storage,
     * it will be removed; otherwise nothing will happen.
     *
     * @param notification The notification to be removed. Must have a type and ID.
     */
    public void removeOutgoingNotification(OutgoingNotification notification) {
        String key = notification.getData() + ":" + notification.getId();
        logger.info("SERVICE - Removing notification: {}", key);
        activeEditNotifications.remove(key);
        //Update our index for editor ids
        HashSet<String> keychain = editorIdIndex.getOrDefault(notification.getEditorId(), new HashSet<>());
        keychain.remove(key);
        editorIdIndex.put(notification.getEditorId(), keychain);
    }


    /**
     * returns a collection of all the active edit notifications.
     * This should be used to then send these notifications,
     * for example, to a user who has just subscribed to the socket.
     *
     * @return a Collection of all the stored notifications.
     */
    public Collection<OutgoingNotification> sendStoredNotifications() {
        logger.info("SERVICE - SENDING STORED NOTIFICATIONS");
        return activeEditNotifications.values();
    }


    /**
     * Will remove all the outgoing notifications that have the provided editor id from storage
     * and then return a list of those removed notifications
     *
     * @param editorId The id of the editor to remove by
     * @return A list (in no particular order) of all the removed notifications
     */
    public List<OutgoingNotification> removeAllOutgoingNotificationByEditorId(String editorId) {
        HashSet<String> keychain = editorIdIndex.getOrDefault(editorId, new HashSet<>());
        ArrayList<OutgoingNotification> removedNotifications = new ArrayList<>();
        for (String key : keychain) {
            //Get the notification, remove it, and then add it to our output
            OutgoingNotification notification = activeEditNotifications.get(key);
            removeOutgoingNotification(notification);
            removedNotifications.add(notification);
        }
        logger.info("Removed {} notifications from existing notifications.", removedNotifications.size());
        return removedNotifications;
    }
}
