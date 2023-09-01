package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Skill entity
 */
@Entity
@Table(name = "skills_table")
public class Skill {

    @Id
    @GeneratedValue
    private Integer id;

    @Column
    private String name;

    private double frequency;

    /** The set of evidence this skill is associated with */
    @JsonIgnore
    @ManyToMany(mappedBy = "skills", fetch = FetchType.EAGER)
    private final Set<Evidence> evidence = new HashSet<>();


    /**
     * Generic constructor used by JPA
     */
    protected Skill() {}


    /**
     * Constructor for a new skill.
     *
     * @param name - The name of the skill.
     */
    public Skill(String name) {
        this.name = name;
    }


    /**
     * Constructor for testing skills.
     *
     * @param name - The name of the skill.
     */
    public Skill(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Evidence> getEvidence() {
        return evidence;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }

    public void removeEvidence(Evidence evidenceToRemove) {
        evidence.remove(evidenceToRemove);
    }

    /**
     * For testing returns the expected json string of the object.
     *
     * @return the expected json string of the object.
     */
    public String toJsonString() {
        return "{\"id\":" + id +
                ",\"name\":\"" + name +
                "\",\"frequency\":" + frequency +"}";
    }
}
