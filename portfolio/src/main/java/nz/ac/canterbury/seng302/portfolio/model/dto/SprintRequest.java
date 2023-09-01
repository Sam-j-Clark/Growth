package nz.ac.canterbury.seng302.portfolio.model.dto;


public class SprintRequest {
    private String sprintId;
    private String sprintName;
    private String sprintStartDate;
    private String sprintEndDate;
    private String sprintDescription;
    private String sprintColour;

    public SprintRequest() {
        super();
    }

    public SprintRequest(String sprintId, String sprintName, String sprintStartDate, String sprintEndDate, String sprintDescription, String sprintColour) {
        super();
        this.sprintId = sprintId;
        this.sprintName = sprintName;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
        this.sprintDescription = sprintDescription;
        this.sprintColour = sprintColour;
    }

    public String getSprintColour() {
        return sprintColour;
    }

    public void setSprintColour(String sprintColour) {
        this.sprintColour = sprintColour;
    }

    public String getSprintId() {
        return this.sprintId;
    }

    public void setSprintId(String sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public String getSprintStartDate() {
        return sprintStartDate;
    }

    public void setSprintStartDate(String sprintStartDate) {
        this.sprintStartDate = sprintStartDate;
    }

    public String getSprintEndDate() {
        return sprintEndDate;
    }

    public void setSprintEndDate(String sprintEndDate) {
        this.sprintEndDate = sprintEndDate;
    }

    public String getSprintDescription() {
        return sprintDescription;
    }

    public void setSprintDescription(String sprintDescription) {
        this.sprintDescription = sprintDescription;
    }
}
