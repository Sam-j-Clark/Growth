package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Implements the server side functionality of the services defined by the groups.proto gRpc contracts.
 */
@GrpcService
public class GroupsServerService extends GroupsServiceGrpc.GroupsServiceImplBase {

    /** For logging the requests related to groups. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The group repository for adding, deleting, updating and retrieving groups. */
    @Autowired
    private GroupRepository groupRepository;

    /** Provides helpful services for adding and removing users from groups. */
    @Autowired
    private GroupService groupService;

    private static final int MAX_SHORT_NAME_LENGTH = 50;
    private static final int MAX_LONG_NAME_LENGTH = 100;
    private static final int MIN_LENGTH = 1;

    /** GroupShortName Comparator */
    Comparator<Group> compareByShortName = Comparator.comparing((Group group) -> group.getShortName().toLowerCase(Locale.ROOT));

    /** GroupLongName Comparator */
    Comparator<Group> compareByLongName = Comparator.comparing((Group group) -> group.getLongName().toLowerCase(Locale.ROOT));

    /** GroupMemberNumber Comparator */
    Comparator<Group> compareByMemberNumber = Comparator.comparing(Group::getMembersNumber);


    /**
     * Follows the gRPC contract and provides the server side service for creating groups.
     *
     * @param request          A CreateGroupRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void createGroup(CreateGroupRequest request, StreamObserver<CreateGroupResponse> responseObserver) {
        logger.info("SERVICE - Creating group {}", request.getShortName());
        CreateGroupResponse.Builder response = CreateGroupResponse.newBuilder().setIsSuccess(true);
        try {
            List<ValidationError> errors = checkValidGroup(request.getShortName(), request.getLongName(), null);
            if (!errors.isEmpty()) {
                errors.forEach(response::addValidationErrors);
                response.setIsSuccess(false).setMessage(errors.get(0).getErrorText());
            }

            if (response.getIsSuccess()) {
                Group group = groupRepository.save(new Group(request.getShortName(), request.getLongName()));
                response.setNewGroupId(group.getId())
                        .setMessage("Group created");
            }
        } catch (Exception exception) {
            logger.error("SERVICE createGroup caught exception");
            logger.error(exception.getMessage());
            response.setIsSuccess(false).setMessage("Unable to create group. Check names and try again.");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for adding members to groups.
     *
     * @param request          An AddGroupMembersRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void addGroupMembers(AddGroupMembersRequest request, StreamObserver<AddGroupMembersResponse> responseObserver) {
        logger.info("SERVICE - Adding users {} to group {}", request.getUserIdsList(), request.getGroupId());
        AddGroupMembersResponse.Builder response = AddGroupMembersResponse.newBuilder().setIsSuccess(true);
        try {
            groupService.addGroupMembers(request.getGroupId(), request.getUserIdsList());
            response.setIsSuccess(true)
                    .setMessage("Successfully added users to group")
                    .build();
        } catch (Exception e) {
            response.setIsSuccess(false)
                    .setMessage(e.getMessage())
                    .build();
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for removing members from groups.
     *
     * @param request          A RemoveGroupMembersRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void removeGroupMembers(RemoveGroupMembersRequest request, StreamObserver<RemoveGroupMembersResponse> responseObserver) {
        logger.info("SERVICE - Removing users {} from group {}", request.getUserIdsList(), request.getGroupId());
        RemoveGroupMembersResponse.Builder response = RemoveGroupMembersResponse.newBuilder().setIsSuccess(true);
        try {
            groupService.removeGroupMembers(request.getGroupId(), request.getUserIdsList());
            response.setIsSuccess(true)
                    .setMessage("Successfully removed users from group")
                    .build();
        } catch (Exception e) {
            response.setIsSuccess(false)
                    .setMessage(e.getMessage())
                    .build();
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for modifying group details.
     * If the group exists and the new names (short and long) don't match existing names, the group
     * is updated and the response isSuccess is true, otherwise it is false.
     *
     * @param request A ModifyGroupDetailsRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void modifyGroupDetails(ModifyGroupDetailsRequest request, StreamObserver<ModifyGroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - modify group details for group with group id {}", request.getGroupId());
        ModifyGroupDetailsResponse.Builder response = ModifyGroupDetailsResponse.newBuilder().setIsSuccess(true);
        try {
            boolean validModification = checkIfValidGroupModification(request, response);

            if (validModification) {
                Group group = groupRepository.findById(request.getGroupId()).get();

                group.setShortName(request.getShortName());
                group.setLongName(request.getLongName());
                groupRepository.save(group);

                response.setIsSuccess(true)
                        .setMessage("Successfully updated details for " + group.getShortName());
                logger.info("Group Modify Success - updated group details for group {}", request.getGroupId());
            }
        } catch (Exception err) {
            logger.error("An error occurred editing modify group request: {} \n See stack trace below \n", request );
            logger.error(err.getMessage());
            response.setIsSuccess(false)
                    .setMessage("Unable to edit the group. Ensure the new names are valid");
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for deleting groups.
     *
     * @param request          A DeleteGroupRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void deleteGroup(DeleteGroupRequest request, StreamObserver<DeleteGroupResponse> responseObserver) {
        logger.info("SERVICE - Deleting group {}", request.getGroupId());
        DeleteGroupResponse.Builder response = DeleteGroupResponse.newBuilder();
        if (groupRepository.existsById(request.getGroupId())) {
            logger.info("SERVICE - Successfully deleted the group with Id: {}", request.getGroupId());
            groupService.removeGroupMembers(request.getGroupId(), groupRepository.getGroupById(request.getGroupId()).getUserList().stream().map(User::getId).toList());
            groupRepository.deleteById(request.getGroupId());
            response.setIsSuccess(true)
                    .setMessage("Successfully deleted the group with Id: " + request.getGroupId());
        } else {
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            response.setIsSuccess(false)
                    .setMessage("No group exists with Id: " + request.getGroupId());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for getting group details.
     *
     * @param request          A GetGroupDetailRequest formatted to satisfy the groups.proto contract.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getGroupDetails(GetGroupDetailsRequest request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting group {}", request.getGroupId());
        GroupDetailsResponse response;
        // Checks that the group exists.
        if (groupRepository.existsById(request.getGroupId())) {
            Group group = groupRepository.getGroupById(request.getGroupId());
            response = group.groupDetailsResponse();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            //If the group doesn't exist
            logger.info("SERVICE - No group exists with Id: {}", request.getGroupId());
            responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
            responseObserver.onCompleted();
        }
    }


    /**
     * Follows the gRPC contract for retrieving the paginated groups. Does this by sorting a list of all the groups based
     * on what was requested and then looping through to add the specific page of groups to the response
     *
     * @param groupsRequest the GetPaginatedGroupsRequest passed through from the client service
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getPaginatedGroups(GetPaginatedGroupsRequest groupsRequest, StreamObserver<PaginatedGroupsResponse> responseObserver) {
        PaginatedGroupsResponse.Builder reply = PaginatedGroupsResponse.newBuilder();
        PaginationRequestOptions request = groupsRequest.getPaginationRequestOptions();

        List<Group> allGroups = (List<Group>) groupRepository.findAll();
        String sortMethod = request.getOrderBy();

        switch (sortMethod) {
            case "longName" -> allGroups.sort(compareByLongName);
            case "membersNumber" -> allGroups.sort(compareByMemberNumber);
            default -> allGroups.sort(compareByShortName); //"shortName" and all other cases
        }

        if (!request.getIsAscendingOrder()){
            Collections.reverse(allGroups);
        }

        for (int i = request.getOffset(); ((i - request.getOffset()) < request.getLimit()) && (i < allGroups.size()); i++) {
            Group group = allGroups.get(i);
            reply.addGroups(group.groupDetailsResponse());
        }
        PaginationResponseOptions options = PaginationResponseOptions.newBuilder()
                                                                     .setResultSetSize(allGroups.size())
                                                                     .build();
        reply.setPaginationResponseOptions(options);
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /**
     * Follows the gRPC contract and provides the server side service for getting the teaching group details.
     *
     * @param request          An empty request.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getTeachingStaffGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting teaching group");
        GroupDetailsResponse response;
        try {
            Optional<Group> group = groupRepository.findByShortName("Teachers");
            if (group.isPresent()) {
                response = group.get().groupDetailsResponse();
            } else {
                response = GroupDetailsResponse.newBuilder().setGroupId(-1).build();
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception err) {
            logger.error("SERVICE - Getting teaching group: {}", err.getMessage());
            responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
            responseObserver.onCompleted();
        }
    }


    /**
     * Follows the gRPC contract and provides the server side service for getting the MWAG group details.
     *
     * @param request          An empty request.
     * @param responseObserver Used to return the response to the client side.
     */
    @Override
    public void getMembersWithoutAGroup(Empty request, StreamObserver<GroupDetailsResponse> responseObserver) {
        logger.info("SERVICE - Getting MWAG group");
        GroupDetailsResponse response;
        try {
            Optional<Group> group = groupRepository.findByShortName("Non-Group");
            if (group.isPresent()) {
                response = group.get().groupDetailsResponse();
            } else {
                response = GroupDetailsResponse.newBuilder().setGroupId(-1).build();
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception err) {
            logger.error("SERVICE - Getting MWAG group: {}", err.getMessage());
            responseObserver.onNext(GroupDetailsResponse.newBuilder().setGroupId(-1).build());
            responseObserver.onCompleted();
        }
    }


    /**
     * Checks if the given modification is valid. If it is not valid, adds validation errors and sets response.isSuccess
     * to false.
     *
     * @param request The requested modification
     * @param response A reference to the response builder. Will be updated depending on status
     * @return A boolean representing whether or not the modification is valid
     */
    private boolean checkIfValidGroupModification(ModifyGroupDetailsRequest request, ModifyGroupDetailsResponse.Builder response) {
        if (groupRepository.findById(request.getGroupId()).isEmpty()) {
            logger.warn("Group Edit Failure - could not find group with id {}", request.getGroupId());
            response.setIsSuccess(false)
                    .setMessage("Could not find group to modify");
        }

        List<ValidationError> errors = checkValidGroup(
                request.getShortName(),
                request.getLongName(),
                request.getGroupId()
        );

        if (!errors.isEmpty()) {
            errors.forEach(response::addValidationErrors);
            response.setIsSuccess(false).setMessage(errors.get(0).getErrorText());
        }
        return response.getIsSuccess();
    }


    /**
     * Checks if the given shortname and longname are the correct lengths and not already in the database, excluding
     * the database entry with the given id. This is useful for modifying a group.
     *
     * @param shortName The shortname to be checked
     * @param longName The longname to be checked
     * @param id The id of the group to ignore when checking
     * @return A list of ValidationErrors, to be added to a response object
     */
    private List<ValidationError> checkValidGroup(String shortName, String longName, Integer id) {

        List<ValidationError> errors = new ArrayList<>();

        errors.addAll(checkLengths(shortName.trim().length(), longName.trim().length()));
        errors.addAll(checkAlreadyExist(shortName, longName, id));


        return errors;
    }


    /**
     * Checks that the two given lengths are in the correct range
     *
     * @param shortNameLength The length of a group's short name
     * @param longNameLength The length of a group's long name
     * @return A list of ValidationErrors, to be added to a response object
     */
    private List<ValidationError> checkLengths(int shortNameLength, int longNameLength) {

        List<ValidationError> errors = new ArrayList<>();

        if (shortNameLength < MIN_LENGTH || shortNameLength > MAX_SHORT_NAME_LENGTH) {
            errors.add(ValidationError.newBuilder()
                    .setFieldName("Short name")
                    .setErrorText("Group short name has to be between " + MIN_LENGTH + " and " +
                            MAX_SHORT_NAME_LENGTH + " characters")
                    .build()
            );
        }
        if (longNameLength < MIN_LENGTH || longNameLength > MAX_LONG_NAME_LENGTH) {
            errors.add(ValidationError.newBuilder()
                    .setFieldName("Long name")
                    .setErrorText("Group long name has to be between " + MIN_LENGTH + " and " +
                            MAX_LONG_NAME_LENGTH + " characters")
                    .build()
            );
        }
        return errors;
    }


    /**
     * Checks if the given shortname and longname are not already in the database, excluding if
     * the database entry with the given id. This is useful for modifying a group.
     *
     * @param shortName The shortname to be checked
     * @param longName The longname to be checked
     * @param id The id of the group to ignore when checking
     * @return A list of ValidationErrors, to be added to a response object
     */
    private List<ValidationError> checkAlreadyExist(String shortName, String longName, Integer id) {
        List<ValidationError> errors = new ArrayList<>();

        Optional<Group> group;
        group = groupRepository.findByShortName(shortName);

        if (group.isPresent() && !group.get().getId().equals(id)) {
            errors.add(ValidationError.newBuilder()
                    .setFieldName("Short name")
                    .setErrorText("A group exists with the short name " + shortName)
                    .build()
            );
        }

        group = groupRepository.findByLongName(longName);

        if (group.isPresent() && !group.get().getId().equals(id)) {
            errors.add(ValidationError.newBuilder()
                    .setFieldName("Long name")
                    .setErrorText("A group already exists with the long name " + longName)
                    .build()
            );
        }
        return errors;
    }
}
