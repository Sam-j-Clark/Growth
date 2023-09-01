package nz.ac.canterbury.seng302.identityprovider.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Manages the endpoint for image handling.
 */
@Controller
public class ImageController {

    /** For logging the requests related to image endpoints. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Retrieves the environment variables at runtime. */
    private final Environment env;

    @Autowired
    public ImageController(Environment env) {
        this.env = env;
    }


    /**
     * Processes requests to the profile/{name} endpoint. Creates an image file if one does not already exist.
     * Retrieves the image at the user's location.
     *
     * @param name     The name of the file to be retrieved - the same as the user's ID number.
     * @param response The response which returns the image to the requester.
     */
    @RequestMapping("/profile/{name}")
    public void image(@PathVariable("name") String name, HttpServletResponse response) {
        logger.info("Retrieving profile image: {}", name);

        String photoLocation = env.getProperty("photoLocation", "/src/main/resources/profile-photos/");
        File image = new File(photoLocation + name);

        if (!image.exists()) {
            logger.info("profile image does not exist using default image");
            image = new File(photoLocation + "default.png");
        }

        try (InputStream in = new FileInputStream(image)){
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.setContentLengthLong(image.length());
            IOUtils.copy(in, response.getOutputStream());
        } catch (IOException e) {
            logger.error("Error - {}", e.getMessage());
        }
    }
}
