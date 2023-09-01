package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class creates a few simple methods for extracting claims from AuthState gRPC messages.
 *
 * Primarily used to extract the userId from an AuthState object
 */
public class PrincipalAttributes {

    private static final Logger logger = LoggerFactory.getLogger(PrincipalAttributes.class);


    /**
     * Makes the constructor invisible so only static methods are reachable
     */
    private PrincipalAttributes() {}


    /**
     * Used to get the claims attributes of a AuthState gRPC message. Note the primary use of this is getting userID
     * with PrincipalAttributes.getClaim(principal, "nameid"). This can be called directly with
     * {@link PrincipalAttributes#getIdFromPrincipal(AuthState) getUserFromPrincipal}
     *
     * @param principal - the Authstate principal the claim is to be extracted from.
     * @param claimType -  the Claims claimType attribute value
     * @return claimValue - a string value of the claim requested.
     */
    public static String getClaim(AuthState principal, String claimType) {
        logger.info("Getting {} from principal", claimType);
        return principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals(claimType))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
    }


    /**
     * Specific use of the getClaim method for returning a userId from their AuthState.
     *
     * @param principal - The AuthState gRPC message.
     * @return userId (int) - a user Id of the principal.
     */
    public static int getIdFromPrincipal(AuthState principal) {
        return Integer.parseInt(PrincipalAttributes.getClaim(principal, "nameid"));
    }


    /**
     * Specific use of the getClaim method for returning a UserResponse from their AuthState.
     *
     * @param principal                 - The AuthState gRPC message.
     * @param userAccountsClientService - requires passing as the classes calling this method have it Autowired
     * @return userId (UserResponse) - a User response object containing user details.
     */
    public static UserResponse getUserFromPrincipal(AuthState principal, UserAccountsClientService userAccountsClientService) {
        // Get user from server
        int userId = getIdFromPrincipal(principal);
        GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder().setId(userId).build();
        return userAccountsClientService.getUserAccountById(userRequest);
    }

}
