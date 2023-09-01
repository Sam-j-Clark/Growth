package nz.ac.canterbury.seng302.portfolio.service.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GroupsClientService {

    /**
     * For logging the grpc requests related to groups
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * The gRpc stub to make calls to the server service
     */
    @GrpcClient("identity-provider-grpc-server")
    private GroupsServiceGrpc.GroupsServiceBlockingStub groupsStub;

    /**
     * The grpc service to request the deletion of a group from the IdP
     * <br>
     *
     * @param request - The request to delete a group following the DeleteGroupRequest message format
     * @return response - The IdP's response following the DeleteGroupResponse message format
     */
    public DeleteGroupResponse deleteGroup(DeleteGroupRequest request) {
        logger.info("SERVICE - send deleteGroupRequest request to server");
        return groupsStub.deleteGroup(request);
    }


    /**
     * The grpc service to request the creation of a group on the IdP
     * <br>
     *
     * @param request - The request to create a group following the CreateGroupRequest message format
     * @return response - The IdP's response following the CreateGroupResponse message format
     */
    public CreateGroupResponse createGroup(CreateGroupRequest request) {
        logger.info("SERVICE - send createGroupRequest request to server");
        return groupsStub.createGroup(request);
    }


    /**
     * The grpc service to request the adding of users to a group on the Idp
     *
     * @param request The request to add users to a group, following the AddGroupMembersRequest message format
     * @return The IdP's response following the AddGroupMembersResponse message format
     */
    public AddGroupMembersResponse addGroupMembers(AddGroupMembersRequest request) {
        logger.info("SERVICE - send addGroupMembersRequest request to server");
        return groupsStub.addGroupMembers(request);
    }


    /**
     * The grpc service to request the removal of users from a group on the Idp
     *
     * @param request The request to remove users from a group, following the RemoveGroupMembersRequest message format
     * @return The IdP's response following the RemoveGroupMembersResponse message format
     */
    public RemoveGroupMembersResponse removeGroupMembers(RemoveGroupMembersRequest request) {
        logger.info("SERVICE - send deleteGroupMembersRequest request to server");
        return groupsStub.removeGroupMembers(request);
    }

    /**
     * The grpc service to request modify a group details on the Idp
     *
     * @param request The request to modify a group details, following the ModifyGroupDetailsRequest message format
     * @return The IdP's response following the ModifyGroupDetailsResponse message format
     */
    public ModifyGroupDetailsResponse modifyGroupDetails(ModifyGroupDetailsRequest request) {
        logger.info("SERVICE - send modifyGroupDetailsRequest request to server");
        return groupsStub.modifyGroupDetails(request);
    }


    /**
     * Sends a request to the GroupsServerService to get a specific group by their group ID
     *
     * @param request the GetGroupDetailsRequest passed through from the controller, with the groupId
     * @return response - a GroupDetailsResponse, a response with the given groups details
     */
    public GroupDetailsResponse getGroupDetails(GetGroupDetailsRequest request) {
        logger.info("SERVICE - send getGroupDetailsRequest request to server");
        return groupsStub.getGroupDetails(request);
    }


    /**
     * Sends a request to the GroupsServerService to get a specific page for the groups list, through a
     * GetPaginatedGroupsRequest
     *
     * @param request the GetPaginatedGroupsRequest passed through from the controller, with the page, size of the list
     *                and the sort order
     * @return response - a GetPaginatedGroupsResponse, a response with a list of groups and the total amount of groups
     */
    public PaginatedGroupsResponse getPaginatedGroups(GetPaginatedGroupsRequest request) {
        logger.info("SERVICE - send getPaginatedGroupsRequest request to server");
        return groupsStub.getPaginatedGroups(request);
    }
}
