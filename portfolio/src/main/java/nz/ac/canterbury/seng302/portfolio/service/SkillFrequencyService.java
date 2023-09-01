package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The service bean responsible for skill frequency
 */
@Service
public class SkillFrequencyService {

    /** Holds persisted information about evidence */
    private final EvidenceRepository evidenceRepository;

    /** Holds persisted information about skills */
    private final SkillRepository skillRepository;


    /**
     * Autowired constructor
     * @param evidenceRepository Evidence storage
     * @param skillRepository Holds persisted information about skills
     */
    @Autowired
    public SkillFrequencyService(EvidenceRepository evidenceRepository, SkillRepository skillRepository) {
        this.evidenceRepository = evidenceRepository;
        this.skillRepository = skillRepository;
    }


    /**
     * Gets a list of evidence that's associated with a skill and a user and a list of evidence for a user and
     * divides them by each other to get the frequency, then rounds down to 2 decimal place.
     * Will return 0.0 if the user has no evidence.
     *
     * @param skill The skill object we want the frequency for
     * @param userId The id of the user that we want to find evidence for
     *
     * @return How frequently the skill appears in the users evidence. Ranges from 0 (none of the time) to 1 (all of the time).
     */
    public double getSkillFrequency(Skill skill, Integer userId){
        List<Evidence> evidenceListAssociatedWithSkill = evidenceRepository
                .findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(userId, skill);
        List<Evidence> evidenceListForUser = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(userId);
        if (evidenceListForUser.isEmpty()) {
            return 0.0;
        }
        double value = (double) evidenceListAssociatedWithSkill.size() / evidenceListForUser.size();
        return Double.parseDouble(String.format("%.2f", value));
    }

    /**
     * Retrieves all skills for the given user from the database and then calls getSkillFrequency on each one
     * @param userId The id of the user that we want to update skills for.
     */
    public void updateAllSkillFrequenciesForUser(Integer userId) {
        skillRepository.findDistinctByEvidenceUserId(userId).forEach((
                skill -> {
                    skill.setFrequency(getSkillFrequency(skill, userId));
                    skillRepository.save(skill);
                }
        ));
    }


}
