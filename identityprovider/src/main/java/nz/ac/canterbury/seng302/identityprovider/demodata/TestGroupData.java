package nz.ac.canterbury.seng302.identityprovider.demodata;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.model.UserRepository;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import nz.ac.canterbury.seng302.identityprovider.model.GroupRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TestGroupData {

    /** The repository containing all users */
    @Autowired
    UserRepository userRepository;

    /** The repository containing all groups */
    @Autowired
    GroupRepository groupRepository;

    /** to log the adding of test data */
    Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Creates the two default groups, members without groups and teaching staff.
     * Saves both the groups to the repository.
     */
    public void addDefaultGroups() {
        logger.info("Creating default groups");
        Group teachingGroup = new Group( "Teachers", "Teaching Staff");
        Group nonGroupGroup = new Group( "Non-Group", "Members Without A Group");
        groupRepository.save(teachingGroup);
        groupRepository.save(nonGroupGroup);
        logger.info("Finished creating default groups");
    }


    /**
     * Creates six test groups, and saves them to the repository
     */
    public void addTestGroups() {
        logger.info("Creating test groups");
        Group groupOne = new Group("Team 100", "Seng 302 Team 100");
        Group groupTwo = new Group("Team 200", "Seng 302 Team 200");
        Group groupThree = new Group("Team 300", "Seng 302 Team 300");
        Group groupFour = new Group("Team 400", "Seng 302 Team 400");
        Group groupFive = new Group("Team 500", "Seng 302 Team 500");
        Group groupSix = new Group("Team 600", "The Best SENG 302 Team");
        for (int i = 0; i < 20; i++) {
          Group group = new Group("Team " + i, "Seng 302 Team " + i);
          groupRepository.save(group);
        }

        groupRepository.save(groupOne);
        groupRepository.save(groupTwo);
        groupRepository.save(groupThree);
        groupRepository.save(groupFour);
        groupRepository.save(groupFive);
        groupRepository.save(groupSix);
        logger.info("Finished creating test groups");
    }


    /**
     * Adds 3 users to test group 3, and 2 users to test group 4.
     */
    public void addUsersToTestGroups() {
        ArrayList<User> groupThreeMembers = new ArrayList<>();
        groupThreeMembers.add(userRepository.findByUsername("steve"));
        groupThreeMembers.add(userRepository.findByUsername("Robert.abe1989"));
        groupThreeMembers.add(userRepository.findByUsername("finagle"));

        ArrayList<User> groupFourMembers = new ArrayList<>();
        groupFourMembers.add(userRepository.findByUsername("Walter.harber"));
        groupFourMembers.add(userRepository.findByUsername("RonnieNick"));

        Optional<Group> optionalGroup3 = groupRepository.findByShortName("Team 100");
        if (optionalGroup3.isPresent()){
            Group group3 = optionalGroup3.get();
            for (User member: groupThreeMembers){
                group3.addGroupMember(member);
            }
            groupRepository.save(group3);
        }

        Optional<Group> optionalGroup4 = groupRepository.findByShortName("Team 200");
        if (optionalGroup4.isPresent()){
            Group group4 = optionalGroup4.get();
            for (User member: groupFourMembers){
                group4.addGroupMember(member);
            }
            groupRepository.save(group4);
        }
    }


    /**
     * Loops through a list that contains every user and filters them into either nonGroupUsers or Teachers.
     */
    public void setInitialTeachersAndMWAGGroupMembers() {
        logger.info("Adding Teacher and Members without a group to default groups");

        List<User> everyUserList = (List<User>) userRepository.findAll();
        List<User> teachers = new ArrayList<>();
        List<User> nonGroupUsers = new ArrayList<>();
        for (User user: everyUserList) {
            if (user.getRoles().contains(UserRole.TEACHER)) {
                teachers.add(user);
            } else {
                if (user.getGroups().isEmpty()) {
                    nonGroupUsers.add(user);
                }
            }
        }

        Optional<Group> optionalTeachersGroup = groupRepository.findByShortName("Teachers");
        if (optionalTeachersGroup.isPresent()){
            Group teachingGroup = optionalTeachersGroup.get();
            for (User member: teachers){
                teachingGroup.addGroupMember(member);
            }
            groupRepository.save(teachingGroup);
        }

        Optional<Group> optionalNonGroup = groupRepository.findByShortName("Non-Group");
        if (optionalNonGroup.isPresent()){
            Group nonGroupGroup = optionalNonGroup.get();
            for (User member: nonGroupUsers){
                nonGroupGroup.addGroupMember(member);
            }
            groupRepository.save(nonGroupGroup);
        }

        logger.info("Finished adding teacher and MWAG to default groups");
    }
}
