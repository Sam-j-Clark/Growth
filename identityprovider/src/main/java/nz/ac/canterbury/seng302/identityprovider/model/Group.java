package nz.ac.canterbury.seng302.identityprovider.model;

import javax.persistence.*;

import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a group of users.
 */
@Entity
@Table(name = "group_table")
public class Group {

    /** The unique ID of the Group. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    /** A list of the users in the group. */
    @ManyToMany
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "groupId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    private List<User> userList = new ArrayList<>();

    /** The group's short name. */
    private String shortName;

    /** The group's long name. */
    private String longName;


    /** The Group constructor required by JPA. */
    protected Group() {}


    /**
     * The default constructor for a group, which automatically generates a unique ID.
     *
     * @param shortName The group's short name.
     * @param longName The group's long name.
     */
    public Group (String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }


    /**
     * The constructor for a group with a specified group ID.
     *
     * @param id        The ID of the group to be created.
     * @param shortName The short name of the group to be created.
     * @param longName  The long name of the group to be created.
     */
    public Group(Integer id, String shortName, String longName) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
    }


    public Integer getId() {
        return id;
    }


    public List<User> getUserList() {
        return this.userList;
    }


    public Integer getMembersNumber() { return this.userList.size(); }


    public String getShortName() {
        return shortName;
    }


    public String getLongName() {
        return longName;
    }


    public void setLongName(String longName) {
        this.longName = longName;
    }


    public void setShortName(String shortName) {
        this.shortName = shortName;
    }


    /**
     * Removes a list of users from a group.
     *
     * @param users a list of the users to be removed.
     */
    public void removeGroupMembers(List<User> users) {
        for (User user : users) {
            userList.remove(user);
        }
    }


    /**
     * Removes a user from the group
     *
     * @param user the user to be removed
     */
    public boolean removeGroupMember(User user) {
        return userList.remove(user);
    }


    /**
     * Adds users from a list to the group object. Each user is only added if the user is not already present.
     *
     * @param users A list of the users to be added.
     */
    public void addGroupMembers(List<User> users) {
        for (User user : users) {
            if (!userList.contains(user)) {
                userList.add(user);
            }
        }
    }


    /**
     * Adds a user to the group if the user is not already present
     *
     * @param user The user to be added
     */
    public void addGroupMember(User user) {
        if (!userList.contains(user)) {
            userList.add(user);
        }
    }


    /**
     * Converts this group to a GroupDetailsResponse.
     *
     * @return GroupDetailsResponse - the GroupDetailsResponse equivalent of this group.
     */
    public GroupDetailsResponse groupDetailsResponse() {
        GroupDetailsResponse.Builder response = GroupDetailsResponse.newBuilder()
                .setLongName(this.getLongName())
                .setShortName(this.getShortName())
                .setGroupId(this.getId());
        List<User> groupMembers = this.getUserList();
        for (User user : groupMembers) {
            response.addMembers(user.userResponse());
        }

        return response.build();
    }
}
