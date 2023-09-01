package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides access to the endpoint for retrieving profile images.
 * A singleton instance of this class is accessible through the UrlUtil class.
 *
 * @see UrlUtil
 */
@Service
public class UrlService {

    /** The environment variables which contain image path components */
    private final Environment env;


    /**
     * Autowired constructor to inject the required environment variables
     *
     * @param env - an interface to retrieve the environment variables
     */
    @Autowired
    public UrlService(Environment env) {
        this.env = env;
    }


    /**
     * Gets the absolute image path based off the users image name and the environment variables
     * for the protocol, endpoint, port and image root path.
     * Note: this method is required to provide the correct path for images independent of the server host.
     *
     * @param user - The user whose profile image is being retrieved
     * @return - A URL object that contains the profile image path.
     */
    public URL getProfileURL(User user) {

        String protocol = env.getProperty("protocol", "http");
        String hostName = env.getProperty("hostName", "localhost");
        int port = Integer.parseInt(env.getProperty("port", "9001"));
        String rootPath = env.getProperty("rootPath", "");

        try {
            return new URL(
                    protocol,
                    hostName,
                    port,
                    rootPath + user.getProfileImagePath()

            );
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL creation failed. Check application.properties has all required properties");
        }
    }
}
