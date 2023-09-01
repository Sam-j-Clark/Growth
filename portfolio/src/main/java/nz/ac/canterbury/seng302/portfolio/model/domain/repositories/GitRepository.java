package nz.ac.canterbury.seng302.portfolio.model.domain.repositories;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * This class models a Git repository object for showing on a groups' repo (settings) page.
 *
 * Currently, the required fields are:
 *
 *  - the repo id (a generated primary key)
 *  - the groupId of the group that owns the repo
 *  - The project id, as seen on a gitlab repo
 *  - The alias (name) of the repository
 *  - The accessToken to access the GitLab API information
 */
@Entity
public class GitRepository {

    /** An automatically generated repository ID to be the primary key of the repo. */
    @Id
    @GeneratedValue
    private Integer id;

    /** The group id of the group who owns the repo (foreign key) */
    private Integer groupId;

    /** The project id of the repo as seen on any gitlab page.  */
    private Integer projectId;

    /** A user defined name for their repository */
    private String alias;

    /** The access token to retrieve repository information from the GitLab API */
    private String accessToken;


    /**
     * Required default constructor for JPA
     */
    protected GitRepository() {}


    /**
     * The general (typical) constructor for the GitRepository object. This generates an id automatically
     *
     * @param groupId - The id of the group who owns the repository
     * @param projectId - The project id of the repo as seen on any gitlab page.
     * @param alias - A user defined name for their repository
     * @param accessToken - The access token to retrieve repository information from the GitLab API
     */
    public GitRepository(Integer groupId, Integer projectId, String alias, String accessToken) {
        this.groupId = groupId;
        this.projectId = projectId;
        this.alias = alias;
        this.accessToken = accessToken;
    }


    public Integer getId() {
        return id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
