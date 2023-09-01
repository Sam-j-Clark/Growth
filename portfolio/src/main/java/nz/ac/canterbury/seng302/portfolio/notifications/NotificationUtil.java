package nz.ac.canterbury.seng302.portfolio.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    private static final NotificationService notificationService = new NotificationService();

    @Autowired
    private NotificationUtil() {
        super();
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

}
