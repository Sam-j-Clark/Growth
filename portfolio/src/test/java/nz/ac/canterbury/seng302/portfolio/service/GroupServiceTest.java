package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AddGroupMembersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    private final int TEACHER_GROUP_ID = 1;

    private GroupService groupService;
    private final GroupsClientService groupsClientService = Mockito.mock(GroupsClientService.class);
    private final UserAccountsClientService userAccountsClientService = Mockito.mock(UserAccountsClientService.class);

    @BeforeEach
    void setUp() {
        groupService = new GroupService(groupsClientService, userAccountsClientService);
        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder().setIsSuccess(true).setMessage("Test").build();
        when(groupsClientService.addGroupMembers(any())).thenReturn(response);
    }
    @Test
    void addUserToTeacherGroup() {
        int userId = 0;
        ArrayList<Integer> list = new ArrayList<>();
        list.add(userId);

        groupService.addUsersToGroup(TEACHER_GROUP_ID, list);
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder().setRole(UserRole.TEACHER).setUserId(userId).build();
        verify(userAccountsClientService, times(1)).addRoleToUser(request);

    }

    @Test
    void removeUserFromTeacherGroup() {
        int userId = 0;
        ArrayList<Integer> list = new ArrayList<>();
        list.add(userId);

        groupService.removeUsersFromGroup(TEACHER_GROUP_ID, list);
        ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder().setRole(UserRole.TEACHER).setUserId(userId).build();
        verify(userAccountsClientService, times(1)).removeRoleFromUser(request);

    }
}