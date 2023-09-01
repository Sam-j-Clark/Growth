package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nz.ac.canterbury.seng302.portfolio.CheckException;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents an Evidence entity
 */
@Entity
@Table(name = "evidence_table")
public class Evidence {

    @Id
    @GeneratedValue
    private int id;

    private int userId;
    private String title;
    private LocalDate occurrenceDate;

    /**
     * The description of a piece of evidence, which has a character length of 500.
     */
    @Column(length = 500)
    private String description;

    /** A list of the web links associated with a piece of Evidence */
    @OneToMany(mappedBy = "evidence", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private final Set<WebLink> webLinks = new HashSet<>();

    /** A list of the skills associated with the piece of Evidence */
    @ManyToMany
    @Fetch(FetchMode.JOIN)
    @JoinTable(name = "evidence_skills",
            joinColumns = @JoinColumn(name = "evidenceId"),
            inverseJoinColumns = @JoinColumn(name = "skillId"))
    private final Set<Skill> skills = new HashSet<>();

    /** The set of categories, can have SERVICE, QUANTITATIVE and QUALITATIVE. Can be multiple */
    @Enumerated(EnumType.ORDINAL)
    @ElementCollection(fetch = FetchType.EAGER)
    private final Set<Category> categories = new HashSet<>();

    /** A list of associates; people who also worked on this evidence.
     * Takes the form of their user IDs.
     * The owner is considered an associate.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private final Set<Integer> associateIds = new HashSet<>();

    /**
     * A list of all the user ids which are currently, or have been associated to this piece of
     * evidence
     */
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    private final Set<Integer> archivedIds = new HashSet<>();


    /**
     * Constructs an instance of the evidence object
     *
     * @param userId      the user associated with the evidence
     * @param title       the title of the evidence
     * @param date        the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int userId, String title, LocalDate date, String description) {
        checkTitleLength(title);
        checkDescriptionLength(description);
        this.userId = userId;
        this.title = title;
        this.occurrenceDate = date;
        this.description = description;
    }


    /**
     * This constructor is used for testing only! Constructs an instance of the evidence object
     *
     * @param evidenceId  the ID of the evidence with is typically Generated automatically.
     * @param userId      the user associated with the evidence
     * @param title       the title of the evidence
     * @param date        the date of the evidence creation
     * @param description the description of the evidence
     */
    public Evidence(int evidenceId, int userId, String title, LocalDate date, String description) {
        checkTitleLength(title);
        checkDescriptionLength(description);
        this.id = evidenceId;
        this.userId = userId;
        this.title = title;
        this.occurrenceDate = date;
        this.description = description;
    }


    /**
     * Default JPA Evidence constructor
     */
    public Evidence() {
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

    /**
     * Verifies that the title is less than 500 characters, and sets the property if so.
     *
     * @param title The title to be checked and set.
     */
    public void setTitle(String title) {
        checkTitleLength(title);
        this.title = title;
    }

    public LocalDate getDate() {
        return occurrenceDate;
    }

    public void setDate(LocalDate date) {
        this.occurrenceDate = date;
    }

    public String getDescription() {
        return description;
    }

    public Set<WebLink> getWebLinks() {
        return webLinks;
    }

    public void addWebLink(WebLink webLink) {
        this.webLinks.add(webLink);
    }

    public void clearWeblinks() {
        webLinks.clear();
    }

    /**
     * Verifies that the description is less than 500 characters, and sets the property if so.
     *
     * @param description The description to be checked and set.
     */
    public void setDescription(String description) {
        checkDescriptionLength(description);
        this.description = description;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void clearSkills() {
        skills.clear();
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }


    public Set<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void clearCategories() {
        categories.clear();
    }

    public List<Integer> getAssociateIds() {
        return new ArrayList<>(associateIds);
    }

    public void addAssociateId(Integer associateId) {
        associateIds.add(associateId);
        archivedIds.add(associateId);
    }

    public void removeAssociateId(Integer associateId) {
        associateIds.remove(associateId);
    }

    public void clearAssociatedIds() {
        associateIds.clear();
    }

    public Set<Integer> getArchivedIds() {
        return archivedIds;
    }

    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        return "{" +
                "\"id\":" + id +
                ",\"userId\":" + userId +
                ",\"title\":\"" + title +
                "\",\"description\":\"" + description +
                "\",\"webLinks\":" + "[]" +
                ",\"skills\":" + "[]" +
                ",\"categories\":" + "[]" +
                ",\"associateIds\":" + associateIds +
                "," + "\"date\":\"" + occurrenceDate + "\"" +
                "}";
    }


    /**
     * Checks that the title is less than 50 chars.
     *
     * @param title - the title to be checked.
     */
    private void checkTitleLength(String title) {
        if (title.length() > 50) {
            throw new CheckException("Title cannot be more than 50 characters");
        }
    }


    /**
     * Checks that the description is less than 500 chars.
     *
     * @param description - the description to be checked.
     */
    private void checkDescriptionLength(String description) {
        if (description.length() > 500) {
            throw new CheckException("description cannot be more than 500 characters");
        }
    }
}
