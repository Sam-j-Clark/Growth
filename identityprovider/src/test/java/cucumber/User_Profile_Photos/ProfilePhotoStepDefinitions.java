package cucumber.User_Profile_Photos;

import com.google.protobuf.ByteString;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.stub.StreamObserver;
import nz.ac.canterbury.seng302.identityprovider.controller.ImageController;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.ImageRequestStreamObserver;
import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptionException;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProfilePhotoStepDefinitions {

    private final String TRAIN_IMG_LENGTH = "17350";
    private final String DEFAULT_IMG_LENGTH = "2431";

    private final MockImageResponseStreamObserver mockImageResponseStreamObserver = new MockImageResponseStreamObserver();

    @MockBean
    UserRepository repository = mock(UserRepository.class);

    private Environment mockEnv;
    private User user;
    private MockMvc mockMvc;
    private ResultActions result;

    @Before
    public void setup() {


        mockEnv = mock(Environment.class);
        when(mockEnv.getProperty(eq("photoLocation"), any(String.class)))
                .thenReturn("src/main/resources/profile-photos/");

        when(mockEnv.getProperty("protocol", "http")).thenReturn("http");
        when(mockEnv.getProperty("hostName", "localhost")).thenReturn("localhost");
        when(mockEnv.getProperty("port", "9001")).thenReturn("9001");
        when(mockEnv.getProperty("rootPath", "")).thenReturn("");

        ImageController controller = new ImageController(mockEnv);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Given("I am logged in as user id {int}")
    public void i_am_logged_in_as_user_id(int userId) throws PasswordEncryptionException {
        user = new User(
                "test",
                "password",
                "FirstName",
                "MiddleName",
                "LastName",
                "Nick",
                "This is a bio",
                "He/Him",
                "test@example.com"
        );
        user.setId(userId);
        when(repository.findById(userId)).thenReturn(user);
    }


    @Given("I have no profile photo")
    public void i_have_no_profile_photo() {
        try {
            user.deleteProfileImage(mockEnv);
        } catch (IOException exception) {
            // The user already does not have a profile photo
        }
    }


    @When("I request my profile photo Image")
    public void i_request_my_profile_photo_image() throws Exception {
        result = mockMvc.perform(get("/profile/%s.jpg".formatted(user.getId())))
                .andExpect(status().isOk());
    }


    @Then("I receive the default profile photo icon")
    public void i_receive_the_default_profile_photo_icon() throws Exception {
        result.andExpect(header().string("content-length", DEFAULT_IMG_LENGTH));
    }


    @When("I change my profile photo")
    public void i_change_my_profile_photo() {
        try {
            URL resource = ProfilePhotoStepDefinitions.class.getResource("/testProfileImage.jpg");
            assert resource != null;
            String imagePath = Paths.get(resource.toURI()).toFile().getAbsolutePath();

            uploadTestProfileImage(imagePath);
        } catch (MalformedURLException exception) {
            fail("Invalid URL setting");
        } catch (IOException exception) {
            fail("Failed to upload the new image");
        } catch (URISyntaxException exception) {
            fail("Failed to Convert image path");
        } catch (NullPointerException exception) {
            fail("Failed to find test image resource");
        }
    }


    @Then("I receive the profile photo for id {int}")
    public void i_receive_the_profile_photo_for_id(int userId) throws Exception {
        result.andExpect(header().string("content-length", TRAIN_IMG_LENGTH));
    }


    private void uploadTestProfileImage(String newImage) throws IOException {
        when(repository.save(user)).thenReturn(null);

        ArrayList<UploadUserProfilePhotoRequest> requestChunks = new ArrayList<>();

        ProfilePhotoUploadMetadata metadata = ProfilePhotoUploadMetadata.newBuilder()
                .setUserId(user.getId())
                .setFileType("jpg")
                .build();

        requestChunks.add(UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(metadata)
                .build()
        );

        InputStream photo = new BufferedInputStream(new FileInputStream(newImage));

        byte[] bytes = new byte[4096];
        int size;
        while ((size = photo.read(bytes)) > 0) {
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0, size))
                    .build();
            requestChunks.add(uploadRequest);
        }
        photo.close();

        StreamObserver<UploadUserProfilePhotoRequest> requestObserver = new ImageRequestStreamObserver(
                mockImageResponseStreamObserver, repository, mockEnv);
        mockImageResponseStreamObserver.initialise(requestObserver);
        mockImageResponseStreamObserver.sendImage(requestChunks);
    }
}
