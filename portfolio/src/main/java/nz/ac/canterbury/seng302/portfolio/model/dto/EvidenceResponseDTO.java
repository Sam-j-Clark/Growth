package nz.ac.canterbury.seng302.portfolio.model.dto;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Category;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A DTO that returns the details of an evidence object
 */
public class EvidenceResponseDTO {

    /**
     * The ID of the evidence.
     */
    private int id;
    /**
     * The ID of the user who created/owns the evidence.
     */
    private int userId;
    /**
     * The title of the evidence.
     */
    private String title;
    /**
     * This is the date the evidence occurred, not the date it was created.
     */
    private LocalDate date;
    /**
     * The description of the evidence.
     */
    private String description;
    /**
     * The weblinks associated to the evidence.
     */
    private final Set<nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink> webLinks;
    /**
     * The skills associated to the evidence.
     */
    private final Set<Skill> skills;
    /**
     * The categories associated to the evidence.
     */
    private final Set<Category> categories;
    /**
     * The ids of users associated to the evidence. The owner is an associate.
     */
    private final List<Integer> associateIds;
    /**
     * The associated users to the piece of evidence.
     * This list has the details of each user within it.
     * The owner is an associate.
     */
    private List<UserDTO> associates;

    /**
     * Constructor that will convert an Evidence object into an EvidenceResponseDTO object.
     * Takes an additional argument to add a list of associates.
     *
     * @param evidence   The evidence you are converting from
     * @param associates The list of associate UserResponses
     */
    public EvidenceResponseDTO(Evidence evidence, List<UserDTO> associates) {
        this.id = evidence.getId();
        this.userId = evidence.getUserId();
        this.title = evidence.getTitle();
        this.date = evidence.getDate();
        this.description = evidence.getDescription();
        this.webLinks = evidence.getWebLinks();
        this.skills = evidence.getSkills();
        this.categories = evidence.getCategories();
        this.associateIds = evidence.getAssociateIds();
        this.associates = associates;
    }

    /**
     * Constructor that will convert an Evidence object into an EvidenceResponseDTO object.
     *
     * @param evidence The evidence you are converting from
     */
    public EvidenceResponseDTO(Evidence evidence) {
        this.userId = evidence.getUserId();
        this.title = evidence.getTitle();
        this.date = evidence.getDate();
        this.description = evidence.getDescription();
        this.webLinks = evidence.getWebLinks();
        this.skills = evidence.getSkills();
        this.categories = evidence.getCategories();
        this.associateIds = evidence.getAssociateIds();
        this.associates = new ArrayList<>();
    }

    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        StringBuilder associateUsers = new StringBuilder("[");
        for (int i = 0; i < associates.size(); i++) {
            associateUsers.append(associates.get(i).toJsonString());
            if (i < associates.size() - 1) associateUsers.append(",");
        }
        associateUsers.append("]");
        //The JSON returns without spaces in between elements, so we remove them.
        String associateIdsNoSpaces = associateIds.toString().replace(" ", "");
        return "{" +
                "\"id\":" + id +
                "," + "\"userId\":" + userId +
                "," + "\"title\":\"" + title + "\"" +
                "," + "\"date\":\"" + date + "\"" +
                "," + "\"description\":\"" + description + "\"" +
                "," + "\"webLinks\":" + webLinks +
                "," + "\"skills\":" + skills +
                "," + "\"categories\":" + categories +
                "," + "\"associateIds\":" + associateIdsNoSpaces +
                "," + "\"associates\":" + associateUsers +
                "}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<WebLink> getWebLinks() {
        return webLinks;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public List<Integer> getAssociateIds() {
        return associateIds;
    }

    public List<UserDTO> getAssociates() {
        return associates;
    }

    public void setAssociates(List<UserDTO> associates) {
        this.associates = associates;
    }
}
