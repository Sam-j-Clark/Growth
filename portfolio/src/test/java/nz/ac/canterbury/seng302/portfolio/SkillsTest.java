package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SkillsTest {

    @Autowired
    EvidenceRepository evidenceRepository;

    @Autowired
    SkillRepository skillRepository;


    @Test
    void createTestEvidenceSkill() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Skill skill = new Skill("Testing");
        skillRepository.save(skill);
        evidence.addSkill(skill);
        evidenceRepository.save(evidence);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getSkills().size(), evidence.getSkills().size());
        Assertions.assertEquals(1, evidence.getSkills().size());
    }


    @Test
    void createTestEvidenceSkills() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Skill skill1 = new Skill("Testing 1");
        Skill skill2 = new Skill("Testing 2");
        Skill skill3 = new Skill("Testing 3");
        evidence.addSkill(skill1);
        evidence.addSkill(skill2);
        evidence.addSkill(skill3);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        skillRepository.save(skill3);
        evidenceRepository.save(evidence);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getSkills().size(), evidence.getSkills().size());
        Assertions.assertEquals(3, evidence.getSkills().size());
    }


    @Test
    void findAllSkillsByUserId() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        Evidence evidenceWithNoSkills = new Evidence(2, "test", LocalDate.now(), "test");
        Skill skill1 = new Skill("Testing 1");
        Skill skill2 = new Skill("Testing 2");
        Skill skill3 = new Skill("Testing 3");
        evidence.addSkill(skill1);
        evidence.addSkill(skill2);
        evidence.addSkill(skill3);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        skillRepository.save(skill3);
        evidenceRepository.save(evidence);
        evidenceRepository.save(evidenceWithNoSkills);

        List<Skill> skillsForUser1 = skillRepository.findDistinctByEvidenceUserId(1);
        List<Skill> skillsForUser2 = skillRepository.findDistinctByEvidenceUserId(2);
        Assertions.assertEquals(3, skillsForUser1.size());
        Assertions.assertEquals(skill1.getName(), skillsForUser1.get(0).getName());
        Assertions.assertEquals(skill2.getName(), skillsForUser1.get(1).getName());
        Assertions.assertEquals(skill3.getName(), skillsForUser1.get(2).getName());

        Assertions.assertEquals(0, skillsForUser2.size());
    }


    @Test
    void testSkillsRepositoryIsNotCaseSensitive() {
        Skill skill1 = new Skill("Testing 1");
        String differentCaseSearchQuery = "tesTing 1";

        skillRepository.save(skill1);
        Optional<Skill> optionalSkill = skillRepository.findByNameIgnoreCase(differentCaseSearchQuery);
        if (optionalSkill.isEmpty()) {
            Assertions.fail("Repository is case sensitive");
        }
        Skill foundSkill = optionalSkill.get();
        Assertions.assertNotEquals(foundSkill.getName(), differentCaseSearchQuery);
        Assertions.assertEquals(skill1.getName(), foundSkill.getName());
    }


    @Test
    void testSkillNameUniqueToUser() {
        Evidence evidence1 = new Evidence(1, "test", LocalDate.now(), "test");
        Evidence evidence2 = new Evidence(2, "test", LocalDate.now(), "test");
        Skill skill1 = new Skill("TESTING 1");
        Skill skill2 = new Skill("testing 1");
        evidence1.addSkill(skill1);
        evidence2.addSkill(skill2);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        evidenceRepository.save(evidence1);
        evidenceRepository.save(evidence2);

        List<Skill> skillsForUser1 = skillRepository.findDistinctByEvidenceUserId(1);
        List<Skill> skillsForUser2 = skillRepository.findDistinctByEvidenceUserId(2);

        Assertions.assertEquals(skill1.getName(), skillsForUser1.get(0).getName());
        Assertions.assertEquals(skill2.getName(), skillsForUser2.get(0).getName());
    }


    @Test
    void testDeletingEvidenceRemovesNoLongerValidSkills() {
        int userId = 1;
        Evidence evidence1 = new Evidence(userId, "Has one Skill", LocalDate.now(), "test");
        Evidence evidence2 = new Evidence(userId, "Has two skills", LocalDate.now(), "another test");
        Skill skill1 = new Skill("TESTING 1");
        Skill skill2 = new Skill("TESTING 2");
        evidence1.addSkill(skill1);
        evidence2.addSkill(skill1);
        evidence2.addSkill(skill2);
        skillRepository.save(skill1);
        skillRepository.save(skill2);
        evidenceRepository.save(evidence1);
        evidenceRepository.save(evidence2);

        List<Skill> skillsForUser = skillRepository.findDistinctByEvidenceUserId(userId);

        Assertions.assertEquals(2, skillsForUser.size());
        Assertions.assertEquals(skill1.getName(), skillsForUser.get(0).getName());
        Assertions.assertEquals(skill2.getName(), skillsForUser.get(1).getName());

        evidenceRepository.delete(evidence2);

        skillsForUser = skillRepository.findDistinctByEvidenceUserId(userId);

        Assertions.assertEquals(1, skillsForUser.size());
        Assertions.assertEquals(skill1.getName(), skillsForUser.get(0).getName());
    }
}
