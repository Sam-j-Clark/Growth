package nz.ac.canterbury.seng302.portfolio.service.grpc;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Used to implement the client side of the services outlined in the authentication.proto contract
 * <br>
 * Mostly these methods just send a request to the server side services to deal with.
 */
@Service
public class AuthenticateClientService {

    @GrpcClient("identity-provider-grpc-server")
    private AuthenticationServiceGrpc.AuthenticationServiceBlockingStub authenticationStub;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Attempts to authenticate a user by sending an AuthenticationRequest to the server side
     *
     * @param username - the username attribute to be passed in the request
     * @param password - the password attribute to be passed in the request
     * @return authenticationResponse - the servers response to the authentication, following the AuthenticationResponse contract
     */
    public AuthenticateResponse authenticate(final String username, final String password) {
        logger.info("SERVICE - send authentication request to server, {}", username);
        AuthenticateRequest authRequest = AuthenticateRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
        return authenticationStub.authenticate(authRequest);
    }

    /**
     * Checks to see if a client is authenticated by passing a request to the server.
     *
     * @return AuthState - information about the authentication status as defined in the authentication.proto contract
     * @throws StatusRuntimeException - if error occurs authenticating
     */
    public AuthState checkAuthState() throws StatusRuntimeException {
        return authenticationStub.checkAuthState(Empty.newBuilder().build());
    }

}
