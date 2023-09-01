package nz.ac.canterbury.seng302.portfolio.middleware;

import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

/**
 * Checks that a user has the teacher or administrator role before forwarding them onto the requested endpoint
 */
@Component
public class RoleBasedIntercepter implements HandlerInterceptor {

    /** To get the user's information */
    @Autowired
    public AuthenticateClientService authenticateClientService;

    /** To get the user's authentication status */
    @Autowired
    public UserAccountsClientService userAccountsClientService;

    /** To log when the checks are made */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Checks that a user has the teacher or course administrator role
     *
     * @param request  - The httpServlet request
     * @param response - The httpServlet response
     * @param handler  - Required parameter for override
     * @return trues if the user has the teacher or administrator role, else false
     * @throws Exception - If the AuthenticateClientService can't be found.
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        logger.info("RoleBasedInterceptor: RoleBasedIntercepter has been called for this endpoint: {}", request.getRequestURI());

        AuthState principal = authenticateClientService.checkAuthState();

        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);
        List<UserRole> usersRoles = user.getRolesList();
        if (usersRoles.contains(UserRole.TEACHER) || usersRoles.contains(UserRole.COURSE_ADMINISTRATOR)) {
            return true;
        } else {
            response.setStatus(401);
            PrintWriter writer = response.getWriter();
            writer.append("Oops! Looks like you don't have permission to do this action. Please reload the page");
            writer.close();
            response.flushBuffer();
            return false;
        }
    }
}
