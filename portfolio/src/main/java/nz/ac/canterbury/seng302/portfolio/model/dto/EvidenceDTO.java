package nz.ac.canterbury.seng302.portfolio.model.dto;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;

import java.util.List;

/**
 * Evidence Data Transfer Object, used for representing the data required to make a piece of evidence.
 */
public class EvidenceDTO {
    Integer id;
    String title;
    String date;
    String description;
    List<WebLinkDTO> webLinks;
    List<String> categories;
    List<Skill> skills;
    /**
     * The users associated with this piece of evidence.
     * Should NOT include the creator of the evidence.
     */
    List<Integer> associateIds;
    Long projectId;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WebLinkDTO> getWebLinks() {
        return webLinks;
    }

    public void setWebLinks(List<WebLinkDTO> webLinks) {
        this.webLinks = webLinks;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Integer> getAssociateIds() {
        return associateIds;
    }

    public void setAssociateIds(List<Integer> associateIds) {
        this.associateIds = associateIds;
    }

    public void addAssociatedId(Integer userId) {
        if (! associateIds.contains(userId)) {
            associateIds.add(userId);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static class EvidenceDTOBuilder {
        private Integer id;
        private String title;
        private String date;
        private String description;
        private List<WebLinkDTO> webLinks;
        private List<String> categories;
        private List<Skill> skills;
        private List<Integer> associateIds;
        private Long projectId;

        public EvidenceDTOBuilder setId(Integer id) {
            this.id = id;
            return this;
        }

        public EvidenceDTOBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public EvidenceDTOBuilder setDate(String date) {
            this.date = date;
            return this;
        }

        public EvidenceDTOBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public EvidenceDTOBuilder setWebLinks(List<WebLinkDTO> webLinks) {
            this.webLinks = webLinks;
            return this;
        }

        public EvidenceDTOBuilder setCategories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public EvidenceDTOBuilder setSkills(List<Skill> skills) {
            this.skills = skills;
            return this;
        }

        public EvidenceDTOBuilder setAssociateIds(List<Integer> associateIds) {
            this.associateIds = associateIds;
            return this;
        }

        public EvidenceDTOBuilder setProjectId(Long projectId) {
            this.projectId = projectId;
            return this;
        }

        public EvidenceDTO build() {
            EvidenceDTO evidenceDTO = new EvidenceDTO();
            evidenceDTO.setId(this.id);
            evidenceDTO.setTitle(title);
            evidenceDTO.setDate(date);
            evidenceDTO.setDescription(description);
            evidenceDTO.setWebLinks(webLinks);
            evidenceDTO.setCategories(categories);
            evidenceDTO.setSkills(skills);
            evidenceDTO.setAssociateIds(associateIds);
            evidenceDTO.setProjectId(projectId);
            return evidenceDTO;
        }
    }
}
