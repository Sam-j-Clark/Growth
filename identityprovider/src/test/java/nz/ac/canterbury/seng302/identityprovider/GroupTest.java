package nz.ac.canterbury.seng302.identityprovider;

import nz.ac.canterbury.seng302.identityprovider.demodata.TestGroupData;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.service.PasswordEncryptionException;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.List;


class GroupTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository repository;

    @InjectMocks
    private TestGroupData testGroupData = Mockito.spy(TestGroupData.class);

    private final List<User> userList = new ArrayList<>();


    @BeforeEach
    void setup() throws PasswordEncryptionException {
        MockitoAnnotations.openMocks(this);

        User test1 = new User(
                "test1",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com"
        );
        User test2 = new User(
                "test2",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com"
        );
        User test3 = new User(
                "test3",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com"
        );
        User test4 = new User(
                "test4",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com"
        );
        User test5 = new User(
                "test5",
                "password",
                "John",
                "McSteves",
                "Wayne",
                "Stev",
                "kdsflkdjf",
                "He/Him",
                "steve@example.com"
        );
        test1.addRole(UserRole.STUDENT);
        test2.addRole(UserRole.STUDENT);
        test3.addRole(UserRole.TEACHER);
        test4.addRole(UserRole.TEACHER);
        test5.addRole(UserRole.TEACHER);


        userList.add(test1);
        userList.add(test2);
        userList.add(test3);
        userList.add(test4);
        userList.add(test5);
    }


    @Test
    void TestAddDefaultGroups() {
        Mockito.when(repository.findAll()).thenReturn(userList);
        Mockito.when(groupRepository.findByShortName("Teachers")).thenReturn(java.util.Optional.of(new Group(0, "Teachers", "Teaching Staff")));
        Mockito.when(groupRepository.findByShortName("Non-Group")).thenReturn(java.util.Optional.of(new Group(1, "Non-Group", "Members Without A Group")));
        ArgumentCaptor<Group> groupArgumentCaptor = ArgumentCaptor.forClass(Group.class);
        testGroupData.addDefaultGroups();
        Mockito.verify(groupRepository, Mockito.atLeast(2)).save(groupArgumentCaptor.capture());
        List<Group> groups = groupArgumentCaptor.getAllValues();
        Group teachingGroup = groups.get(0);
        Group nonMemberGroup = groups.get(1);
        Assertions.assertEquals("Teachers", teachingGroup.getShortName());
        Assertions.assertEquals("Non-Group", nonMemberGroup.getShortName());
    }

}
