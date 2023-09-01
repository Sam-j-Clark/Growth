package nz.ac.canterbury.seng302.portfolio.controller;

import io.cucumber.gherkin.internal.com.eclipsesource.json.Json;
import io.cucumber.gherkin.internal.com.eclipsesource.json.JsonArray;
import nz.ac.canterbury.seng302.portfolio.PortfolioApplication;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import nz.ac.canterbury.seng302.portfolio.model.dto.GroupResponseDTO;
import nz.ac.canterbury.seng302.portfolio.service.PaginationService;
import nz.ac.canterbury.seng302.portfolio.service.UserService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.GroupService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import nz.ac.canterbury.seng302.shared.util.ValidationError;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PortfolioApplication.class)
@WebMvcTest(controllers = GroupsController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupsControllerTest {

    private Authentication principal;

    private UserResponse idpUser;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    GroupsClientService groupsClientService;

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    GroupService groupService;

    @MockBean
    UserService userService;

    @MockBean
    PaginationService paginationService;

    @MockBean
    private DataInitialisationManagerPortfolio dataInitialisationManagerPortfolio;

    private final ArrayList<GroupDetailsResponse> expectedGroupsList = new ArrayList<>();

    /** used to set the values for the tests, this number should be the same as the value in the GroupController **/
    private Integer groupsPerPage = 10;

    @BeforeEach
    void beforeEach() {
        expectedGroupsList.clear();
        groupsPerPage = 10;
        addExpectedGroupsToList(0, groupsPerPage * 4 + 1);
    }


    @Test
    void testDeleteGroupUnauthorizedToStudent() throws Exception {
        setUserToStudent();

        mockMvc.perform(delete("/groups/edit"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testCreateGroupUnauthorizedToStudent() throws Exception {
        setUserToStudent();

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", "short")
                        .param("longName", "long"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testRecentDemotionCannotCreateGroup() throws Exception {
        setUserToTeacher();
        demoteWithoutUpdatingPrincipal();

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", "short")
                        .param("longName", "long"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRecentDemotionCannotEditGroup() throws Exception {
        setUserToTeacher();
        demoteWithoutUpdatingPrincipal();

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", "short")
                        .param("longName", "long"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testRecentDemotionCannotAddUsers() throws Exception {
        setUserToTeacher();
        demoteWithoutUpdatingPrincipal();
        MockHttpServletRequestBuilder request = buildUserAddRequest();

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    void testRecentDemotionCannotRemoveUsers() throws Exception {
        setUserToTeacher();
        demoteWithoutUpdatingPrincipal();
        MockHttpServletRequestBuilder request = buildUserRemoveRequest();

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }


    @Test
    void testCreateValidShortAndLongName() throws Exception {
        setUserToTeacher();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);
        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                .setIsSuccess(true)
                .setNewGroupId(3)
                .build();
        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isCreated());
    }


    @Test
    void testCreateGroupInvalidShortName() throws Exception {
        setUserToTeacher();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);

        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Short name")
                        .setErrorText("A group exists with the shortName " + request.getShortName())
                        .build())
                .setIsSuccess(false)
                .build();

        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testCreateGroupInvalidLongName() throws Exception {
        setUserToTeacher();
        String shortName = "Test Name";
        String longName = "Test Name But Longer";
        CreateGroupRequest request = buildCreateRequest(shortName, longName);

        CreateGroupResponse response = CreateGroupResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Long name")
                        .setErrorText("A group exists with the longName " + request.getLongName())
                        .build())
                .setIsSuccess(false)
                .build();

        Mockito.when(groupsClientService.createGroup(request)).thenReturn(response);

        mockMvc.perform(post("/groups/edit")
                        .param("shortName", shortName)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testEditLongNameValidLongNameAndGroupId() throws Exception {
        setUserToTeacher();
        String longName = "Test Name But Longer";
        String groupId = "2";
        ModifyGroupDetailsRequest request = buildModifyRequest(Integer.parseInt(groupId),"a short name", longName);
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .setIsSuccess(true)
                .build();
        Mockito.when(groupsClientService.modifyGroupDetails(request)).thenReturn(response);

        GetGroupDetailsRequest groupRequest = GetGroupDetailsRequest.newBuilder()
                                                                    .setGroupId(2)
                                                                    .build();
        GroupDetailsResponse groupResponse = GroupDetailsResponse.newBuilder()
                .addMembers(UserResponse.newBuilder().setId(1).build())
                .setShortName("a short name")
                .build();
        Mockito.when(groupsClientService.getGroupDetails(groupRequest)).thenReturn(groupResponse);

        mockMvc.perform(patch("/groups/edit/longName")
                        .param("groupId", groupId)
                        .param("longName", longName))
                .andExpect(status().isOk());
    }


    @Test
    void testEditLongNameInvalidLongName() throws Exception {
        setUserToStudent();
        String longName = "Test Name But Longer";
        String groupId = "2";
        ModifyGroupDetailsRequest request = buildModifyRequest(Integer.parseInt(groupId),"a short name", longName);
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Long name")
                        .setErrorText("A group exists with the longName " + request.getLongName())
                        .build())
                .setIsSuccess(false)
                .build();
        Mockito.when(groupsClientService.modifyGroupDetails(request)).thenReturn(response);

        GetGroupDetailsRequest groupRequest = GetGroupDetailsRequest.newBuilder()
                .setGroupId(2)
                .build();
        GroupDetailsResponse groupResponse = GroupDetailsResponse.newBuilder()
                .addMembers(UserResponse.newBuilder().setId(1).build())
                .setShortName("a short name")
                .build();
        Mockito.when(groupsClientService.getGroupDetails(groupRequest)).thenReturn(groupResponse);

        mockMvc.perform(patch("/groups/edit/longName")
                        .param("groupId", groupId)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testEditLongNameInvalidGroupId() throws Exception {
        setUserToStudent();
        String longName = "Test Name But Longer";
        String groupId = "9000";
        ModifyGroupDetailsRequest request = buildModifyRequest(Integer.parseInt(groupId),"a short name", longName);
        ModifyGroupDetailsResponse response = ModifyGroupDetailsResponse.newBuilder()
                .addValidationErrors(ValidationError.newBuilder()
                        .setFieldName("Group Id")
                        .setErrorText("No group exists with the group id: " + request.getGroupId())
                        .build())
                .setIsSuccess(false)
                .build();
        Mockito.when(groupsClientService.modifyGroupDetails(request)).thenReturn(response);

        GetGroupDetailsRequest groupRequest = GetGroupDetailsRequest.newBuilder()
                .setGroupId(9000)
                .build();
        GroupDetailsResponse groupResponse = GroupDetailsResponse.newBuilder()
                .addMembers(UserResponse.newBuilder().setId(1).build())
                .setShortName("a short name")
                .build();
        Mockito.when(groupsClientService.getGroupDetails(groupRequest)).thenReturn(groupResponse);

        mockMvc.perform(patch("/groups/edit/longName")
                        .param("groupId", groupId)
                        .param("longName", longName))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testDeleteGroupValid() throws Exception {
        setUserToTeacher();
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder().setGroupId(1).build();

        DeleteGroupResponse response = DeleteGroupResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully deleted the group with Id: " + request.getGroupId())
                .build();

        Mockito.when(groupsClientService.deleteGroup(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/edit")
                        .param("groupId", "1"))
                .andExpect(status().isOk());
    }


    @Test
    void testDeleteGroupInvalid() throws Exception {
        setUserToTeacher();
        DeleteGroupRequest request = DeleteGroupRequest.newBuilder().setGroupId(1).build();

        DeleteGroupResponse response = DeleteGroupResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("No group exists with Id: " + request.getGroupId())
                .build();

        Mockito.when(groupsClientService.deleteGroup(request)).thenReturn(response);

        mockMvc.perform(delete("/groups/edit")
                        .param("groupId", "1"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testAddUsersToGroup() throws Exception {
        setUserToTeacher();

        MockHttpServletRequestBuilder request = buildUserAddRequest();

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }


    @Test
    void testRemoveUsers() throws Exception {
        setUserToTeacher();

        MockHttpServletRequestBuilder request = buildUserRemoveRequest();

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }


    @Test
    void testAddUsersToGroupNotAGroup() throws Exception {
        setUserToTeacher();
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage(groupId + " does not refer to a valid group")
                .build();

        Mockito.when(groupService.addUsersToGroup(Integer.parseInt(groupId), userIds)).thenReturn(response);

        mockMvc.perform(post("/groups/addUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddUserToGroupNotUser() throws Exception {
        setUserToTeacher();
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage("1 does not refer to a valid user")
                .build();

        Mockito.when(groupService.addUsersToGroup(Integer.parseInt(groupId), userIds)).thenReturn(response);

        mockMvc.perform(post("/groups/addUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testDeleteUserNotAGroup() throws Exception {
        setUserToTeacher();
        String groupId = "100";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        RemoveGroupMembersResponse response = RemoveGroupMembersResponse.newBuilder()
                .setIsSuccess(false)
                .setMessage(groupId + " does not refer to a valid group")
                .build();

        Mockito.when(groupService.removeUsersFromGroup(Integer.parseInt(groupId), userIds)).thenReturn(response);

        mockMvc.perform(delete("/groups/removeUsers")
                        .param("groupId", groupId)
                        .params(params))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getGroupsPaginationFirstPage() throws Exception {
        setUserToTeacher();
        setUpContext();
        createMockResponse(0, "10");
        addExpectedGroupsToList(0, 40);
        MvcResult result = mockMvc.perform(get("/getGroups"))
                .andExpect(status().isOk())
                .andReturn();
        String groups = result.getResponse().getContentAsString();
        Assertions.assertTrue(groups.contains("Test Name 0"));
        Assertions.assertTrue(groups.contains("Test Name 9"));
        Assertions.assertFalse(groups.contains("Test Name 10"));
    }

    @Test
    void getGroupPaginationLastPage() throws Exception {
        setUserToTeacher();
        setUpContext();
        createMockResponse(groupsPerPage * 4, "10");
        addExpectedGroupsToList(0, 40);

        MvcResult result = mockMvc.perform((get("/getGroups")).param("page", "4"))
                .andExpect(status().isOk())
                .andReturn();
        String groups = result.getResponse().getContentAsString();
        Assertions.assertTrue(groups.contains("Test Name 40"));
        Assertions.assertFalse(groups.contains("Test Name 39"));
    }

    @Test
    void getGroupPaginationPageNumberLargerThanResults() throws Exception {
        setUserToTeacher();
        setUpContext();
        createMockResponse(groupsPerPage * 4, "10");
        addExpectedGroupsToList(0, 40);
        MvcResult result = mockMvc.perform((get("/getGroups")).param("page", "10"))
                .andExpect(status().isOk())
                .andReturn();
        String groups = result.getResponse().getContentAsString();
        Assertions.assertTrue(groups.contains("Test Name 40"));
        Assertions.assertFalse(groups.contains("Test Name 39"));
    }

    @Test
    void getGroupsPaginationFirstPageWith20Groups() throws Exception {
        setUserToTeacher();
        setUpContext();
        createMockResponse(0, "20");
        addExpectedGroupsToList(0, 40);
        MvcResult result = mockMvc.perform(get("/getGroups").param("groupsPerPage", "20"))
                .andExpect(status().isOk())
                .andReturn();
        String groups = result.getResponse().getContentAsString();
        Assertions.assertTrue(groups.contains("Test Name 0"));
        Assertions.assertTrue(groups.contains("Test Name 9"));
        Assertions.assertTrue(groups.contains("Test Name 10"));
        Assertions.assertFalse(groups.contains("Test Name 20"));
    }





    // ------------------------------------- Helpers -----------------------------------------

    /**
     * Creates a mock response for a specific users per page limit and for an offset. Mocks the server side service of
     * retrieving the users from the repository
     *
     * @param offset the offset of where to start getting users from in the list, used for paging
     */
    private void createMockResponse(int offset, String groupsPerPage) {
        if (groupsPerPage != null){
            switch(groupsPerPage){
                case "10" -> this.groupsPerPage = 10;
                case "20" -> this.groupsPerPage = 20;
                case "40" -> this.groupsPerPage = 40;
                case "60" -> this.groupsPerPage = 60;
                case "all" -> this.groupsPerPage = 999999999;
                default -> this.groupsPerPage = 10;
            }
        }

        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                .setOrderBy("shortName")
                .setOffset(offset)
                .setLimit(this.groupsPerPage)
                .setIsAscendingOrder(true)
                .build();

        GetPaginatedGroupsRequest request = GetPaginatedGroupsRequest.newBuilder()
                .setPaginationRequestOptions(options)
                .build();

        PaginatedGroupsResponse.Builder response = PaginatedGroupsResponse.newBuilder();

        for (int i = offset; ((i - offset) < this.groupsPerPage) && (i < expectedGroupsList.size()); i++) {
            response.addGroups(expectedGroupsList.get(i));
        }

        PaginationResponseOptions responseOptions = PaginationResponseOptions.newBuilder()
                .setResultSetSize(expectedGroupsList.size())
                .build();

        response.setPaginationResponseOptions(responseOptions);

        when(groupService.getPaginatedGroupsFromServer(0, "shortName", this.groupsPerPage, true)).thenReturn(response.build());
        when(groupService.getPaginatedGroupsFromServer(60, "shortName", this.groupsPerPage, true)).thenReturn(response.build());
        when(groupService.getPaginatedGroupsFromServer(30, "shortName", this.groupsPerPage, true)).thenReturn(response.build());
        when(groupService.getPaginatedGroupsFromServer(40, "shortName", this.groupsPerPage, true)).thenReturn(response.build());
        when(groupService.getPaginatedGroupsFromServer(90, "shortName", this.groupsPerPage, true)).thenReturn(response.build());
        when(groupService.createGroupListFromResponse(response.build())).thenReturn(createGroupListFromResponse(response.build()));
        when(groupsClientService.getPaginatedGroups(request)).thenReturn(response.build());
    }

    private void addExpectedGroupsToList(int min, int max) {
    for (int i = min; i < max; i++) {
            String shortName = "Test Name " + i;
            String longName = "Test Name But Longer " + i;
            expectedGroupsList.add(buildCreateResponse(i, shortName, longName));
        }
    }

    public List<GroupResponseDTO> createGroupListFromResponse(PaginatedGroupsResponse paginatedGroupsResponse){
        List<GroupResponseDTO> groupDTOS = new ArrayList<>();
        for(GroupDetailsResponse groupDetailsResponse: paginatedGroupsResponse.getGroupsList()) {
            GroupResponseDTO groupDTO = new GroupResponseDTO(groupDetailsResponse);
            groupDTOS.add(groupDTO);
        }
        return groupDTOS;
    }

    private GroupDetailsResponse buildCreateResponse(int groupId, String shortName, String longName) {
        return GroupDetailsResponse.newBuilder()
                .setGroupId(groupId)
                .setShortName(shortName)
                .setLongName(longName)
                .build();
    }

    private CreateGroupRequest buildCreateRequest(String shortName, String longName) {
        return CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName(longName)
                .build();
    }


    private ModifyGroupDetailsRequest buildModifyRequest(int id, String shortName, String longName) {
        return ModifyGroupDetailsRequest.newBuilder()
                .setGroupId(id)
                .setShortName(shortName)
                .setLongName(longName)
                .build();
    }

    private MockHttpServletRequestBuilder buildUserAddRequest() {
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));

        }

        AddGroupMembersResponse response = AddGroupMembersResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully added users to group")
                .build();

        Mockito.when(groupService.addUsersToGroup(Integer.parseInt(groupId), userIds)).thenReturn(response);

        return post("/groups/addUsers")
                .param("groupId", groupId)
                .params(params);

    }


    private MockHttpServletRequestBuilder buildUserRemoveRequest() {
        String groupId = "3";
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Integer userId : userIds) {
            params.addAll("userIds", Collections.singletonList(userId.toString()));
        }

        RemoveGroupMembersResponse response = RemoveGroupMembersResponse.newBuilder()
                .setIsSuccess(true)
                .setMessage("Successfully removed users from group")
                .build();

        Mockito.when(groupService.removeUsersFromGroup(Integer.parseInt(groupId), userIds)).thenReturn(response);

        return delete("/groups/removeUsers").param("groupId", groupId).params(params);
    }


    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());
        idpUser = UserResponse.newBuilder().setId(1).addRoles(UserRole.STUDENT).build();

        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(idpUser);
        setUpContext();
    }


    private void setUserToTeacher() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("course_administrator").build())
                .build());
        idpUser = UserResponse.newBuilder().setId(1).addRoles(UserRole.COURSE_ADMINISTRATOR).build();

        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(idpUser);
        setUpContext();
    }

    private void demoteWithoutUpdatingPrincipal() {
        idpUser = UserResponse.newBuilder().setId(1).addRoles(UserRole.STUDENT).build();
        Mockito.when(userAccountsClientService.getUserAccountById(any())).thenReturn(idpUser);
    }


    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }
}
