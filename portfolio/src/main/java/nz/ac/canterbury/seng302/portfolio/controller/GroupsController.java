package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.dto.GroupCreationDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.GroupResponseDTO;
import nz.ac.canterbury.seng302.portfolio.service.GroupService;
import nz.ac.canterbury.seng302.portfolio.service.PaginationService;
import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import nz.ac.canterbury.seng302.portfolio.service.UserService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The controller for managing requests to edit groups and their user's memberships.
 */
@Controller
public class GroupsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GroupsClientService groupsClientService;
    private final GroupService groupService;
    private final UserService userService;
    private final UserAccountsClientService userAccountsClientService;
    private final PaginationService paginationService;

    private static final String SHORTNAME = "shortName";
    private int pageNum = 1;
    private int totalPages = 1;
    private int groupsPerPageLimit = 10;
    private static final Integer TEACHER_GROUP_ID = 1;
    private String orderBy = SHORTNAME;
    private Boolean isAscending = true;
    private ArrayList<Integer> footerNumberSequence = new ArrayList<>();


    /**
     * Autowired constructor
     * 
     * @param groupsClientService The service class for GRPC stuff for Groups.
     * @param groupService The service class for related methods for Groups.
     * @param userService The service class for related methods for Users.
     * @param userAccountsClientService The user account service.
     * @param paginationService The pagination service class.
     */
    @Autowired
    GroupsController(GroupsClientService groupsClientService,
                     GroupService groupService,
                     UserService userService,
                     UserAccountsClientService userAccountsClientService,
                     PaginationService paginationService){
        this.groupsClientService = groupsClientService;
        this.groupService = groupService;
        this.userService = userService;
        this.userAccountsClientService = userAccountsClientService;
        this.paginationService = paginationService;
    }


    /**
     * This endpoint retrieves all groups as a paginated list.
     *
     * @param principal The authentication principal.
     * @return The ModalAndView that contains the groups.
     */
    @GetMapping("/groups")
    public ModelAndView groups(
            @AuthenticationPrincipal Authentication principal)
    {
        logger.info("GET REQUEST /groups - attempt to get all groups and return modelAndView");
        UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
        ModelAndView modelAndView = new ModelAndView("groups");
        userService.checkAndAddUserRole(user, modelAndView);
        try {
            footerNumberSequence = paginationService.createFooterNumberSequence(footerNumberSequence, totalPages, pageNum);
            modelAndView.addObject("user", user);
            modelAndView.addObject("footerNumberSequence", footerNumberSequence);
            modelAndView.addObject("selectedGroupsPerPage", this.groupsPerPageLimit);
            modelAndView.addObject("gitlabToken", RegexPattern.GITLAB_TOKEN);

        } catch (Exception e) {
            logger.error("ERROR /groups - an error occurred while retrieving groups and modelAndView");
            logger.error(e.getMessage());
            return new ModelAndView("errorPage").addObject(e.getMessage(), e);
        }

        return modelAndView;
    }


    /**
     * This endpoint retrieves groups depending on the inputs. It returns them as a responseEntity.
     * @param page The page number the user is on in the groups list
     * @param groupsPerPage The number of groups to display per page
     * @param sortBy Which way to sort the groups by
     * @return returns a ResponseEntity with the groups contained.
     */
    @GetMapping("/getGroups")
    public ResponseEntity<Object> getGroups(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "groupsPerPage", required = false) String groupsPerPage,
            @RequestParam(name = "sortBy", required = false) String sortBy)
     {
         logger.info("GET REQUEST /getGroups - attempt to get all groups");
         try {
             if (page != null) {
                 pageNum = page;
             }
             boolean goToLastPage = (pageNum == -1); // If page is a -1 then the user wants to go to the last page
             if (pageNum <= 1) { //to ensure no negative page numbers
                 pageNum = 1;
             }

             // check for new values
             if (groupsPerPage != null){
                 switch(groupsPerPage){
                     case "20" -> this.groupsPerPageLimit = 20;
                     case "40" -> this.groupsPerPageLimit = 40;
                     case "60" -> this.groupsPerPageLimit = 60;
                     case "all" -> this.groupsPerPageLimit = 999999999;
                     default -> this.groupsPerPageLimit = 10;
                 }
             }
             if (sortBy != null){
                 switch (sortBy) {
                     case "Short Name Desc" -> {
                         this.orderBy = SHORTNAME;
                         this.isAscending = false;
                     }
                     case "Long Name Asc" -> {
                         this.orderBy = "longName";
                         this.isAscending = true;
                     }
                     case "Long Name Desc" -> {
                         this.orderBy = "longName";
                         this.isAscending = false;
                     }
                     default -> {
                         this.orderBy = SHORTNAME;
                         this.isAscending = true;
                     }
                 }
             }
             int offset = (pageNum - 1) * groupsPerPageLimit; // The number to start retrieving groups from
             PaginatedGroupsResponse response = groupService.getPaginatedGroupsFromServer(offset, orderBy, groupsPerPageLimit, isAscending);
             int totalNumGroups = response.getPaginationResponseOptions().getResultSetSize();
             totalPages = totalNumGroups / groupsPerPageLimit;
             if ((totalNumGroups % groupsPerPageLimit) != 0) {
                 totalPages++; // Checks if there are leftover groups to display
             }
             if (pageNum > totalPages || goToLastPage) { //to ensure that the last page will be shown if the page number is too large
                 pageNum = totalPages;
                 offset = (pageNum - 1) * groupsPerPageLimit;
                 response = groupService.getPaginatedGroupsFromServer(offset, orderBy, groupsPerPageLimit, isAscending);
             }
             footerNumberSequence = paginationService.createFooterNumberSequence(footerNumberSequence, totalPages, pageNum);

             HashMap<String, Object> returnMap = new HashMap<>();
             returnMap.put("groups", groupService.createGroupListFromResponse(response));
             returnMap.put("footerNumberSequence", footerNumberSequence);
             returnMap.put("groupsPerPage", this.groupsPerPageLimit);
             returnMap.put("page", pageNum);

             return new ResponseEntity<>(returnMap, HttpStatus.OK);
         } catch (Exception e) {
             logger.error("ERROR /getGroups - an error occurred while retrieving groups");
             logger.error(e.getMessage());
             return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
         }

    }


    /**
     * Gets an individual group by the group Id.
     *
     * @param groupId - The id group whose information is being retrieved
     * @return a Response entity containing the HTTPStatus and the groups information.
     */
    @GetMapping("/group")
    public ResponseEntity<Object> getGroup(@RequestParam Integer groupId) {
        logger.info("GET REQUEST /group - attempt to get group {}", groupId);
        try {
            GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            GroupDetailsResponse response = groupsClientService.getGroupDetails(request);
            return new ResponseEntity<>(new GroupResponseDTO(response), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("ERROR /group - an error occurred while retrieving group {}", groupId);
            logger.error(exception.getMessage());
            return new ResponseEntity<>("An error occurred while retrieving the group", HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * The get request to get the create group html
     *
     * @param principal - The user who made the request
     * @return ModelAndView - the model and view of the group creation page
     */
    @GetMapping("/groupsCreate")
    public ModelAndView getCreatePage(@AuthenticationPrincipal Authentication principal) {
        try {
            logger.info("GET REQUEST /groups/create - get group creation page");
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);

            ModelAndView model = new ModelAndView("groupCreation");
            model.addObject("user", user);
            return model;
        } catch (Exception err) {
            logger.error("GET /groups/create: {}", err.getMessage());
            return new ModelAndView("error");
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint deletes an existing group.
     *
     * @param principal The user who made the request.
     * @param groupId   The group ID of the group to be deleted.
     * @return ResponseEntity A response entity containing either OK or NOT FOUND (for now).
     */
    @DeleteMapping("/groups/edit")
    public ResponseEntity<String> deleteGroup(@AuthenticationPrincipal Authentication principal,
                                              @RequestParam Integer groupId) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("DELETE REQUEST /groups - attempt to delete group {} by user: {}", groupId, userId);
        try {
            DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();
            DeleteGroupResponse response = groupsClientService.deleteGroup(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while deleting a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred while deleting the group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, This endpoint creates a new group.
     *
     * @param principal - The user who made the request
     * @param createInfo the group request that contains the short and long name
     * @return ResponseEntity - a response entity containing either CREATED or BAD_REQUEST (for now)
     */
    @PostMapping("/groups/edit")
    public ResponseEntity<String> createGroup(@AuthenticationPrincipal Authentication principal,
                                              @ModelAttribute(name="editDetailsForm") GroupCreationDTO createInfo) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit - attempt to create group {} by user: {}", createInfo.getShortName(), userId);
        try {
            CreateGroupRequest request = CreateGroupRequest.newBuilder()
                    .setShortName(createInfo.getShortName())
                    .setLongName(createInfo.getLongName())
                    .build();
            CreateGroupResponse response = groupsClientService.createGroup(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit - an error occurred while creating a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("Unable to create group " + createInfo.getShortName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Restricted to teachers and course administrators, this endpoint modifies a group details.
     *
     * @param principal The user who made the request.
     * @param groupId The id of the group to be modified.
     * @param shortName The new short name of the group.
     * @param longName  The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    @PostMapping("/groups/edit/details")
    public ResponseEntity<String> modifyGroupDetails(@AuthenticationPrincipal Authentication principal,
                                                     @RequestParam Integer groupId,
                                                     @RequestParam String shortName,
                                                     @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("POST REQUEST /groups/edit/details - attempt to modify details of group {} by user: {}", groupId, userId);
        return groupEdit(groupId, shortName, longName);
    }


    /**
     * Endpoint for students to edit their own groups longname.
     * Students have access for this endpoint, but can only modify the longname of a group they are in.
     *
     * @param principal The user who made the request.
     * @param groupId The id of the group to be modified.
     * @param longName  The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    @PatchMapping("/groups/edit/longName")
    public ResponseEntity<String> modifyGroupLongName(@AuthenticationPrincipal Authentication principal,
                                                     @RequestParam Integer groupId,
                                                     @RequestParam String longName) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal.getAuthState());
        logger.info("PATCH REQUEST /groups/edit/longName - attempt to modify details of group {} by user: {}", groupId, userId);
        // Firstly, we have to find the shortname of the group
        GetGroupDetailsRequest request = GetGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        GroupDetailsResponse response = groupsClientService.getGroupDetails(request);

        // Checks if the user trying to edit is a member of the group being edited
        if (!response.getMembersList().stream().map(UserResponse::getId).toList().contains(userId)){
            return new ResponseEntity<>("Only members of this group can edit the name", HttpStatus.UNAUTHORIZED);
        }

        return groupEdit(groupId, response.getShortName(), longName);
    }


    /**
     * An extracted helper method that makes a request to the identity provider
     * to modify a group's details.
     *
     * @param groupId The id of the group to be modified
     * @param shortName The new short name of the group. Use "" to leave it unmodified.
     * @param longName The new long name of the group.
     * @return A response entity containing either OK, BAD_REQUEST, or INTERNAL_SERVER_ERROR.
     */
    private ResponseEntity<String> groupEdit(Integer groupId, String shortName, String longName) {
        try {
            ModifyGroupDetailsRequest request = ModifyGroupDetailsRequest.newBuilder()
                    .setGroupId(groupId)
                    .setShortName(shortName)
                    .setLongName(longName)
                    .build();
            ModifyGroupDetailsResponse response = groupsClientService.modifyGroupDetails(request);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/edit/ - an error occurred while modifying a group's details");
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred when editing the group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Post mapping for a user to be added to a group. Restricted to course administrators and teachers.
     *
     * @param userIds The users to be added to the group.
     * @param groupId The group to which the use will be added.
     * @return A response entity containing the status of the response and the response message.
     */
    @PostMapping("/groups/addUsers")
    public ResponseEntity<String> addUsersToGroup(
            @RequestParam Integer groupId,
            @RequestParam List<Integer> userIds
    ) {
        logger.info("POST REQUEST /groups/addUsers");

        try {
            AddGroupMembersResponse response = groupService.addUsersToGroup(groupId, userIds);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/addUsers - an error occurred while adding a user to a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred while adding a user to the group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Post mapping for users to be removed from a group. Restricted to course administrators and teachers.
     *
     * @param userIds The users to be removed from the group.
     * @param groupId The group to which the use will be removed.
     * @return A response entity containing the status of the response and the response message.
     */
    @DeleteMapping("/groups/removeUsers")
    public ResponseEntity<String> removeUsersFromGroup(
            @AuthenticationPrincipal Authentication principal,
            @RequestParam(value = "groupId") Integer groupId,
            @RequestParam(value = "userIds") List<Integer> userIds
    ) {
        logger.info("DELETE REQUEST /groups/removeUsers");

        try {
            if (Objects.equals(groupId, TEACHER_GROUP_ID)) {
                logger.info("Removing users from teacher group, checking user is admin");
                UserResponse userResponse = PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService);
                if (!userResponse.getRolesList().contains(UserRole.COURSE_ADMINISTRATOR)) {
                    return new ResponseEntity<>("You must be a course administrator to do this.", HttpStatus.UNAUTHORIZED);
                }

            }
            RemoveGroupMembersResponse response = groupService.removeUsersFromGroup(groupId, userIds);
            if (response.getIsSuccess()) {
                return new ResponseEntity<>(response.getMessage(), HttpStatus.OK);
            }
            return new ResponseEntity<>(response.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("ERROR /groups/removeUsers - an error occurred while removing a user from a group");
            logger.error(e.getMessage());
            return new ResponseEntity<>("An error occurred while removing a user from the group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
