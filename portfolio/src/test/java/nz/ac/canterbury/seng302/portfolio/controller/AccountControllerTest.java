package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.authentication.AuthenticationException;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.PasswordRequest;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserRequest;
import nz.ac.canterbury.seng302.portfolio.service.LoginService;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {


    private final ProjectRepository projectRepository = mock(ProjectRepository.class);

    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
    private final HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
    private final LoginService loginService = mock(LoginService.class);
    private final RegexService regexService = new RegexService();

    private final AccountController accountController = new AccountController(mockClientService, loginService, regexService);

    private final Authentication principal = new Authentication(
            AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build()
    );

    @BeforeEach
    public void beforeAll() throws AuthenticationException {
        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);
        UserResponse user = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), mockClientService)).thenReturn(user);
        GetUserByIdRequest userByIdRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        when(mockClientService.getUserAccountById(userByIdRequest)).thenReturn(user);

        UserRegisterResponse userRegisterResponse = UserRegisterResponse.newBuilder().setIsSuccess(true).build();
        when(mockClientService.register(any(UserRegisterRequest.class))).thenReturn(userRegisterResponse);

        Project project = new Project("test");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Mockito.when(loginService.attemptLogin(any(), any(), any())).thenReturn(
                AuthenticateResponse.newBuilder().build()
        );
    }

    @Test
    void testGetRegister() {
        ModelAndView model = accountController.register();

        Assertions.assertTrue(model.hasView());
        Assertions.assertTrue(model.getModel().containsKey("generalUnicodeRegex"));
        Assertions.assertTrue(model.getModel().containsKey("firstLastNameRegex"));
        Assertions.assertTrue(model.getModel().containsKey("middleNameRegex"));
        Assertions.assertTrue(model.getModel().containsKey("generalUnicodeNoSpacesRegex"));
        Assertions.assertTrue(model.getModel().containsKey("emailRegex"));
    }

    @Test
    void testAttemptRegistrationNotAllMandatoryFields() {
        UserRequest userRequest = new UserRequest("TestCase", "Password");
        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Required field"));
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("is missing"));
    }

    @Test
    void testAttemptRegistrationIncorrectPatternEmailNoAtSign() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternEmailNothingBeforeAtSign() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternEmailNothingAfterAtSign() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternEmailNoDotSomething() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationValidEmail() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternFirstnameAsSpace() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname(" ");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectFirstnameWithComma() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Te,st");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectFirstnameWithHyphen() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Mary-Jane");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectFirstnameWithApostrophe() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Mc'Gregor");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectFirstnameWithPeriod() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Jr.");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternMiddlenameWithComma() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename("Luth, the great");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternMiddlenameWithHyphen() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename("Jade-Rose");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternMiddlenameWithApostrophe() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename("Mc'Gregor");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternMiddlenameWithPeriod() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename("Luth. the great");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternLastnameAsSpace() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("test");
        userRequest.setLastname(" ");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectLastnameWithComma() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("test");
        userRequest.setLastname("Test,ing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectLastnameWithHyphen() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("test");
        userRequest.setLastname("Test-ing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectLastnameWithApostrophe() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("test");
        userRequest.setLastname("O'Reilly");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectLastnameWithPeriod() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("test");
        userRequest.setLastname("Jr.");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternUsernameWithSpaceInIt() {

        UserRequest userRequest = new UserRequest("TestCase CaseTest", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternUsernameWithOnlySpace() {

        UserRequest userRequest = new UserRequest(" ", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternUsernameWithUnusualChars() {

        UserRequest userRequest = new UserRequest("TestCase900!^_^", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternPasswordWithOnlyFiveSpaces() {

        UserRequest userRequest = new UserRequest("TestCase", "     ");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectPatternPasswordWithUnusualChars() {

        UserRequest userRequest = new UserRequest("TestCase", "Password9057#!$%^&*_=");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationIncorrectPatternPasswordWithSpacesInIt() {

        UserRequest userRequest = new UserRequest("TestCase", "Password Not Correct");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname(null);
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectNicknameWithComma() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname("jim, the best");
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectNicknameWithHyphen() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname("jim-bo");
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectNicknameWithApostrophe() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname("Mc'Gregor");
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationCorrectNicknameWithPeriod() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setBio(null);
        userRequest.setNickname("Jr.");
        userRequest.setPersonalPronouns(null);
        userRequest.setMiddlename(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationBlankUsernameField() {

        UserRequest userRequest = new UserRequest("", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationBlankPasswordField() {

        UserRequest userRequest = new UserRequest("TestCase", "");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationBlankEmailField() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationBlankFirstnameField() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationBlankLastnameField() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationRequiredUsernameNullField() {

        UserRequest userRequest = new UserRequest(null, "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationRequiredPasswordNullField() {

        UserRequest userRequest = new UserRequest("TestCase", null);
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationRequiredEmailNullField() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail(null);
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationRequiredFirstnameNullField() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname(null);
        userRequest.setLastname("Testing");

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationRequiredLastnameNullFields() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAttemptRegistrationNullFieldsOtherThanRequired() {

        UserRequest userRequest = new UserRequest("TestCase", "Password");
        userRequest.setEmail("test@test.com");
        userRequest.setFirstname("Test");
        userRequest.setLastname("Testing");
        userRequest.setMiddlename(null);
        userRequest.setNickname(null);
        userRequest.setBio(null);
        userRequest.setPersonalPronouns(null);

        ResponseEntity<Object> response = accountController.attemptRegistration(userRequest, mockServletRequest, mockServletResponse);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    void testGetAccount() {
        ModelAndView modelAndView = accountController.account(principal);

        Assertions.assertTrue(modelAndView.hasView());
        Assertions.assertTrue(modelAndView.getModel().containsKey("generalUnicodeNoSpacesRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("generalUnicodeRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("firstLastNameRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("middleNameRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("emailRegex"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("user"));
        Assertions.assertTrue(modelAndView.getModel().containsKey("membersince"));

    }


    @Test
    void testEditAccount() {
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(true);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }


    @Test
    void testEditAccountBadRequest() {
        UserRequest userRequest = new UserRequest("testUser", "password");
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void testEditAccountFailToChange() {
        UserRequest userRequest = new UserRequest("testUser", "password");
        userRequest.setFirstname("Test");
        userRequest.setLastname("User");
        userRequest.setEmail("Test@Test.com");
        EditUserResponse.Builder editUserResponse = EditUserResponse.newBuilder();
        editUserResponse.setIsSuccess(false);
        editUserResponse.build();
        Mockito.when(mockClientService.editUser(Mockito.any())).thenReturn(editUserResponse.build());
        ResponseEntity<Object> response = accountController.editDetails(principal, userRequest);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }


    @Test
    void testEditPassword() {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(true);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testEditPasswordFailToChange() {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(false);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    void testEditPasswordPasswordsDontMatch() {
        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setNewPassword("password");
        passwordRequest.setConfirmPassword("password2");
        passwordRequest.setOldPassword("password");

        ChangePasswordResponse.Builder changePasswordresponse = ChangePasswordResponse.newBuilder();
        changePasswordresponse.setIsSuccess(false);
        Mockito.when(mockClientService.changeUserPassword(Mockito.any())).thenReturn(changePasswordresponse.build());
        ResponseEntity<Object> response = accountController.editPassword(principal, passwordRequest);
        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        Assertions.assertEquals("Confirm password does not match new password.", response.getBody());
    }


    @Test
    void testDeleteProfileImg() {

        DeleteUserProfilePhotoResponse.Builder delete = DeleteUserProfilePhotoResponse.newBuilder();
        delete.setIsSuccess(true);
        Mockito.when(mockClientService.deleteUserProfilePhoto(Mockito.any())).thenReturn(delete.build());
        ResponseEntity<String> response = accountController.deleteProfilePhoto(principal);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }


}
