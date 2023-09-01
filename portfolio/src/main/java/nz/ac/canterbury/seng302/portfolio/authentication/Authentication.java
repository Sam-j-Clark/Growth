package nz.ac.canterbury.seng302.portfolio.authentication;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import java.security.Principal;

/**
 * This class serves as a wrapper around the AuthState object used as an authentication token. As it implements the
 * interface Principal, it works better with external tools (such as stompjs).
 */
public class Authentication implements Principal {

    /**
     * The internal AuthState object
     */
    private final AuthState authState;

    /**
     * Construct a new Authentication object
     *
     * @param authState The AuthState to wrap
     */
    public Authentication(AuthState authState) {
        this.authState = authState;
    }

    /**
     * This needs to be implemented to meet the contract of Principal. Sent as a field by
     * the STOMP broker when it responds to a connection request.
     *
     * @return The users name as a String
     */
    @Override
    public String getName() {
        return authState.getName();
    }

    public AuthState getAuthState() {
        return authState;
    }
}
