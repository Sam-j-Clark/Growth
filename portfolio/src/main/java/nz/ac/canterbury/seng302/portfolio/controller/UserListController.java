package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.preferences.UserPrefRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.preferences.UserPrefs;
import nz.ac.canterbury.seng302.portfolio.service.PaginationService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


@Controller
public class UserListController {

    private final UserAccountsClientService userAccountsClientService;
    private UserPrefRepository prefRepository;
    private final PaginationService paginationService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int pageNum = 1;
    private int totalPages = 1;
    private Integer usersPerPageLimit = 10;
    private int offset = 0;
    private int totalNumUsers = 0;
    private String sortOrder = "firstname";
    private boolean isAscending = true;
    private ArrayList<Integer> footerNumberSequence = new ArrayList<>();
    private List<UserResponse> userResponseList;
    private final HashMap<String, UserRole> stringToRole = setUserRolesDict();


    /**
     * Autowired constructor
     * @param userAccountsClientService The user account service.
     * @param userPrefRepository The user account preference service.
     */
    @Autowired
    UserListController(UserAccountsClientService userAccountsClientService,
                       UserPrefRepository userPrefRepository,
                       PaginationService paginationService) {
        this.userAccountsClientService = userAccountsClientService;
        this.prefRepository = userPrefRepository;
        this.paginationService = paginationService;
    }


    /**
     * Used to create the list of users, 50 per page, by default sorted by users names. Adds all these values on
     * the webpage to be displayed. Also used for the other pages in the user list. Passes through users as well as
     * information needed to create the navigation.
     *
     * @param principal the principal representing the logged-in user's authentication
     * @param model parameters sent to thymeleaf template to be rendered into HTML
     * @param page an optional integer parameter that is used to get the correct page of users
     * @param order the order in which users will be sorted
     * @param isAscending indicates the reversal of the sort order
     * @param usersPerPage the number of users to be displayed on each paginated page of users
     * @return a model-and-view of the user list page
     */
    @GetMapping("/user-list")
    public ModelAndView getUserList(
            @AuthenticationPrincipal Authentication principal,
            Model model,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "sortField", required = false) String order,
            @RequestParam(name = "isAscending", required = false) String isAscending,
            @RequestParam(name = "usersPerPage", required = false) String usersPerPage)
    {
        logger.info("GET REQUEST /user-list - retrieve paginated users for the user list");

        selectSortOrder(PrincipalAttributes.getIdFromPrincipal(principal.getAuthState()),
                Objects.requireNonNullElse(order, ""),
                Objects.requireNonNullElse(isAscending, ""),
                Objects.requireNonNullElse(usersPerPage, ""));
        if (page != null) {
            pageNum = page;
        }

        if (pageNum <= 1) { //to ensure no negative page numbers
            pageNum = 1;
        }
        offset = (pageNum - 1) * usersPerPageLimit;

        PaginatedUsersResponse response = getPaginatedUsersFromServer();
        totalNumUsers = response.getPaginationResponseOptions().getResultSetSize();
        totalPages = totalNumUsers / usersPerPageLimit;
        if ((totalNumUsers % usersPerPageLimit) != 0) {
            totalPages++;
        }
        if (pageNum > totalPages) { //to ensure that the last page will be shown if the page number is too large
            pageNum = totalPages;
            offset = (pageNum - 1) * usersPerPageLimit;
            response = getPaginatedUsersFromServer();
        }

        footerNumberSequence = paginationService.createFooterNumberSequence(footerNumberSequence, totalPages, pageNum);
        userResponseList = response.getUsersList();
        addAttributesToModel(principal.getAuthState(), model);

        logger.info("RESOLVED /user-list");
        return new ModelAndView("user-list");
    }


    /**
     * A helper method to select the user's sort order for the user list.
     *
     * @param userId The userId that you're selecting the sort order for.
     * @param order  The order that the user send with the request. If they didn't send one, this should be ""
     *               This can be done easily with the line Objects.requireNonNullElse(order, "").
     * @param usersPerPage The number of users to be displayed on each paginated page.
     */
    private void selectSortOrder(int userId, String order, String isAscending, String usersPerPage) {
        String genericLogMessage = "VIEWING USERS - ID: " + userId + "{}";
        logger.info(genericLogMessage, " : Beginning sort order selection");
        UserPrefs user = prefRepository.getUserPrefsByUserId(userId);

        if (user != null) {
            logger.info(genericLogMessage, " : user found, fetching preferences...");
            this.sortOrder = user.getListSortPref();
            this.isAscending = user.getIsAscending();
            this.usersPerPageLimit = user.getUsersPerPage();
        } else {
            // if empty set default values
            this.sortOrder = "firstname";
            this.isAscending = true;
        }

        // check for new values
        if (!usersPerPage.isBlank()) {
            switch(usersPerPage){
                case "20" -> this.usersPerPageLimit = 20;
                case "40" -> this.usersPerPageLimit = 40;
                case "60" -> this.usersPerPageLimit = 60;
                case "all" -> this.usersPerPageLimit = 999999999;
                default -> this.usersPerPageLimit = 10;
            }
        }

        if (!isAscending.isBlank()) {
            this.isAscending = Boolean.parseBoolean(isAscending);
        }

        if (!order.isBlank()){
            this.sortOrder = order;
        }

        prefRepository.save(new UserPrefs(userId, this.sortOrder, this.isAscending, this.usersPerPageLimit));
        logger.info(genericLogMessage, " : preferences saved successfully");
    }

    /**
     * Adds to the model the attributes required to display, format, and interact with the user list table.
     *
     * @param principal the principal representing the logged-in user's authentication
     * @param model the model to which the attributes will be added.
     */
    private void addAttributesToModel(AuthState principal, Model model) {
        UserRole[] possibleRoles = UserRole.values();
        possibleRoles = Arrays.stream(possibleRoles).filter(role -> role != UserRole.UNRECOGNIZED).toArray(UserRole[]::new);
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

        // Checks what role the user has. Adds boolean object to the view so that displays can be changed on the frontend.
        List<UserRole> roles = user.getRolesList();
        Boolean userCanEdit = roles.contains(UserRole.TEACHER) || roles.contains(UserRole.COURSE_ADMINISTRATOR);

        model.addAttribute("user", user);
        model.addAttribute("userCanEdit", userCanEdit);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalItems", totalNumUsers);
        model.addAttribute("userList", userResponseList);
        model.addAttribute("footerNumberSequence", footerNumberSequence);
        model.addAttribute("possibleRoles", possibleRoles);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("isAscending", isAscending);
        model.addAttribute("selectedUsersPerPage", usersPerPageLimit.toString());
    }


    /**
     * Deletes a selected user role from a requested user, using a ModifyRoleOfUserRequest to communicate the user ID
     * and role of the user to be changed. Only authenticated users with teacher/course administrator permissions can
     * perform role deletions.
     *
     * @param userId     The user ID of the user being edited.
     * @param roleString The role being deleted from the user, in a string format.
     * @return The success status of the deletion.
     */
    @DeleteMapping("/editUserRole")
    public ResponseEntity<String> deleteUserRole(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "role") String roleString) {
        logger.info("DELETE REQUEST /editUserRole - remove role from user {}", userId);
        ModifyRoleOfUserRequest request = formUserRoleChangeRequest(userId, roleString);
        UserRoleChangeResponse response = userAccountsClientService.removeRoleFromUser(request);
        logger.info("RESOLVED /editUserRole");
        if (response.getIsSuccess()){
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Adds a selected user role to a requested user, using a ModifyRoleOfUserRequest to communicate the user ID
     * and role of the user to be changed. Only authenticated users with teacher/course administrator permissions can
     * perform role additions.
     *
     * @param userId     The user ID of the user being edited.
     * @param roleString The role being added to the user, in a string format.
     * @return The success status of the addition.
     */
    @PutMapping("/editUserRole")
    public ResponseEntity<String> addUserRole(
            @ModelAttribute(value = "userId") String userId,
            @RequestParam(value = "role") String roleString) {
        logger.info("PUT REQUEST /editUserRole - add role to user {}", userId);
        ModifyRoleOfUserRequest request = formUserRoleChangeRequest(userId, roleString);
        UserRoleChangeResponse response = userAccountsClientService.addRoleToUser(request);
        logger.info("RESOLVED /editUserRole");

        if (response.getIsSuccess()){
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Helper method for adding and deleting user roles end points. This method extracts the common request formation
     * as both adding and deleting requests use the same message format.
     *
     *
     * @param userId     - The userId of the user whose roles are being edited
     * @param roleString - A string representation of the role of the user to be added. converted with a dict to a UserRole
     * @return request - the ModifyRoleOfUserRequest that will be sent over grpc
     */
    private ModifyRoleOfUserRequest formUserRoleChangeRequest(String userId, String roleString) {
        return ModifyRoleOfUserRequest.newBuilder()
                .setRole(stringToRole.get(roleString))
                .setUserId(Integer.parseInt(userId))
                .build();
    }


    /**
     * A helper function to get the values of the offset and users per page limit and send a request to the client
     * service, which then gets a response from the server service
     *
     * @return PaginatedUsersResponse, a type that contains all users for a specific page and the total number of users
     */
    private PaginatedUsersResponse getPaginatedUsersFromServer() {
        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                                                                   .setOffset(offset)
                                                                   .setLimit(usersPerPageLimit)
                                                                   .setOrderBy(sortOrder)
                                                                   .setIsAscendingOrder(isAscending)
                                                                   .build();
        GetPaginatedUsersRequest request = GetPaginatedUsersRequest.newBuilder()
                                                                   .setPaginationRequestOptions(options)
                                                                   .build();
        return userAccountsClientService.getPaginatedUsers(request);
    }


    /**
     * To get the list of users for the specific page number
     *
     * @return a list of users
     */
    public List<UserResponse> getUserResponseList() {
        return this.userResponseList;
    }


    /**
     * to get the list of page numbers that is displayed at the bottom of the page for navigation
     *
     * @return an ArrayList of numbers used for the navigation
     */
    public List<Integer> getFooterSequence() {
        return this.footerNumberSequence;
    }


    /**
     * To get the string describing how to sort the data
     *
     * @return a string of how to data is to be sorted
     */
    public String getSortOrder() {
        return this.sortOrder;
    }


    /**
     * To get the boolean describing how if the data should be sorted ascending or descending
     *
     * @return a boolean of if data is ascending or descending
     */
    public boolean getIsAscending() { return this.isAscending; }


    /**
     * To get the number describing how many users should be displayed per page
     *
     * @return and integer of how many users should be displayed per page
     */
    public int getUsersPerPageLimit() { return this.usersPerPageLimit;}


    /**
     * Defines the value roles a user can have, and maps them to their string representation.
     *
     * @return A hashmap which maps string reprs to their UserRole enum element
     */
    private HashMap<String, UserRole> setUserRolesDict() {
        HashMap<String, UserRole> rolesDictionary = new HashMap<>();
        rolesDictionary.put("STUDENT", UserRole.STUDENT);
        rolesDictionary.put("TEACHER", UserRole.TEACHER);
        rolesDictionary.put("COURSE_ADMINISTRATOR", UserRole.COURSE_ADMINISTRATOR);
        rolesDictionary.put("UNRECOGNIZED", UserRole.UNRECOGNIZED);

        return rolesDictionary;
    }


    /**
     * Used to set the UserPrefsRepository used in tests.
     *
     * @param repository - The UserPrefsRepository autowired in the test class.
     */
    public void setPrefRepository(UserPrefRepository repository) {
        this.prefRepository = repository;
    }
}
