package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


class GroupServiceTest {

    private final GroupRepository groupRepository = Mockito.mock(GroupRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private GroupService groupService;

    @BeforeEach
    public void setUp() {
        groupService = new GroupService(groupRepository, userRepository);
    }


    @Test
    void testAddUser() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);


        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getUserList().size());
        assertEquals(userList, group.getUserList());
    }


    @Test
    void testAddAlreadyPresentUser() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);

        Group group = new Group(1, "Short", "Long");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(2, group.getMembersNumber());
        assertEquals(userList, group.getUserList());
    }


    @Test
    void testDeleteUser() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        User user2 = new User("Steve2", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        userList.add(user2);

        Group group = new Group(1, "Short", "Long");
        Group MwagGroup = new Group(2, "Non-Group", "Members Without A Group");
        ReflectionTestUtils.setField(group, "userList", new ArrayList<>());
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findByShortName(MwagGroup.getShortName())).thenReturn(Optional.of(MwagGroup));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);

        groupService.addGroupMembers(group.getId(), userIds);
        groupService.removeGroupMembers(group.getId(), userIds);

        assertEquals(0, group.getMembersNumber());

    }


    @Test
    void testRemoveUserFromLastGroup() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);

        Group group = new Group(1, "Short", "Long");
        group.addGroupMember(user);
        Group MwagGroup = new Group(2, "Non-Group", "Members Without A Group");
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findByShortName(MwagGroup.getShortName())).thenReturn(Optional.of(MwagGroup));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);

        groupService.removeGroupMembers(group.getId(), userIds);

        assertEquals(0, group.getMembersNumber());
    }


    @Test
    void testAddUserToNewGroupRemovedFromMwag() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);

        Group group = new Group(1, "Short", "Long");
        Group MwagGroup = new Group(2, "Non-Group", "Members Without A Group");
        MwagGroup.addGroupMember(user);
        user.getGroups().add(MwagGroup);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findByShortName(MwagGroup.getShortName())).thenReturn(Optional.of(MwagGroup));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);

        groupService.addGroupMembers(group.getId(), userIds);

        assertEquals(0, MwagGroup.getMembersNumber());
        assertEquals(userList, group.getUserList());
    }

    @Test
    void testUserAddedToMwagRemovedFromOtherGroups() throws Exception {
        User user = new User("Steve1", "password", "Steve", "Stevenson", "McSteve", "KingSteve", "", "", "Steve@steve.com");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        ArrayList<Integer> userIds = new ArrayList<>();
        userIds.add(1);

        Group group = new Group(1, "Short", "Long");
        Group MwagGroup = new Group(2, "Non-Group", "Members Without A Group");
        user.getGroups().add(group);
        group.addGroupMember(user);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findById(MwagGroup.getId())).thenReturn(Optional.of(MwagGroup));
        when(groupRepository.findByShortName(MwagGroup.getShortName())).thenReturn(Optional.of(MwagGroup));
        when(userRepository.findAllById(Mockito.any())).thenReturn(userList);

        groupService.addGroupMembers(MwagGroup.getId(), userIds);

        assertEquals(0, group.getMembersNumber());
        assertEquals(userList, MwagGroup.getUserList());
    }
}