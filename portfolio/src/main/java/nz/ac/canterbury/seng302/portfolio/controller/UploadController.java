package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class UploadController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserAccountsClientService userAccountsClientService;


    /**
     * Shows the form to upload a profile image if the user is logged in
     *
     * @return The Thymeleaf upload html template.
     */
    @GetMapping("/uploadImage")
    public ModelAndView showUpload(
            @AuthenticationPrincipal Authentication principal
    ) {
        logger.info("Endpoint reached: GET /uploadImage");
        ModelAndView modelAndView = new ModelAndView("upload-image");
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        modelAndView.addObject("user", user);
        return modelAndView;
    }


    /**
     * Endpoint that sends the given file to the identity provider
     *
     * @param file The file sent in the body of the post request
     */
    @PostMapping("/upload")
    public ResponseEntity<Object> upload(
            @AuthenticationPrincipal Authentication authentication,
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        AuthState principal = authentication.getAuthState();
        logger.info("Endpoint reached: POST /upload");
        ModelAndView modelAndView = new ModelAndView("upload-image");
        int id = PrincipalAttributes.getIdFromPrincipal(principal);
        userAccountsClientService.uploadProfilePhoto(file.getInputStream(), id, "jpg");
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        return new ResponseEntity<>(new UserDTO(user), HttpStatus.OK);
    }
}
