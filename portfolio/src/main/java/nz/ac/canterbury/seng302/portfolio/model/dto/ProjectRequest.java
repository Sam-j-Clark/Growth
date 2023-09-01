package nz.ac.canterbury.seng302.portfolio.model.dto;


public class ProjectRequest {
    private String projectId;
    private String projectName;
    private String projectStartDate;
    private String projectEndDate;
    private String projectDescription;

    public ProjectRequest() {
        super();
    }

    public ProjectRequest(String projectId, String projectName, String projectStartDate, String projectEndDate, String projectDescription) {
        super();
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.projectDescription = projectDescription;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(String projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public String getProjectEndDate() {
        return projectEndDate;
    }

    public void setProjectEndDate(String projectEndDate) {
        this.projectEndDate = projectEndDate;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }
}

