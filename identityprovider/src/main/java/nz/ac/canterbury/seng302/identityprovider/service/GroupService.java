package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides utility functions to add and remove users from groups.
 */
@Service
public class GroupService {

    /** The repository containing the groups being managed by the group service. */
    private final GroupRepository groupRepository;

    /** The repository containing the users being managed by the group service. */
    private final UserRepository userRepository;

    /** For logging the requests related to groups. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The Teacher group ID as stored in the Database */
    private static final Integer TEACHERS_GROUP_ID = 1;


    /**
     * The default constructor for the group service.
     *
     * @param groupRepository The repository containing the groups being managed by the group service.
     * @param userRepository The repository containing the users being managed by the group service.
     */
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }


    /**
     * Adds users to a group. If the group is the Members Without A Group group, the users will be removed from every
     * other group. Otherwise, removes the users from Members Without A Group if they are a member
     *
     * @param groupId The id of the group.
     * @param userIds The ids of the users.
     * @throws Exception If the group ID or user IDs are invalid.
     */
    @Transactional
    public void addGroupMembers(Integer groupId, List<Integer> userIds) throws Exception {
        logger.info("Adding users to group {}", groupId);
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            logger.info("Error adding users to group as group id is not valid");
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        try {
            List<User> usersToAdd = (List<User>) userRepository.findAllById(userIds);
            if (group.getLongName().equals("Members Without A Group")) {
                addUsersToMWAG(usersToAdd, group); // Need to remove users from all the other groups in this case
            } else {
                Group mwagGroup = getMWAG();
                for (User user : usersToAdd) {
                    group.addGroupMember(user);
                    if (user.getGroups().contains(mwagGroup)) {
                        removeUserFromMWAG(user, mwagGroup);
                    }
                }
            }
            logger.info("Successfully added users to group {}", groupId);
            groupRepository.save(group);
        } catch (EntityNotFoundException e) {
            logger.info("Error adding users to group as user id is not valid");
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
    }


    /**
     * Removes users from a given group. Checks if the user has been removed from their last group and if so, adds them
     * to Members Without A Group
     *
     * @param groupId The id of the group from which users will be removed.
     * @param userIds The id of the users to be removed.
     */
    @Transactional
    public void removeGroupMembers(Integer groupId, List<Integer> userIds) {
        logger.info("Removing users from group {}", groupId);
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            logger.info("Error removing users from group as group id is invalid");
            throw new IllegalArgumentException(groupId + " does not refer to a valid group");
        }
        Group group = optionalGroup.get();
        if (group.getLongName().equals("Members Without A Group")) {
            logger.info("Error cannot remove users from Members Without A Group");
            throw new IllegalArgumentException("Can't remove user from 'Members Without A Group'");
        }
        try {
            List<User> usersToRemove = (List<User>) userRepository.findAllById(userIds);
            for (User user : usersToRemove) {
                int initialNumGroups = user.getGroups().size();
                boolean removed = group.removeGroupMember(user);
                if (initialNumGroups == 1 && removed) {
                    addUserToMWAG(user);
                }
            }
            logger.info("Successfully removed users from group {}", groupId);
            groupRepository.save(group);
        } catch (EntityNotFoundException e) {
            logger.info("Error removing users from group as user id is invalid");
            throw new IllegalArgumentException(userIds + " does not refer to valid users");
        }
    }

    /**
     * Used to remove a user from a group when we only know the group shortname, useful for automatic removal when a
     * group object is not passed around (eg for auto removal from teacher group when teacher role is removed)
     *
     * @param shortname The shortname of the group the user is being removed from
     * @param userId The id of the user to be removed from the group
     */
    @Transactional
    public void removeGroupMembersByGroupShortName(String shortname, Integer userId){
        logger.info("Retrieving group with shortname {}", shortname);
        Optional<Group> optionalGroup = groupRepository.findByShortName(shortname);
        if (optionalGroup.isPresent()) {
            int groupId = optionalGroup.get().getId();
            ArrayList<Integer> users = new ArrayList<>();
            users.add(userId);
            removeGroupMembers(groupId, users);
        }
    }


    /**
     * Used to add a user to a group when we only know the group shortname, useful for automatically adding a user when
     * we don't pass around a group object (eg for new users automatically being added to the Members Without A Group
     * group
     *
     * @param shortname The shortname of the group the user is being added to
     * @param userId The id of the user being added to the group
     */
    @Transactional
    public void addGroupMemberByGroupShortName(String shortname, Integer userId) throws Exception {
        logger.info("Retrieving group with shortname {}", shortname);
        Optional<Group> optionalGroup = groupRepository.findByShortName(shortname);
        if (optionalGroup.isPresent()) {
            int groupId = optionalGroup.get().getId();
            ArrayList<Integer> users = new ArrayList<>();
            users.add(userId);
            addGroupMembers(groupId, users);
        }
    }


    /**
     * Removes to user from all the groups they are currently a member of except for
     * the teachers group.
     *
     * @param user user to be removed from all groups
     */
    private void removeUserFromAllGroups(User user){
        logger.info("Removing user {} from all groups", user.getId());
        List<Group> usersCurrentGroups = user.getGroups();
        for (Group group: usersCurrentGroups){
            if (!(group.getId().equals(TEACHERS_GROUP_ID) && user.getRoles().contains(UserRole.TEACHER))) {
                group.removeGroupMember(user);
            }
        }
    }


    private void addUserToMWAG(User user) {
        Group mwagGroup = getMWAG();
        mwagGroup.addGroupMember(user);
    }


    /**
     * Adds the users to Members Without A Group, also removes them from every other group,
     * doesn't add teachers to MWAG.
     *
     * @param usersToAdd a list of users the be added to Members Without A Group
     * @param mwaggroup The Members Without A Group group to add the users to
     */
    private void addUsersToMWAG(List<User> usersToAdd, Group mwaggroup) {
        logger.info("Adding users {} to Members Without A Group", usersToAdd);
        for (User user: usersToAdd) {
            removeUserFromAllGroups(user);
            if (!user.getRoles().contains(UserRole.TEACHER)) {
                mwaggroup.addGroupMember(user);
            }
        }
    }


    /**
     * Removes the user from Members Without A Group
     *
     * @param user The user to remove
     * @throws Exception Thrown when there is an error getting Members Without A Group from the repository
     */
    private void removeUserFromMWAG(User user, Group mwagGroup) throws Exception {
        if (mwagGroup == (null)){
            logger.info("Failed to retrieve MWAG");
            throw new Exception("An error occurred getting the MWAG group");
        } else {
            mwagGroup.removeGroupMember(user);
        }
    }


    /**
     * Gets the Member Without A Group from the group repository
     *
     * @return The Member Without A Group group
     */
    public Group getMWAG() {
        logger.info("Retrieving Members Without A Group");
        Optional<Group> group = groupRepository.findByShortName("Non-Group");
        return group.orElse(null);
    }
}