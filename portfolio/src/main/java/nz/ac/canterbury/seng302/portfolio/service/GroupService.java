package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.dto.GroupResponseDTO;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for more complex actions involving groups, abstracted to make it more testable
 */
@Service
public class GroupService {

    /** The ID of the default teacher group */
    private static final int TEACHER_GROUP_ID = 1;

    /** Used to add / remove users from groups */
    private final GroupsClientService groupsClientService;

    /** Used to add and remove roles when users move groups */
    private final UserAccountsClientService userAccountsClientService;

    @Autowired
    public GroupService(GroupsClientService groupsClientService, UserAccountsClientService userAccountsClientService) {
        this.groupsClientService = groupsClientService;
        this.userAccountsClientService = userAccountsClientService;
    }


    /**
     * Add users to the given group, assigning the teacher role as needed
     *
     * @param groupId The group to add the users to
     * @param userIds The users to add to the group
     * @return A response message as defined in the protobuf
     */
    public AddGroupMembersResponse addUsersToGroup(int groupId, List<Integer> userIds) {
        if (groupId == TEACHER_GROUP_ID) {
            for (Integer userId: userIds) {
                ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                        .setRole(UserRole.TEACHER)
                        .setUserId(userId)
                        .build();
                userAccountsClientService.addRoleToUser(request);
            }
        }
        AddGroupMembersRequest request = AddGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsClientService.addGroupMembers(request);
    }


    /**
     * Removes users from the given group, assigning roles as needed
     * @param groupId The group to remove the users from
     * @param userIds The users to remove from the group
     * @return A response message as defined in the protobuf
     */
    public RemoveGroupMembersResponse removeUsersFromGroup(int groupId, List<Integer> userIds) {
        if (groupId == TEACHER_GROUP_ID) {
            for (Integer userId: userIds) {
                ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                        .setRole(UserRole.TEACHER)
                        .setUserId(userId)
                        .build();
                userAccountsClientService.removeRoleFromUser(request);
            }
        }
        RemoveGroupMembersRequest request = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsClientService.removeGroupMembers(request);
    }


    /**
     * Makes a request to the server for the groups
     * @param offset The offset for the groups
     * @param orderBy The order to get the groups
     * @param groupsPerPageLimit The number of groups to get
     * @param isAscending Ascending or descending
     * @return A group response from the server.
     */
    public PaginatedGroupsResponse getPaginatedGroupsFromServer(int offset, String orderBy, int groupsPerPageLimit, boolean isAscending) {
        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                .setOffset(offset)
                .setOrderBy(orderBy)
                .setLimit(groupsPerPageLimit)
                .setIsAscendingOrder(isAscending)
                .build();
        GetPaginatedGroupsRequest request = GetPaginatedGroupsRequest.newBuilder()
                .setPaginationRequestOptions(options)
                .build();
        return groupsClientService.getPaginatedGroups(request);
    }


    /**
     * Creates a list of Group objects from a paginated group response.
     *
     * @param paginatedGroupsResponse The paginated group response to create the list from.
     * @return A list of group objects.
     */
    public List<GroupResponseDTO> createGroupListFromResponse(PaginatedGroupsResponse paginatedGroupsResponse){
        List<GroupResponseDTO> groupDTOS = new ArrayList<>();
        for(GroupDetailsResponse groupDetailsResponse: paginatedGroupsResponse.getGroupsList()) {
            GroupResponseDTO groupDTO = new GroupResponseDTO(groupDetailsResponse);
            groupDTOS.add(groupDTO);
        }
        return groupDTOS;
    }
}
