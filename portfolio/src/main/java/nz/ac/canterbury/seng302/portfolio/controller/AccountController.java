package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.dto.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.DateTimeService;
import nz.ac.canterbury.seng302.portfolio.service.LoginService;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Controller class for the account page.
 *
 * This page is responsible for displaying user information.
 */
@Controller
public class AccountController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The client service allowing requests to be made to the IdP. */
    private final UserAccountsClientService userAccountsClientService;

    /** Injected LoginService Bean for assisting logging in and adding token cookies */
    private final LoginService loginService;

    /** Injected Bean to assist with validation */
    private final RegexService regexService;


    /**
     * Autowired constructor to automatically inject the required beans.
     *
     * @param userAccountsClientService - The UserAccountsClientService bean to be injected
     * @param loginService - The LoginService bean to be injected
     * @param regexService - The RegexService bean to be injected
     */
    @Autowired
    public AccountController(UserAccountsClientService userAccountsClientService,
                             LoginService loginService,
                             RegexService regexService) {
        this.userAccountsClientService = userAccountsClientService;
        this.loginService = loginService;
        this.regexService = regexService;
    }


    /**
     * Redirects users to their account page if they go to the root path
     *
     * @return A redirect to the account model and view
     */
    @GetMapping("/")
    public ModelAndView indexRedirectToAccount() {
        return new ModelAndView("redirect:account");
    }


    /**
     * This method is responsible for populating the account page template
     * It adds in variables to the html template, as well as the values of those variables
     * It then returns the 'filled in' html template, to be displayed in a web browser
     *
     * Once a user class is created, we will want to supply this page with the specific user that is viewing it
     *
     * @param principal the principal
     * @return ModelAndView of accounts page
     */
    @RequestMapping("/account")
    public ModelAndView account(
            @AuthenticationPrincipal Authentication principal
    ) {
        try {
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
            logger.info("GET REQUEST /account - retrieving account details for user {}", user.getUsername());

            ModelAndView model = new ModelAndView("account");
            model.addObject("generalUnicodeRegex", RegexPattern.GENERAL_UNICODE);
            model.addObject("firstLastNameRegex", RegexPattern.FIRST_LAST_NAME);
            model.addObject("middleNameRegex", RegexPattern.MIDDLE_NAME);
            model.addObject("generalUnicodeNoSpacesRegex", RegexPattern.GENERAL_UNICODE_NO_SPACES);
            model.addObject("emailRegex", RegexPattern.EMAIL);
            model.addObject("user", user);
            String memberSince = DateTimeService.getReadableDate(user.getCreated())
                    + " (" + DateTimeService.getReadableTimeSince(user.getCreated()) + ")";
            model.addObject("membersince", memberSince);
            logger.info("Account details populated for {}", user.getUsername());
            return model;
        } catch (Exception err) {
            logger.error("GET /account: {}", err.getMessage());
            return new ModelAndView("error");
        }
    }


    /**
     * Gets the user via the principal. This is used to fill in the account page info
     *
     * @param principal the principal
     * @return a response entity with the userDTO
     */
    @GetMapping("/getUser")
    public ResponseEntity<Object> getUser(@AuthenticationPrincipal Authentication principal) {
        try {
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
            UserDTO userDTO = new UserDTO(user);
            logger.info("GET REQUEST /account - retrieving account details for user {}", user.getUsername());

            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (Exception err) {
            logger.error("GET /account: {}", err.getMessage());
            return new ResponseEntity<>("An error occurred. Please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Returns the template for the register page.
     *
     * @return Thymeleaf template for the register screen.
     */
    @GetMapping("/register")
    public ModelAndView register() {
        logger.info("GET REQUEST /register - get register page");
        ModelAndView model = new ModelAndView("accountRegister");
        model.addObject("generalUnicodeNoSpacesRegex", RegexPattern.GENERAL_UNICODE_NO_SPACES);
        model.addObject("firstLastNameRegex", RegexPattern.FIRST_LAST_NAME);
        model.addObject("middleNameRegex", RegexPattern.MIDDLE_NAME);
        model.addObject("generalUnicodeRegex", RegexPattern.GENERAL_UNICODE);
        model.addObject("emailRegex", RegexPattern.EMAIL);

        return model;
    }


    /**
     * Called when a user attempts to register a new account, if the registration is successful forwards a user to
     * their account page, otherwise informs the user why their attempt was unsuccessful.
     *
     * @param userRequest A UserRequest object used to retrieve user input from the html.
     * @return view The html page redirected to, either account details on successful registration or register on failure.
     */
    @PostMapping("/register")
    public ResponseEntity<Object> attemptRegistration(
            @ModelAttribute(name = "registerForm") UserRequest userRequest,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        String warningMessage = "Registration Failed: {}";
        logger.info("POST REQUEST /register - attempt to register new user");
        try {
            ResponseEntity<Object> checkCredentials = checkUsernameAndPassword(userRequest);
            ResponseEntity<Object> checkUserRequest = checkUserRequestNoPasswordOrUser(userRequest);
            if (checkCredentials.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.warn(warningMessage, checkCredentials.getBody());
                return checkCredentials;
            }
            if (checkUserRequest.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.warn(warningMessage, checkUserRequest.getBody());
                return checkUserRequest;
            }

            // Make UserRegisterRequest and send to Server
            UserRegisterResponse registerReply = userAccountsClientService.register(createUserRegisterRequest(userRequest));
            // Attempt to login new user
            if (registerReply.getIsSuccess()) {
                logger.info("Registration Success: {}", registerReply.getMessage());
                logger.info("Log in new user");
                loginService.attemptLogin(userRequest, servletRequest, servletResponse);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                logger.info(warningMessage, registerReply.getMessage());
                return new ResponseEntity<>(registerReply.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
        } catch (CheckException exception) {
            logger.error(warningMessage, exception.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error(warningMessage, err.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Checks that the UserRequest follows the required patterns and contains everything needed.
     *
     * @param userRequest the UserRequest to be checked.
     * @return ResponseEntity, either an accept, or a not accept with message as to what went wrong.
     */
    private ResponseEntity<Object> checkUsernameAndPassword(UserRequest userRequest) {

        try {
            regexService.checkInput(RegexPattern.GENERAL_UNICODE_NO_SPACES, userRequest.getPassword(), 5, 50, "Password");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE_NO_SPACES, userRequest.getUsername(), 1, 50, "Username");
        } catch (CheckException exception) {
            logger.warn("Registration failed to meet requirement, {}", exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    /**
     * Checks that the UserRequest follows the required patterns and contains everything needed.
     *
     * @param userRequest the UserRequest without password.
     * @return ResponseEntity, either an accept, or a not accept with message as to what went wrong.
     */
    private ResponseEntity<Object> checkUserRequestNoPasswordOrUser(UserRequest userRequest) {
        try {
            regexService.checkInput(RegexPattern.FIRST_LAST_NAME, userRequest.getFirstname(), 2, 100, "First name");
            regexService.checkInput(RegexPattern.MIDDLE_NAME, userRequest.getMiddlename(), 0, 100, "Middle name");
            regexService.checkInput(RegexPattern.FIRST_LAST_NAME, userRequest.getLastname(), 2, 100, "Last name");
            regexService.checkInput(RegexPattern.EMAIL, userRequest.getEmail(), 1, 100, "Email");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, userRequest.getNickname(), 0, 50, "Nick name");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, userRequest.getPersonalPronouns(), 0, 50, "Pronouns");
            regexService.checkInput(RegexPattern.GENERAL_UNICODE, userRequest.getBio(), 0, 250, "Bio");

            if (userRequest.getMiddlename() == null) {
                userRequest.setMiddlename("");
            }
            if (userRequest.getNickname() == null) {
                userRequest.setNickname("");
            }
            if (userRequest.getBio() == null) {
                userRequest.setBio("");
            }
            if (userRequest.getPersonalPronouns() == null) {
                userRequest.setPersonalPronouns("");
            }

        } catch (CheckException exception) {
            logger.warn("Registration or update failed to meet requirement, {}", exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    /**
     * Entry point for editing account details,
     * This also handle the logic for changing the account details,
     *
     * @param authentication The authentication state
     * @param editInfo       The thymeleaf-created form object
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/details")
    public ResponseEntity<Object> editDetails(
            @AuthenticationPrincipal Authentication authentication,
            @ModelAttribute(name = "editDetailsForm") UserRequest editInfo
    ) {
        try {
            ResponseEntity<Object> checkUserRequest = checkUserRequestNoPasswordOrUser(editInfo); // Checks that the userRequest object passes all checks
            if (checkUserRequest.getStatusCode() != HttpStatus.ACCEPTED) {
                logger.warn("Editing Failed: {}", checkUserRequest.getBody());
                return checkUserRequest;
            }

            EditUserRequest.Builder editRequest = EditUserRequest.newBuilder();
            AuthState principal = authentication.getAuthState();
            int userId = PrincipalAttributes.getIdFromPrincipal(principal);
            logger.info(" POST REQUEST /edit/details - update account details for user {}", userId);

            // Used to trim off leading and training spaces
            String firstname = editInfo.getFirstname().trim();
            String middlename = editInfo.getMiddlename().trim();
            String lastname = editInfo.getLastname().trim();
            String nickname = editInfo.getNickname().trim();

            EditUserRequest editUserRequest = editRequest.setUserId(userId)
                    .setFirstName(firstname)
                    .setMiddleName(middlename)
                    .setLastName(lastname)
                    .setNickname(nickname)
                    .setBio(editInfo.getBio())
                    .setPersonalPronouns(editInfo.getPersonalPronouns())
                    .setEmail(editInfo.getEmail())
                    .build();
            EditUserResponse reply = userAccountsClientService.editUser(editUserRequest);
            if (reply.getIsSuccess()) {
                logger.info("Successfully updated details for user {}", userId);
            } else {
                logger.error("Failed to update details for user {}", userId);
                return new ResponseEntity<>(reply.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>(new UserDTO(editUserRequest), HttpStatus.OK);
        } catch (Exception err) {
            logger.error("/edit/details ERROR: {}", err.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Entry point for editing the password.
     * This also handle the logic for changing the password.
     * Note: this injects an attribute called "passwordchangemessage" into the template it redirects to.
     *
     * @param authentication The authentication state
     * @param editInfo       the thymeleaf-created form object
     * @return a redirect to the main /edit endpoint
     */
    @PostMapping("/edit/password")
    public ResponseEntity<Object> editPassword(
            @AuthenticationPrincipal Authentication authentication,
            @ModelAttribute(name = "editPasswordForm") PasswordRequest editInfo
    ) {
        try {
            int userId = PrincipalAttributes.getIdFromPrincipal(authentication.getAuthState());
            logger.info("POST REQUEST /edit/password - update password for user {}", userId);
            ChangePasswordRequest.Builder changePasswordRequest = ChangePasswordRequest.newBuilder();
            ChangePasswordResponse changePasswordResponse;
            if (editInfo.getNewPassword().equals(editInfo.getConfirmPassword())) {
                regexService.checkInput(RegexPattern.GENERAL_UNICODE_NO_SPACES, editInfo.getNewPassword(), 5, 50, "Password");
                logger.info("New password and confirm password match, requesting change password service ({})", userId);
                //Create request
                changePasswordRequest.setUserId(userId)
                        .setCurrentPassword(editInfo.getOldPassword())
                        .setNewPassword(editInfo.getNewPassword());
                changePasswordResponse = userAccountsClientService.changeUserPassword(changePasswordRequest.build());
                if (changePasswordResponse.getIsSuccess()) {
                    logger.info("Password change success: {}", changePasswordResponse.getMessage());
                } else {
                    logger.warn("Password change failed: {}", changePasswordResponse.getMessage());
                    return new ResponseEntity<>(changePasswordResponse.getMessage(), HttpStatus.NOT_ACCEPTABLE);
                }

            } else {
                logger.info("Confirm password does not match new password. Cancelling password change for {}", userId);
                // Tell the user to confirm their passwords match
                return new ResponseEntity<>("Confirm password does not match new password.", HttpStatus.NOT_ACCEPTABLE);
            }
            //Give the user the response from the IDP
            return new ResponseEntity<>(changePasswordResponse.getMessage(), HttpStatus.OK);

        } catch (CheckException exception) {
            logger.error("/edit/password Could not change password {}", exception.getMessage());
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception err) {
            logger.error("/edit/password Error {}", err.getMessage());
            return new ResponseEntity<>(err.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Takes a UserRequest object populated from a registration form and returns a UserRegisterRequest to send to the server.
     *
     * @param userRequest - A UserRequest object populated from a accountRegister.html form.
     * @return userRegisterRequest - a populated userRegisterRequest from the user_accounts.proto format.
     */
    private UserRegisterRequest createUserRegisterRequest(UserRequest userRequest) {
        // Used to trim off leading and training spaces
        String firstname = userRequest.getFirstname().trim();
        String middlename = userRequest.getMiddlename().trim();
        String lastname = userRequest.getLastname().trim();
        String nickname = userRequest.getNickname().trim();

        logger.info("Creating user register request from UserRequest");
        UserRegisterRequest.Builder userRegisterRequest = UserRegisterRequest.newBuilder();
        userRegisterRequest.setUsername(userRequest.getUsername())
                .setPassword(userRequest.getPassword())
                .setFirstName(firstname)
                .setMiddleName(middlename)
                .setLastName(lastname)
                .setEmail(userRequest.getEmail())
                .setBio(userRequest.getBio())
                .setPersonalPronouns(userRequest.getPersonalPronouns())
                .setNickname(nickname);
        return userRegisterRequest.build();
    }


    /**
     * Processes a request to delete the profile image of the currently logged-in user.
     *
     * @param authentication - an Authentication object used to identify the user.
     * @return a response entity indicating the completion of the request processing.
     */
    @DeleteMapping("/deleteProfileImg")
    public ResponseEntity<String> deleteProfilePhoto(
            @AuthenticationPrincipal Authentication authentication
    ) {
        logger.info("Endpoint reached: DELETE /deleteProfileImg");
        int id = PrincipalAttributes.getIdFromPrincipal(authentication.getAuthState());

        DeleteUserProfilePhotoRequest deleteRequest = DeleteUserProfilePhotoRequest.newBuilder().setUserId(id).build();

        DeleteUserProfilePhotoResponse response = userAccountsClientService.deleteUserProfilePhoto(deleteRequest);
        if (response.getIsSuccess()) {
            logger.info("Profile photo deleted - {}", response.getMessage());
        } else {
            logger.warn("Didn't delete profile photo - {}", response.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}