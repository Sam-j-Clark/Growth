package nz.ac.canterbury.seng302.portfolio.model.domain.evidence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;

import javax.persistence.*;
import java.net.MalformedURLException;


/**
 * Represents an WebLink Entity
 */
@Entity
@Table(name = "weblinks")
public class WebLink {

    public static final int MAXURLLENGTH = 2000;
    public static final int MAXNAMELENGTH = 50;

    @Id
    @GeneratedValue
    private int id;

    private String alias;
    @Column(length = MAXURLLENGTH + 1)
    private String url;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "evidence")
    private Evidence evidence;


    /**
     * Constructs an instance of the WebLink Object
     *
     * @param evidence The evidence that this web link is associated with
     * @param webLinkDTO The DTO containing the weblink's alias, address, and security.
     * @throws MalformedURLException when the url string is not valid. This Weblink is not allowed to be created.
     */
    public WebLink(Evidence evidence, WebLinkDTO webLinkDTO) {
        this.alias = webLinkDTO.getName();
        this.evidence = evidence;
        this.url = webLinkDTO.getUrl();
    }

    /**
     * Default JPA Evidence constructor
     */
    public WebLink() {
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getAlias() {
        return alias;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public int getMaxURLLength() {
        return MAXURLLENGTH;
    }

    /**
     * This method is used to help with testing. It returns the expected JSON string created for this object.
     *
     * @return the Json string the represents this piece of evidence.
     */
    public String toJsonString() {
        return "{" +
                "\"id\":" + id +
                ",\"alias\":\"" + alias + "\"" +
                ",\"url\":\"" + url +
                "\",\"maxURLLength\":" + getMaxURLLength() +
                "}";
    }
}
