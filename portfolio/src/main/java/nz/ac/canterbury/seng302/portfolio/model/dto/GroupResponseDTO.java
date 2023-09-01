package nz.ac.canterbury.seng302.portfolio.model.dto;

import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroupResponseDTO {

    private final Integer id;

    /**
     * The group's short name.
     */
    private final String shortName;

    /**
     * The group's long name.
     */
    private final String longName;

    private final List<UserDTO> userList = new ArrayList<>();


    public GroupResponseDTO(GroupDetailsResponse groupDetailsResponse) {
        this.id = groupDetailsResponse.getGroupId();
        this.shortName = groupDetailsResponse.getShortName();
        this.longName = groupDetailsResponse.getLongName();

        for (UserResponse userResponse : groupDetailsResponse.getMembersList()) {
            userList.add(new UserDTO(userResponse));
        }
        userList.sort(Comparator.comparingInt(UserDTO::getId));
    }

    public Integer getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public List<UserDTO> getUserList() {
        return userList;
    }
}
