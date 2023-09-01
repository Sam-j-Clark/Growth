package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.spy;

class SkillFrequencyServiceTest {

    private final EvidenceRepository evidenceRepository = spy(EvidenceRepository.class);
    private final SkillRepository skillRepository = spy(SkillRepository.class);
    private Skill skill;


    @InjectMocks
    private SkillFrequencyService skillFrequencyService = new SkillFrequencyService(evidenceRepository, skillRepository);


    @Test
    void testFrequency(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(5, 10, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(5, 10, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.50, frequency);
    }


    @Test
    void testFrequencyPointTwo(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(20, 100, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(20, 100, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.20, frequency);
    }

    @Test
    void testFrequencyZero(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(0, 100, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(0, 100, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.00, frequency);
    }


    @Test
    void testFrequencyPointTwoFive(){
        ArrayList<Evidence> evidenceListWithSkill = createEvidenceList(1, 4, true);
        ArrayList<Evidence> evidenceListTotal = createEvidenceList(1, 4, false);
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.25, frequency);
    }


    @Test
    void testFrequencyNoEvidence() throws CheckException {
        ArrayList<Evidence> evidenceListWithSkill = new ArrayList<>();
        ArrayList<Evidence> evidenceListTotal = new ArrayList<>();
        Mockito.when(evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(1, skill)).thenReturn(evidenceListWithSkill);
        Mockito.when(evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1)).thenReturn(evidenceListTotal);
        double frequency = skillFrequencyService.getSkillFrequency(skill, 1);
        Assertions.assertEquals(0.0, frequency);
    }


    /**
     * Returns a list of evidence and takes 3 parameters
     * @param amountOfEvidenceWithSkill The amount of evidence with the skill contained
     * @param amountOfEvidenceWithoutSkill The amount of evidence in total
     * @param returnJustSkills Boolean to return just the evidence with the skills, or the total amount.
     * @return An array list of evidence.
     */
    ArrayList<Evidence> createEvidenceList(int amountOfEvidenceWithSkill, int amountOfEvidenceWithoutSkill, boolean returnJustSkills) {
        skill = new Skill("test");
        ArrayList<Evidence> evidenceList = new ArrayList<>();
        for(int i = 0; i < amountOfEvidenceWithSkill; i++) {
            Evidence evidence = new Evidence(1, "evidence" + i, LocalDate.now(), "test evidence");
            evidence.addSkill(skill);
            evidenceList.add(evidence);
        }
        if (returnJustSkills){
            return evidenceList;
        }

        for(int i = amountOfEvidenceWithSkill; i < amountOfEvidenceWithoutSkill; i++) {
            Evidence evidence = new Evidence(1, "evidence non skill" + i, LocalDate.now(), "test evidence");
            evidenceList.add(evidence);
        }
        return evidenceList;
    }


}
