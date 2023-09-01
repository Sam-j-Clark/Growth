package nz.ac.canterbury.seng302.identityprovider.service;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.util.BasicStringFilteringOptions;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;

/**
 * The UserAccountsServerService implements the server side functionality of the defined by the
 * user_accounts.proto rpc contracts.
 */
@GrpcService
public class UserAccountsServerService extends UserAccountServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository where Users details are stored. */
    private final UserRepository userRepository;

    /** Provides access to environment variables. */
    private final Environment env;

    /** To edit group details related to users roles. */
    private final GroupService groupService;


    // Repeat messages
    private static final String UNEXPECTED_ERROR_MESSAGE = "An Unexpected error occurred";
    private static final String UNFOUND_USER_ERROR_MESSAGE = "Could not find user";


    /** First Name Comparator, has other name fields after to decide order if first names are the same*/
    Comparator<User> compareByFirstName = Comparator.comparing((User user) ->
            (user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
             user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
             user.getLastName().toLowerCase(Locale.ROOT)));

    /** Middle Name Comparator, has other name fields after to decide order if middle names are the same */
    Comparator<User> compareByMiddleName = Comparator.comparing((User user) ->
            (user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
             user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
             user.getLastName().toLowerCase(Locale.ROOT)));

    /** Last Name Comparator, has other name fields after to decide order if last names are the same */
    Comparator<User> compareByLastName = Comparator.comparing((User user) ->
            (user.getLastName().toLowerCase(Locale.ROOT) + ' ' +
             user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
             user.getMiddleName().toLowerCase(Locale.ROOT)));

    /** Username Comparator */
    Comparator<User> compareByUsername = Comparator.comparing((User user) ->
            (user.getUsername().toLowerCase(Locale.ROOT)));

    /** Alias Comparator, has name fields afterwards to decide order if the aliases are the same */
    Comparator<User> compareByAlias = Comparator.comparing((User user) ->
            (user.getNickname().toLowerCase(Locale.ROOT) + ' ' +
             user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
             user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
             user.getLastName().toLowerCase(Locale.ROOT)));

    /** Role Comparator */
    Comparator<User> compareByRole = (userOne, userTwo) -> {
        List<UserRole> userOneRoles = userOne.getRoles();
        List<UserRole> userTwoRoles = userTwo.getRoles();
        Integer userOnePrecedence = (userOneRoles.contains(UserRole.STUDENT) ? 1 : 0) +
                                    (userOneRoles.contains(UserRole.TEACHER) ? 2 : 0) +
                                    (userOneRoles.contains(UserRole.COURSE_ADMINISTRATOR) ? 4 : 0);
        Integer userTwoPrecedence = (userTwoRoles.contains(UserRole.STUDENT) ? 1 : 0) +
                                    (userTwoRoles.contains(UserRole.TEACHER) ? 2 : 0) +
                                    (userTwoRoles.contains(UserRole.COURSE_ADMINISTRATOR) ? 4 : 0);

        userOneRoles.sort(Collections.reverseOrder());
        userTwoRoles.sort(Collections.reverseOrder());

        return userTwoPrecedence.compareTo(userOnePrecedence);
    };


    /**
     * Autowired constructor to inject the required beans.
     *
     * @param userRepository - The repo that stores the users
     * @param env  - Gives access to the environment variables
     * @param groupService - For CRUD actions to do with groups.
     */
    @Autowired
    public UserAccountsServerService(UserRepository userRepository, Environment env, GroupService groupService) {
       this.userRepository = userRepository;
       this.env = env;
       this.groupService = groupService;
    }


    /**
     * getUserAccountByID follows the gRPC contract and provides the server side service for retrieving
     * user account details from the repository of users.
     *
     * @param request - The GetUserByIDRequest formatted to satisfy the user_accounts.proto gRPC
     * @param responseObserver - used to return the response to the Client side of the service
     */
    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        logger.info("SERVICE - Getting user details by Id: {}", request.getId());
        User user = userRepository.findById(request.getId());
        UserResponse reply;
        if (user == null) {
            logger.warn("Could not find user with id {}, -1 responded", request.getId());
            reply = UserResponse.newBuilder().setId(-1).build();
        } else {
            logger.info("Sending user details for {}", user.getUsername());
            reply = user.userResponse();
        }
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for registering new users, adding them to the database
     *
     * @param request - A UserRegisterRequest formatted to satisfy the user_accounts.proto contract
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
        logger.info("SERVICE - Registering new user with username {}", request.getUsername());
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();

        try {
            User user = new User(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getMiddleName(),
                    request.getLastName(),
                    request.getNickname(),
                    request.getBio(),
                    request.getPersonalPronouns(),
                    request.getEmail()
            );

            if (userRepository.findByUsername(user.getUsername()) == null) {
                logger.info("Registration Success - for new user {}", request.getUsername());
                userRepository.save(user);
                groupService.addGroupMemberByGroupShortName("Non-Group",user.getId());
                reply.setIsSuccess(true)
                        .setNewUserId(user.getId())
                        .setMessage("Account has successfully been registered");
            } else {
                    logger.info("Registration Failure - username {} already in use", request.getUsername());
                    reply.setIsSuccess(false);
                    reply.setMessage("Username already in use");
            }

        } catch (io.grpc.StatusRuntimeException e) {
            reply.setIsSuccess(false);
            reply.setMessage("An error occurred registering user from request");
            logger.error("An error occurred registering user from request: {}\n see stack trace below \n", request);
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.info("An unexpected error occurred when trying to add the new user:\n {}", e.getMessage());
            reply.setIsSuccess(false)
                    .setMessage(UNEXPECTED_ERROR_MESSAGE);
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to edit the details of a user.
     *
     * This service first attempts to find the user by their id so that they can have their details edited
     *  - If the user can't be found a response message is set to send a failure message to the client
     *  - Otherwise the users details are updated as according to the request.
     *
     * @param request - The gRPC EditUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Transactional
    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        logger.info("SERVICE - Editing details for user with id {}", request.getUserId());
        EditUserResponse.Builder response = EditUserResponse.newBuilder();
        // Try to find user by ID
        User userToEdit = userRepository.findById(request.getUserId());

        if (userToEdit != null) {
            try {
                logger.info("User Edit Success - updated user details for user {}", request.getUserId());
                userToEdit.setFirstName(request.getFirstName());
                userToEdit.setMiddleName(request.getMiddleName());
                userToEdit.setLastName(request.getLastName());
                userToEdit.setNickname(request.getNickname());
                userToEdit.setBio(request.getBio());
                userToEdit.setPronouns(request.getPersonalPronouns());
                userToEdit.setEmail(request.getEmail());
                userRepository.save(userToEdit);
                response.setIsSuccess(true)
                        .setMessage("Successfully updated details for " + userToEdit.getUsername());
            } catch (StatusRuntimeException e) {
                logger.error("An error occurred editing user from request: {}\n See stack trace below \n", request);
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("Incorrect current password provided");
            }
        } else {
            logger.info("User Edit Failure - could not find user with id {}", request.getUserId());
            response.setIsSuccess(false)
                    .setMessage("Could not find user to edit");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to change the password of a User
     *
     * This service first attempts to find the user by their id so that they can have their password changed
     *  - If the user can't be found a response message is set to send a failure message to the client
     *  - Otherwise the oldPassword is checked against the database to make sure the user knows their old password
     *  before changing
     *    - If this password is correct the password is updated to the new password, otherwise the user is informed
     *    that they have used an incorrect old password.
     *
     * @param request - The gRPC ChangePasswordRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Transactional
    @Override
    public void changeUserPassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        logger.info("SERVICE - Changing password for user with id {}", request.getUserId());
        ChangePasswordResponse.Builder response = ChangePasswordResponse.newBuilder();

        User userToUpdate = userRepository.findById(request.getUserId());
        if (userToUpdate != null) {
            // User is found, check correct current password provided
            try {
                // encrypt attempted current password to "match" pwhash
                LoginService encryptor = new LoginService();
                String inputPWHash = encryptor.getHash(request.getCurrentPassword(), userToUpdate.getSalt());
                // Check encrypted password against pw hash
                if (userToUpdate.getPwhash().equals(inputPWHash)) {
                    // If password hash matches, update
                    logger.info("Password Change Success - password updated for user {}", request.getUserId());
                    userToUpdate.setPwhash(request.getNewPassword());
                    userRepository.save(userToUpdate);
                    response.setIsSuccess(true)
                            .setMessage("Successfully updated details for " + userToUpdate.getUsername());
                } else {
                    logger.info("Password Change Failure - incorrect old password for {}", request.getUserId());
                    // Password hash doesn't match so don't update
                    response.setIsSuccess(false)
                            .setMessage("Incorrect current password provided");
                }
            } catch (StatusRuntimeException e) {
                logger.error("An error occurred changing user password from request: {}\n See stack trace below \n", request);
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("An error has occurred while connecting to the database");
            } catch (PasswordEncryptionException e) {
                logger.error("An error occurred encrypting the new password");
                logger.error(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage("An error has occurred while encrypting the new password");
            }
        } else {
            logger.info("Password Change Failure - could not find user with id {}", request.getUserId());
            response.setIsSuccess(false)
                    .setMessage(UNFOUND_USER_ERROR_MESSAGE);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * The gRPC implementation of bidirectional streaming used to receive uploaded user profile images.
     * 
     * The server creates a stream observer and defines its actions when the client calls the OnNext, onError and
     * onComplete methods.
     *
     * @param responseObserver - Contains an observer, which the Client side defines the implementation for. This allows
     *                           client side actions to be called from the server side. E.g., if bytes have been
     *                           received from the client successfully, the server will call
     *                           responseObserver.onNext(FileUploadStatusResponse) to inform the client to send more.
     * @return requestObserver - Contains an observer defined by the server, so that the client can call server side
     *                           actions. Therefore, this method defines the servers actions when the client calls them.
     */
    @Override
    public StreamObserver<UploadUserProfilePhotoRequest> uploadUserProfilePhoto(StreamObserver<FileUploadStatusResponse> responseObserver) {
        return new ImageRequestStreamObserver(responseObserver, userRepository, env);
    }


    /**
     * Follows the gRPC contract for deleting a users profile photo.
     *
     * @param request The request with the users id to delete the profile photo from
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void deleteUserProfilePhoto(DeleteUserProfilePhotoRequest request, StreamObserver<DeleteUserProfilePhotoResponse> responseObserver) {
        DeleteUserProfilePhotoResponse.Builder response = DeleteUserProfilePhotoResponse.newBuilder();
        try {
            int id = request.getUserId();
            User user = userRepository.findById(id);
            user.deleteProfileImage(env);
            response.setIsSuccess(true);
        } catch (Exception exception) {
            response.setIsSuccess(false);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to add a role to a User.
     *
     * This service first attempts to find the user by their id so that they can have their role changed <br>
     *  - If the user can't be found a response message is set to send a failure message to the client <br>
     *  - Otherwise the role to be added is checked against the user's current roles to prevent duplication, then the
     *  role is added if it's unique for the user.
     *
     * @param request - The gRPC ModifyRoleOfUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void addRoleToUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("Service - Adding role {} to user {}", request.getRole(), request.getUserId() );
        UserRoleChangeResponse.Builder response = UserRoleChangeResponse.newBuilder();

        User userToUpdate = userRepository.findById(request.getUserId());
        if (userToUpdate != null) {
            try {
                if (!userToUpdate.getRoles().contains(request.getRole())) {
                    userToUpdate.addRole(request.getRole());
                    userRepository.save(userToUpdate);
                    if (request.getRole() == UserRole.TEACHER) {
                        groupService.addGroupMemberByGroupShortName("Teachers", userToUpdate.getId());
                    }
                    response.setIsSuccess(true)
                            .setMessage(MessageFormat.format("Successfully added role {0} to user {1}",
                                    request.getRole(), userToUpdate.getId()));
                } else {
                    response.setIsSuccess(false)
                            .setMessage("User already has that role");
                }
            } catch (Exception e){
                logger.info("An unexpected error occurred when trying to add a role to user:\n{}", e.getMessage());
                response.setIsSuccess(false)
                        .setMessage(UNEXPECTED_ERROR_MESSAGE);
            }
        } else {
            response.setIsSuccess(false)
                    .setMessage(UNFOUND_USER_ERROR_MESSAGE);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for editing users, this method attempts to remove a role from a User.
     *
     * This service first attempts to find the user by their id so that they can have their role changed
     *  - If the user can't be found a response message is set to send a failure message to the client
     *
     *  - Otherwise the role to be removed is checked against the user's current roles to prevent deleting a role
     *  that doesn't exist.
     *
     *  - Finally, we attempt to delete the role. If the user has 1 - or somehow no roles (which should not happen) -
     *  then an exception gets thrown, because a user should always have at least 1 role. We catch this exception
     *  and send a failure message.
     *
     * @param request - The gRPC ModifyRoleOfUserRequest passed from the client
     * @param responseObserver - Used to return the response to the client side.
     */
    @Override
    public void removeRoleFromUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("Service - Removing role {} from user {}", request.getRole(), request.getUserId());
        UserRoleChangeResponse.Builder response = UserRoleChangeResponse.newBuilder();

        User userToUpdate = userRepository.findById(request.getUserId());
        if (userToUpdate != null) {
            //We've found the user!
            try {
                userToUpdate.deleteRole(request.getRole());
                userRepository.save(userToUpdate);
                logger.info("Role Removal Success - removed {} from user {}", request.getRole(), request.getUserId());
                if (request.getRole().equals(UserRole.TEACHER)){
                    groupService.removeGroupMembersByGroupShortName("Teachers", userToUpdate.getId());
                }
                response.setIsSuccess(true)
                        .setMessage(MessageFormat.format("Successfully removed role {0} from user {1}",
                                request.getRole(), userToUpdate.getId()));

            } catch (IllegalStateException e) {
                //The user has only one role - we can't delete it!
                logger.info("Role Removal Failure - user {} has 1 role. Users cannot have 0 roles", request.getUserId());
                response.setIsSuccess(false)
                        .setMessage("The user can't have zero roles");
            } catch (Exception e) {
                logger.info(e.getMessage());
                response.setIsSuccess(false)
                        .setMessage(UNEXPECTED_ERROR_MESSAGE);
            }
        } else {
            //Here, we couldn't find the user, so we do not succeed.
            logger.info("Role Removal Failure - could not find user {}", request.getUserId());
            response.setIsSuccess(false)
                    .setMessage(UNFOUND_USER_ERROR_MESSAGE);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for retrieving the paginated users. Does this by sorting a list of all the users based
     * on what was requested and then looping through to add the specific page of users to the response
     *
     * @param usersRequest the GetPaginatedUsersRequest passed through from the client service
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getPaginatedUsers(GetPaginatedUsersRequest usersRequest, StreamObserver<PaginatedUsersResponse> responseObserver) {
        PaginatedUsersResponse.Builder response = getPaginatedUsersHelper(usersRequest.getPaginationRequestOptions());
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract for retrieving the paginated users and filtering them. Does this by calling a helper
     * function that gets the paginated users and then filters them by their last and first name
     *
     * @param usersRequest the GetPaginatedUsersFilteredRequest passed through from the client service
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getPaginatedUsersFilteredByName(GetPaginatedUsersFilteredRequest usersRequest, StreamObserver<PaginatedUsersResponse> responseObserver){
        PaginatedUsersResponse.Builder response = getPaginatedUsersHelper(usersRequest.getPaginationRequestOptions());

        BasicStringFilteringOptions filteringOptions = usersRequest.getFilteringOptions();
        // Filtered by first, last and then full name so that the order for the auto-complete is more natural
        Predicate<UserResponse> firstName = user -> (user.getFirstName().toLowerCase(Locale.ROOT))
                .contains(filteringOptions.getFilterText().toLowerCase(Locale.ROOT));
        Predicate<UserResponse> lastName = user -> (user.getLastName().toLowerCase(Locale.ROOT))
                .contains(filteringOptions.getFilterText().toLowerCase(Locale.ROOT));
        Predicate<UserResponse> fullName = user -> (user.getFirstName().toLowerCase(Locale.ROOT) + " " +
                user.getLastName().toLowerCase(Locale.ROOT))
                .contains((filteringOptions.getFilterText().toLowerCase(Locale.ROOT)));

        ArrayList<UserResponse> filteredUsers = new ArrayList<>();
        filteredUsers.addAll(response.getUsersList().stream().filter(firstName).toList());
        filteredUsers.addAll(response.getUsersList().stream().filter(lastName).toList());
        filteredUsers.addAll(response.getUsersList().stream().filter(fullName).toList());

        response.clearUsers();
        response.addAllUsers(new ArrayList<>(new LinkedHashSet<>(filteredUsers))); // to remove duplicates

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * A helper function to get all the users, sort them, and select the ones needed for the requested page
     *
     * @param request the PaginationRequestOptions passed through from the client service
     * @return a builder for the PaginatedUsersResponse populated with the paginated users
     */
    private PaginatedUsersResponse.Builder getPaginatedUsersHelper(PaginationRequestOptions request){
        PaginatedUsersResponse.Builder response = PaginatedUsersResponse.newBuilder();
        List<User> allUsers = (List<User>) userRepository.findAll();
        String sortMethod = request.getOrderBy();

        switch (sortMethod) {
            case "roles" -> allUsers.sort(compareByRole);
            case "username" -> allUsers.sort(compareByUsername);
            case "aliases" -> allUsers.sort(compareByAlias);
            case "middlename" -> allUsers.sort(compareByMiddleName);
            case "lastname" -> allUsers.sort(compareByLastName);
            default -> allUsers.sort(compareByFirstName);
        }

        if (!request.getIsAscendingOrder()){
            Collections.reverse(allUsers);
        }

        //for each user up to the limit or until all the users have been looped through, add to the response
        for (int i = request.getOffset(); ((i - request.getOffset()) < request.getLimit()) && (i < allUsers.size()); i++) {
            response.addUsers(allUsers.get(i).userResponse());
        }
        PaginationResponseOptions options = PaginationResponseOptions.newBuilder()
                .setResultSetSize(allUsers.size())
                .build();
        response.setPaginationResponseOptions(options);
        return response;
    }
}
