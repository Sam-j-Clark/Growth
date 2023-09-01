package nz.ac.canterbury.seng302.identityprovider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a singleton instance of the urlService so that it can be used by the User class.
 * This makes the functionality of the UrlService accessible statically.
 */
@Component
public class UrlUtil {

    /** The UrlService singleton */
    private static UrlService urlService;


    /** Singleton constructor with autowired UrlService component */
    @Autowired
    private UrlUtil(UrlService urlService) {
        UrlUtil.urlService = urlService;
    }


    /**
     * A static getter which return the singleton instance of the UrlService.
     *
     * @return the urlService singleton
     */
    public static UrlService getUrlService() {
        return urlService;
    }
}
