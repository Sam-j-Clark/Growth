package nz.ac.canterbury.seng302.portfolio.service;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserRequest;
import nz.ac.canterbury.seng302.portfolio.authentication.AuthenticationException;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public AuthenticateClientService authenticateClientService;

    /**
     * This method attempts to authenticate a user by sending an Authentication request to the server and if successful
     * adding a Cookie, otherwise it does not add the cookie
     *
     * @param userRequest - The userRequest object with the authentication fields
     * @param request     - used for creating the cookie
     * @param response    - used for creating the cookie
     * @return authenticate response - contains information about the authentication attempt.
     * @throws AuthenticationException - if the Identity provider can't be reached.
     */
    public AuthenticateResponse attemptLogin(UserRequest userRequest,
                                             HttpServletRequest request,
                                             HttpServletResponse response) throws AuthenticationException {
        AuthenticateResponse authenticateResponse;
        //This try/catch block is the login attempt
        try {
            logger.info("Sending authentication request for username {}", userRequest.getUsername());
            authenticateResponse = authenticateClientService.authenticate(userRequest.getUsername(), userRequest.getPassword());
        } catch (StatusRuntimeException e) {
            logger.error("Error connecting to Identity Provider");
            throw new AuthenticationException("failed to connect to the Identity Provider");
        }
        //If the login was successful, create a cookie!
        if (authenticateResponse.getSuccess()) {
            logger.info("Login successful - Added cookie to username {}", authenticateResponse.getUsername());
            var domain = request.getHeader("host");
            CookieUtil.create(
                    response,
                    "lens-session-token",
                    authenticateResponse.getToken(),
                    true,
                    5 * 60 * 60, // Expires in 5 hours
                    domain.startsWith("localhost") ? null : domain
            );
        } else {
            logger.info(authenticateResponse.getMessage());
        }
        return authenticateResponse;
    }
}
