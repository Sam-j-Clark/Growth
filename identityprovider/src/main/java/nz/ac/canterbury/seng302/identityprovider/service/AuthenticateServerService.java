package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.authentication.AuthenticationServerInterceptor;
import nz.ac.canterbury.seng302.identityprovider.authentication.JwtTokenUtil;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc.AuthenticationServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The Server side gRPC service used to authenticate users, both attempting to login and
 * checking the status of a user, ie checking their authentication status.
 * <br>
 * This class was initially
 * provided by the university of canterbury and was then built upon.
 */
@GrpcService
public class AuthenticateServerService extends AuthenticationServiceImplBase{

    private final JwtTokenUtil jwtTokenService = JwtTokenUtil.getInstance();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository repository;

    /**
     * Attempts to authenticate a user with a given username and password.
     *
     * This method attempts to find a user in the repository by the username provided in the request.
     * This user is then passed to the LoginController to check if the request forms a valid login.
     * Depending on the Login status returned from the LoginService, one of 3 helper methods is called to form
     * the request response.
     */
    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
        logger.info("SERVICE - Authenticating user with username: {}", request.getUsername());
        AuthenticateResponse.Builder reply = AuthenticateResponse.newBuilder();

        User foundUser = repository.findByUsername(request.getUsername());
        LoginService service = new LoginService();
        LoginService.LoginStatus status = service.checkLogin(foundUser, request);

        switch (status) {
            case VALID -> setSuccessReply(foundUser, reply);
            case USER_INVALID -> setNoUserReply(request.getUsername(), reply);
            case PASSWORD_INVALID -> setBadPasswordReply(reply);
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }



    /**
     * The AuthenticationInterceptor already handles validating the authState for us, so here we just need to
     * retrieve that from the current context and return it in the gRPC body
     */
    @Override
    public void checkAuthState(Empty request, StreamObserver<AuthState> responseObserver) {
        responseObserver.onNext(AuthenticationServerInterceptor.AUTH_STATE.get());
        responseObserver.onCompleted();
    }


    /**
     * Helper function for the authenticate method. This is called when a user enters the correct
     * login details, to set the reply details to reflect a successful login response
     *
     * @param foundUser - the user being logged in as
     * @param reply - the AuthenticateResponse.Builder to add the correct elements to.
     */
    private void setSuccessReply(User foundUser, AuthenticateResponse.Builder reply) {
        String token = jwtTokenService.generateTokenForUser(
                foundUser.getUsername(),
                foundUser.getId(),
                foundUser.getFirstName() + " " + foundUser.getLastName(),
                foundUser.getRolesCsv()
        );

        reply
                .setEmail(foundUser.getEmail())
                .setFirstName(foundUser.getFirstName())
                .setLastName(foundUser.getLastName())
                .setMessage("Logged in successfully!")
                .setSuccess(true)
                .setToken(token)
                .setUserId(1)
                .setUsername(foundUser.getUsername());
    }


    /**
     * Helper function for the authenticate method. This is called when a user attempt to login
     * using a username that can't be found in the database
     *
     * @param username - the username that was attempted to be login in as
     * @param reply - the AuthenticateResponse.Builder to add the response elements to.
     */
    private void setNoUserReply(String username, AuthenticateResponse.Builder reply) {
        reply
                .setMessage("Log in attempt failed: could not find user " + username)
                .setSuccess(false)
                .setToken("");
    }


    /**
     * Helper function for the authenticate method. This is called when a user attempt to log in
     * to an account but the password is not correct for the user.
     *
     * @param reply - the AuthenticateResponse.Builder to add the response elements to.
     */
    private void setBadPasswordReply(AuthenticateResponse.Builder reply) {
        reply
                .setMessage("Log in attempt failed: username or password incorrect")
                .setSuccess(false)
                .setToken("");
    }


}
