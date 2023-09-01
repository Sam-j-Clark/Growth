package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.Project;
import nz.ac.canterbury.seng302.portfolio.model.domain.projects.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.EvidenceResponseDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.UserDTO;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import nz.ac.canterbury.seng302.portfolio.service.EvidenceService;
import nz.ac.canterbury.seng302.portfolio.service.RegexService;
import nz.ac.canterbury.seng302.portfolio.service.SkillFrequencyService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
class EvidenceControllerTest {

    private Authentication principal;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthenticateClientService authenticateClientService;

    @MockBean
    UserAccountsClientService userAccountsClientService;

    @MockBean
    EvidenceRepository evidenceRepository;

    @MockBean
    WebLinkRepository webLinkRepository;

    @MockBean
    ProjectRepository projectRepository;

    @MockBean
    SkillRepository skillRepository;

    @MockBean
    EvidenceService evidenceService;

    @MockBean
    SkillFrequencyService skillFrequencyService;

    @Autowired
    private RegexService regexService;

    private final String WEBLINK_ADDRESS = "https://www.canterbury.ac.nz/";

    private EvidenceDTO evidenceDTO;

    private EvidenceController evidenceController;



    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        initialiseEvidenceDto();
        evidenceController = new EvidenceController(userAccountsClientService,
                projectRepository,
                evidenceRepository,
                evidenceService,
                skillFrequencyService);
    }


    @Test
    void testAddEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test title";
        LocalDate date = LocalDate.now();
        String description = "testing title";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService, skillFrequencyService);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceDateInFuture() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(1).toString();
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Date is in the future"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceOutsideProjectDates() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().minusDays(1).toString();
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Date is outside project dates"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title should be longer than 1 character"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        LocalDate date = LocalDate.now();
        String description = "testing";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService, skillFrequencyService);

        evidenceDTO.setTitle(title);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceTitleLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "This should almost definitely be past 50 characters in length?";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title cannot be more than 50 characters"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceTitleNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "@@@";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Title shouldn't be strange"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionEmpty() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "testing";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Text should be longer than 1 character"));

        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionMixed() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        LocalDate date = LocalDate.now();
        String description = "@#!@#&(*&!@#(&*!@(*&#(*!@&#(&(*&!@(*#&!@#asdasd";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService, skillFrequencyService);

        evidenceDTO.setDescription(description);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceDescriptionLength() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "This should definitely be past 500 characters in length".repeat(10);
        long projectId = 1;
        evidenceDTO.setDescription(description);
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Description cannot be more than 500 characters"));
        mockMvc.perform(post("/evidence")
                        .param("title", title)
                        .param("date", date)
                        .param("description", description)
                        .param("projectId", String.valueOf(projectId)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescriptionNoAlpha() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "Test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "@@@";
        long projectId = 1;
        evidenceDTO.setDescription(description);
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Text shouldn't be strange"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDescription() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "title";
        LocalDate date = LocalDate.now();
        String description = "Description";
        long projectId = 1;
        Project project = new Project("Testing");
        Evidence evidence = new Evidence(1, title, date, description);

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService, skillFrequencyService);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(any(), any())).thenReturn(evidence);

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(200, responseEntity.getStatusCode().value());
    }


    @Test
    void testAddEvidenceProjectId() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        long projectId = 1;

        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new CheckException("Project Id does not match any project"));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "WOW this shouldn't work";
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new DateTimeParseException("test", "test", 0));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceDateNoDate() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = "";
        String description = "testing";
        long projectId = 1;
        Mockito.when(evidenceService.addEvidence(any(), eq(evidenceDTO)))
                .thenThrow(new DateTimeParseException("test", "test", 0));
        mockMvc.perform(post("/evidence")
                .param("title", title)
                .param("date", date)
                .param("description", description)
                .param("projectId", String.valueOf(projectId))).andExpect(status().isBadRequest());
    }


    @Test
    void testAddEvidenceException() throws Exception {
        setUserToStudent();
        setUpContext();
        long projectId = 1;
        Project project = new Project("Testing");

        EvidenceController evidenceController = new EvidenceController(userAccountsClientService, projectRepository, evidenceRepository, evidenceService, skillFrequencyService);

        Mockito.when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        Mockito.when(evidenceService.addEvidence(principal, evidenceDTO)).thenThrow(new RuntimeException());

        ResponseEntity<Object> responseEntity = evidenceController.addEvidence(principal, evidenceDTO);
        Assertions.assertEquals(500, responseEntity.getStatusCode().value());
    }

    @Test
    void testAddEvidenceInvalidAssociateId() throws Exception {
        setUserToStudent();
        setUpContext();
        String title = "test";
        String date = LocalDate.now().plusDays(2).toString();
        String description = "testing";
        List<WebLinkDTO> webLinks = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> associateIds = new ArrayList<>(List.of("5", "My dear friend Joe"));
        long projectId = 1;
        mockMvc.perform(post("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"" + title + "\", \"date\": \"" + date + "\", " +
                                "\"description\": \"" + description + "\", \"webLinks\": " + webLinks + ", " +
                                "\"categories\": " + categories + ", \"skills\": " + skills + ", " +
                                "\"associateIds\": " + associateIds + ", \"projectId\": \"" + projectId + "\"" +
                                "}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    // ------------------------------------ PATCH evidence tests -------------------------------------


    @Test
    void testEditEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        EvidenceDTO testEvidence = createDefaultEvidenceDTO();
        mockMvc.perform(patch("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildEvidenceJSON(testEvidence))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void testEditEvidenceCheckException() throws Exception {

        final String MESSAGE = "Error message";

        setUserToStudent();
        setUpContext();
        EvidenceDTO testEvidence = createDefaultEvidenceDTO();

        when(evidenceService.editEvidence(any(), any())).thenThrow(new CheckException(MESSAGE));

        MvcResult result = mockMvc.perform(patch("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildEvidenceJSON(testEvidence))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        Assertions.assertTrue(result.getResponse().getContentAsString().contains(MESSAGE));
    }


    @Test
    void testEditEvidenceDateTimeParseException() throws Exception {
        setUserToStudent();
        setUpContext();
        EvidenceDTO testEvidence = createDefaultEvidenceDTO();
        testEvidence.setDate("Four score and seven years ago");


        when(evidenceService.editEvidence(any(), any())).thenThrow(new DateTimeParseException("test", "test", 0));

        mockMvc.perform(patch("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildEvidenceJSON(testEvidence))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testEditEvidenceMalformedURLException() throws Exception {
        setUserToStudent();
        setUpContext();
        EvidenceDTO testEvidence = createDefaultEvidenceDTO();

        when(evidenceService.editEvidence(any(), any())).thenThrow(new MalformedURLException());

        mockMvc.perform(patch("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildEvidenceJSON(testEvidence))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditEvidenceGenericError() throws Exception {
        setUserToStudent();
        setUpContext();
        EvidenceDTO testEvidence = createDefaultEvidenceDTO();

        when(evidenceService.editEvidence(any(), any())).thenThrow(new RuntimeException("I am an error"));

        mockMvc.perform(patch("/evidence")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildEvidenceJSON(testEvidence))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // ------------------------------------ GET evidence tests -------------------------------------

    @Test
    void TestGetEvidenceWhenUserExistsAndHasNoEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasOneEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(2, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserExistsAndHasMultipleEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence1 = new Evidence(2, "Title", LocalDate.now(), "description");
        Evidence evidence2 = new Evidence(4, "Title 2", LocalDate.now(), "description 2");
        usersEvidence.add(evidence1);
        usersEvidence.add(evidence2);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andReturn();
        EvidenceResponseDTO expectedResponse1 = new EvidenceResponseDTO(evidence1);
        EvidenceResponseDTO expectedResponse2 = new EvidenceResponseDTO(evidence2);

        String expectedContent = "[" + expectedResponse1.toJsonString() + "," + expectedResponse2.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void TestGetEvidenceWhenUserDoesntExistsReturnsStatusNotFound() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String notExistingUserId = "2";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", notExistingUserId))
                .andExpect(status().isNotFound());
    }


    @Test
    void TestGetEvidenceWhenBadUserIdReturnsStatusBadRequest() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String illegalUserId = "IllegalId";

        mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", illegalUserId))
                .andExpect(status().isBadRequest());
    }


    @Test
    void TestGetEvidenceReturnsBadRequestWhenNoIdIncluded() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();

        mockMvc.perform(get("/evidenceData"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasNoAssociates() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);

        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasOneAssociate() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);
        evidence.addAssociateId(1); // The user themselves should be considered an associate

        GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(1).build();
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
        UserResponse userResponse = userBuilder.build();
        when(userAccountsClientService.getUserAccountById(request)).thenReturn(userResponse);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);


        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();

        UserDTO expectedUser = new UserDTO(userResponse);
        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence, List.of(expectedUser));
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void TestGetEvidenceWhenUserExistsAndHasMultipleAssociates() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";

        ArrayList<Evidence> usersEvidence = new ArrayList<>();
        Evidence evidence = new Evidence(1, 1, "Title", LocalDate.now()
                , "description");
        usersEvidence.add(evidence);
        evidence.addAssociateId(1); // The user themselves should be considered an associate
        evidence.addAssociateId(2);
        evidence.addAssociateId(3);

        List<UserDTO> expectedUsers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            UserResponse.Builder userBuilder = UserResponse.newBuilder().setId(i);
            userBuilder.addRoles(UserRole.STUDENT);
            UserResponse userResponse = userBuilder.build();

            GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(i).build();
            when(userAccountsClientService.getUserAccountById(request)).thenReturn(userResponse);

            UserDTO expectedUser = new UserDTO(userResponse);
            expectedUsers.add(expectedUser);
        }
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(usersEvidence);

        MvcResult result = mockMvc.perform(get("/evidenceData")
                        .queryParam("userId", existingUserId))
                .andExpect(status().isOk())
                .andReturn();


        EvidenceResponseDTO expectedResponse = new EvidenceResponseDTO(evidence, expectedUsers);
        String expectedContent = "[" + expectedResponse.toJsonString() + "]";
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void testDeleteEvidenceValidEvidenceId() throws Exception {
        setUserToStudent();
        setUpContext();

        Integer evidenceId = 1;
        Evidence existingEvidence = new Evidence(evidenceId, 1, "Title", LocalDate.now(), "description");
        Mockito.when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(existingEvidence));

        mockMvc.perform(delete("/evidence")
                        .param("evidenceId", String.valueOf(evidenceId)))
                .andExpect(status().isOk());

        Mockito.verify(evidenceRepository, Mockito.times(1)).delete(existingEvidence);
    }


    @Test
    void testDeleteEvidenceInvalidEvidenceIdForm() throws Exception {
        setUserToStudent();
        setUpContext();

        mockMvc.perform(delete("/evidence")
                        .param("evidenceId", "banana"))
                .andExpect(status().isBadRequest());

        Mockito.verify(evidenceRepository, Mockito.never()).delete(Mockito.any());
    }


    @Test
    void testDeleteEvidenceInvalidEvidenceIdNoSuchEvidence() throws Exception {
        setUserToStudent();
        setUpContext();

        Integer evidenceId = 1;
        Mockito.when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/evidence")
                        .param("evidenceId", String.valueOf(evidenceId)))
                .andExpect(status().isNotFound());

        Mockito.verify(evidenceRepository, Mockito.never()).delete(Mockito.any());
    }


    @Test
    void testDeleteEvidenceUnauthorisedIfUserDoesntOwnEvidence() throws Exception {
        setUserToStudent();
        setUpContext();

        int notCurrentUserId = 2;
        Integer evidenceId = 1;
        Evidence existingEvidence = new Evidence(evidenceId, notCurrentUserId, "Title", LocalDate.now(), "description");
        Mockito.when(evidenceRepository.findById(evidenceId)).thenReturn(Optional.of(existingEvidence));

        mockMvc.perform(delete("/evidence")
                        .param("evidenceId", String.valueOf(evidenceId)))
                .andExpect(status().isUnauthorized());

        Mockito.verify(evidenceRepository, Mockito.never()).delete(Mockito.any());
    }


    // -------------- Helper context functions ----------------------------------------------------


    /**
     * Takes an EvidenceDTO object and returns a JSON string representation of it.
     * Used for making post requests
     *
     * @param evidenceDTO The EvidenceDTO you want to convert into JSON
     *
     * @return a JSON representation of the DTO
     */
    private String buildEvidenceJSON(EvidenceDTO evidenceDTO) {
        return "{ \"title\": \"" + evidenceDTO.getTitle() + "\", \"date\": \"" + evidenceDTO.getDate() + "\", " +
                "\"description\": \"" + evidenceDTO.getDescription() + "\", \"webLinks\": " + evidenceDTO.getWebLinks() + ", " +
                "\"categories\": " + evidenceDTO.getCategories() + ", \"skills\": " + evidenceDTO.getSkills() + ", " +
                "\"associateIds\": " + evidenceDTO.getAssociateIds() + ", \"projectId\": \"" + evidenceDTO.getProjectId() + "\"" +
                "}";
    }


    /**
     * Creates a default evidence DTO
     *
     * @return an EvidenceDTO, with arbitrary (but valid) values and empty lists.
     */
    private EvidenceDTO createDefaultEvidenceDTO() {
        EvidenceDTO.EvidenceDTOBuilder builder = new EvidenceDTO.EvidenceDTOBuilder();
        builder.setId(1)
                .setTitle("Default Evidence")
                .setDate(LocalDate.now().plusDays(2).toString())
                .setDescription("The Default Evidence Description")
                .setWebLinks(new ArrayList<>())
                .setCategories(new ArrayList<>())
                .setSkills(new ArrayList<>())
                .setAssociateIds(new ArrayList<>())
                .setProjectId(1L);
        return builder.build();
    }


    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }


    private void setUserToStudent() {
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
        UserResponse userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }


    private void initialiseGetRequestMocks() {
        GetUserByIdRequest existingUserRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        UserResponse userResponse = UserResponse.newBuilder().setId(1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(existingUserRequest)).thenReturn(userResponse);

        GetUserByIdRequest nonExistentUserRequest = GetUserByIdRequest.newBuilder().setId(2).build();
        UserResponse notFoundResponse = UserResponse.newBuilder().setId(-1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(nonExistentUserRequest)).thenReturn(notFoundResponse);
    }


    private void initialiseEvidenceDto() {
        evidenceDTO = new EvidenceDTO.EvidenceDTOBuilder()
                .setId(10)
                .setTitle("New Title")
                .setDate(LocalDate.now().toString())
                .setDescription("New description")
                .setWebLinks(new ArrayList<>(
                        Arrays.asList(
                                new WebLinkDTO("New weblink 1", "http://www.google.com"),
                                new WebLinkDTO("New weblink 2", "https://localhost:9000/test")
                        )))
                .setCategories(new ArrayList<>(
                        Arrays.asList("SERVICE", "QUANTITATIVE"
                        )))
                .setSkills(new ArrayList<>(
                        Arrays.asList(
                                new Skill("Testing"),
                                new Skill("Backend")
                )))
                .setAssociateIds(new ArrayList<>(
                        Arrays.asList(2, 3, 4, 5)
                ))
                .setProjectId(1L)
                .build();
    }
}
