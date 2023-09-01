package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.PortfolioApplication;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.demodata.DataInitialisationManagerPortfolio;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.repositories.GitRepository;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PortfolioApplication.class)
@WebMvcTest(controllers = GitRepoController.class)
@AutoConfigureMockMvc(addFilters = false)
class GitRepoControllerTest {

    private Authentication principal;
    private GroupDetailsResponse response;
    private UserResponse userResponse;

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    GroupsClientService groupsClientService;

    @MockBean
    GitRepoRepository gitRepoRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataInitialisationManagerPortfolio dataInitialisationManagerPortfolio;


    private final String VALID_ACCESS_TOKEN = "MysE3EYxRooxpDijMpHW";

    @Test
    void testAddGitRepoValidWithExistingRepo() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        GitRepository repo = new GitRepository(1, 1, "test", "token");

        ArrayList<GitRepository> repos = new ArrayList<>();
        repos.add(repo);
        Mockito.when(gitRepoRepository.findAllByGroupId(1)).thenReturn(repos);

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isOk());

        ArgumentCaptor<GitRepository> argument = ArgumentCaptor.forClass(GitRepository.class);

        Mockito.verify(gitRepoRepository, Mockito.times(1)).save(argument.capture());
        assertEquals("repo alias", argument.getValue().getAlias());
    }


    @Test
    void testAddGitRepoValidWithoutExistingRepo() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        Mockito.when(gitRepoRepository.findAllByGroupId(1)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isOk());

        ArgumentCaptor<GitRepository> argument = ArgumentCaptor.forClass(GitRepository.class);

        Mockito.verify(gitRepoRepository, Mockito.times(1)).save(argument.capture());
        assertEquals("repo alias", argument.getValue().getAlias());
    }


    @Test
    void testAddGitRepoGroupDoesntExist() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "2")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testAddGitRepoValidTeacher() throws Exception {
        setUserRoleToTeacher();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isOk());
    }


    @Test
    void testAddGitRepoValidAdmin() throws Exception {
        setUserRoleToAdmin();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isOk());
    }


    @Test
    void testAddGitRepoInvalidUser() throws Exception {
        setUserRoleToStudent();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testAddGitRepoInvalidGroupId() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "invalid")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidProjectId() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "invalid")
                        .param("alias", "repo alias")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAlias() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "")
                        .param("accessToken", VALID_ACCESS_TOKEN))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoNoAccessToken() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", ""))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAccessTokenCharacters() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(post("/editGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "tooShort"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testRetrieveGitRepoInvalidGroupId() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(get("/getRepo")
                        .param("groupId", "involid group id"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testRetrieveGitRepo() throws Exception {
        setUserRoleToStudent();
        setUserToGroupMember();
        setupContext();

        mockMvc.perform(get("/getRepo")
                        .param("groupId", "1"))
                .andExpect(status().isOk());
    }


    @Test
    void testRetrieveGitRepoWithoutGroupId() throws Exception {
        setUserRoleToStudent();
        setupContext();

        mockMvc.perform(get("/getRepo"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testRetrieveGitRepoStudentNotInGroup() throws Exception {
        setUserRoleToStudent();
        setUserToNotGroupMember();
        setupContext();

        mockMvc.perform(get("/getRepo")
                        .param("groupId", "1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testRetrieveGitRepoGroupDoesntExistOnIDP() throws Exception {
        setUserRoleToAdmin();
        setUserToGroupMember();
        setupContext();

        // This is the response sent when a group cant be found.
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().setGroupId(-1).build();
        GitRepository repository = new GitRepository(1, 1, "Test alias", "xxx");

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);
        Mockito.when(gitRepoRepository.findAllByGroupId(1)).thenReturn(List.of(repository));
        mockMvc.perform(get("/getRepo")
                        .param("groupId", "1"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------


    private void setUserToGroupMember() {
        GroupDetailsResponse groupExistsResponse = GroupDetailsResponse.newBuilder()
                .addMembers(userResponse).build();

        GroupDetailsResponse groupDoesntExistResponse = GroupDetailsResponse.newBuilder().setGroupId(-1).build();

        when(groupsClientService.getGroupDetails(GetGroupDetailsRequest.newBuilder()
                .setGroupId(1).build())).thenReturn(groupExistsResponse);
        when(groupsClientService.getGroupDetails(GetGroupDetailsRequest.newBuilder()
                .setGroupId(2).build())).thenReturn(groupDoesntExistResponse);
    }


    private void setUserToNotGroupMember() {
        UserResponse emptyUserResponse = UserResponse.newBuilder().build();

        response = GroupDetailsResponse.newBuilder()
                .addMembers(emptyUserResponse).build();

        GroupDetailsResponse groupDoesntExistResponse = GroupDetailsResponse.newBuilder().setGroupId(-1).build();

        when(groupsClientService.getGroupDetails(GetGroupDetailsRequest.newBuilder()
                .setGroupId(1).build())).thenReturn(response);
        when(groupsClientService.getGroupDetails(GetGroupDetailsRequest.newBuilder()
                .setGroupId(2).build())).thenReturn(groupDoesntExistResponse);
    }


    private void setUserRoleToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
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
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setUserRoleToTeacher() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("teacher").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.TEACHER);
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setUserRoleToAdmin() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("course_administrator").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.COURSE_ADMINISTRATOR);
        userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
    }


    private void setupContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }


}
