package nz.ac.canterbury.seng302.portfolio.notifications;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles websockets intentionally disconnecting or crashing. Sends a message to all connected clients to
 * inform that the client who disconnected is no longer editing anything.
 */
@Component
public class SocketCloseListener implements ApplicationListener<SessionDisconnectEvent> {

    /** Provides methods for sending STOMP messages */
    @Autowired
    private SimpMessagingTemplate template;

    /** For logging when disconnection events occur */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Notification service which provides the logic for sending notifications to subscribed users */
    private static final NotificationService notificationService = NotificationUtil.getNotificationService();


    /**
     * Spring publishes a SessionDisconnectEvent when a websocket goes down. This method listens for that event and
     * sends the STOMP message to all other clients.
     *
     * @param event The Spring SessionDisconnectionEvent
     */
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        logger.info("Got SessionDisconnectEvent");
        Principal principal = event.getUser();
        PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) principal;
        if (token != null) {

            Authentication auth = (Authentication) token.getPrincipal();
            AuthState state = auth.getAuthState();

            String editorId = String.valueOf(PrincipalAttributes.getIdFromPrincipal(state));
            //Remove all the active notifications belonging to that user
            removeAndInform(editorId);
        } else {
            throw new RuntimeException("AuthState null in websocket disconnect message");
        }
    }


    /**
     * Helper method that removes all active notifications with the editor id given
     * and then informs the listeners subscribed to the notifications/sending/occasions endpoint
     *
     * @param editorId The id of the person disconnected
     */
    private void removeAndInform(String editorId) {
        logger.info("User ID: {} has disconnected, removing their active notifications.", editorId);

        List<OutgoingNotification> removedNotifications = notificationService.removeAllOutgoingNotificationByEditorId(editorId);
        ArrayList<OutgoingNotification> stopNotifications = new ArrayList<>();
        for (OutgoingNotification notification : removedNotifications) {
            stopNotifications.add(new OutgoingNotification(
                    editorId,
                    notification.getEditorName(),
                    notification.getData(),
                    notification.getId(),
                    "stop"
            ));
        }
        template.convertAndSend("notifications/sending/occasions", stopNotifications);
    }
}
