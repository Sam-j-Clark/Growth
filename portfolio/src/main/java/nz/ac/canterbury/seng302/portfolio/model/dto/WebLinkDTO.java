package nz.ac.canterbury.seng302.portfolio.model.dto;

/**
 * Weblink Data Transfer Object, which represents a valid web link with an alias.
 */
public class WebLinkDTO {

    /** The readable name of the link */
    private String name;

    /** The followable URL */
    private String url;


    public WebLinkDTO(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() { return name; }

    public String getUrl() { return url; }

    public void setName(String name) { this.name = name; }

    public void setUrl(String url) { this.url = url; }
}
