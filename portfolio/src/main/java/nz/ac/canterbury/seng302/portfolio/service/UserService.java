package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * A service class of user related methods.
 */
@Service
public class UserService {

    /**
     * Checks the users role and adds a boolean to the modelAndView so that it can be accessed by the front-end.
     *
     * @param user The user we want to check
     * @param modelAndView The modelAndView we want to add the boolean too.
     */
    public void checkAndAddUserRole(UserResponse user, ModelAndView modelAndView) {
        // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
        List<UserRole> roles = user.getRolesList();
        modelAndView.addObject("userCanEdit", roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR));
    }
}
