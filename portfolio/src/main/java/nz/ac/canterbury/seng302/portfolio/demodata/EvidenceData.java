package nz.ac.canterbury.seng302.portfolio.demodata;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.*;
import nz.ac.canterbury.seng302.portfolio.service.SkillFrequencyService;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 *The service to initialize the evidence, weblink and skills data.
 */
@Service
public class EvidenceData {

    /** Logs the applications' initialisation process */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The repository contain the evidence */
    private final EvidenceRepository evidenceRepository;

    /** The repository contain the weblinks */
    private final WebLinkRepository webLinkRepository;

    /** The repository contain the skills */
    private final SkillRepository skillRepository;

    /** Provides helper functions for skill frequency operations */
    private final SkillFrequencyService skillFrequencyService;


    @Autowired
    public EvidenceData(EvidenceRepository evidenceRepository,
                        WebLinkRepository webLinkRepository,
                        SkillRepository skillRepository,
                        SkillFrequencyService skillFrequencyService) {
        this.evidenceRepository = evidenceRepository;
        this.webLinkRepository = webLinkRepository;
        this.skillRepository = skillRepository;
        this.skillFrequencyService = skillFrequencyService;
    }


    /**
     * Adds in the default evidence, skills and weblinks.
     */
    public void createEvidenceData() {
        try {
            LocalDate date = LocalDate.now();

            int adminId = 29;
            int steveId = 30;

            Evidence evidence = evidenceRepository.save(new Evidence(adminId, "Title", date, "Description"));
            Evidence evidence1 = evidenceRepository.save(new Evidence(adminId, "Created test Data", date, "Created a selection of default evidence objects for testing"));
            Evidence evidence2 = evidenceRepository.save(new Evidence(adminId, "making more evidence", date, "Description of another one"));
            Evidence evidence3 = evidenceRepository.save(new Evidence(adminId, "Writing Long Descriptions", date, "A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. "));
            Evidence evidence4 = evidenceRepository.save(new Evidence(adminId, "No Skill Evidence", date, "A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. A really long Description. "));

            Evidence steveEvidence = evidenceRepository.save(new Evidence(steveId, "Created Python Program", date, "I created a python program that ran through the library's database to find all of shakespears books"));
            Evidence steveEvidence1 = evidenceRepository.save(new Evidence(steveId, "Created test Data", date, "Created a selection of test data to make sure our application runs smoothly"));

            WebLinkDTO webLinkDTO = new WebLinkDTO( "localhost",  "https://localhost");
            WebLinkDTO webLinkDTO2 = new WebLinkDTO( "evidence1 weblink",  "https://localhost/evidence1");
            WebLinkDTO webLinkDTO3 = new WebLinkDTO( "lots of web links",  "https://lotsOfTestWeblinks");

            WebLink webLink =  webLinkRepository.save(new WebLink(evidence, webLinkDTO));
            WebLink webLink1 = webLinkRepository.save(new WebLink(evidence1, webLinkDTO2));
            WebLink webLink2 = webLinkRepository.save(new WebLink(evidence1, webLinkDTO3));

            Skill skill = skillRepository.save(new Skill("test"));
            Skill skill1 = skillRepository.save(new Skill("java"));
            Skill skill2 = skillRepository.save(new Skill("debugging"));
            Skill skill3 = skillRepository.save(new Skill("making data"));

            evidence.addWebLink(webLink);
            evidence1.addWebLink(webLink1);
            evidence1.addWebLink(webLink2);

            steveEvidence.addWebLink(webLink1);
            steveEvidence1.addWebLink(webLink2);

            evidence.addSkill(skill);
            evidence.addSkill(skill1);
            evidence.addSkill(skill2);
            evidence.addSkill(skill3);

            evidence1.addSkill(skill);
            evidence1.addSkill(skill1);
            evidence1.addSkill(skill2);

            evidence2.addSkill(skill2);
            evidence2.addSkill(skill3);

            evidence3.addSkill(skill);
            evidence3.addSkill(skill1);
            evidence3.addSkill(skill2);
            evidence3.addSkill(skill3);

            steveEvidence.addSkill(skill2);
            steveEvidence1.addSkill(skill);
            steveEvidence1.addSkill(skill3);

            evidence.addCategory(Category.SERVICE);
            evidence.addCategory(Category.QUALITATIVE);
            evidence.addCategory(Category.QUANTITATIVE);
            evidence1.addCategory(Category.SERVICE);
            evidence1.addCategory(Category.QUALITATIVE);
            evidence2.addCategory(Category.SERVICE);
            evidence3.addCategory(Category.QUALITATIVE);
            evidence4.addCategory(Category.QUANTITATIVE);

            steveEvidence.addCategory(Category.QUANTITATIVE);
            steveEvidence1.addCategory(Category.SERVICE);

            evidence.addAssociateId(adminId);
            evidence1.addAssociateId(adminId);
            evidence2.addAssociateId(adminId);
            evidence3.addAssociateId(adminId);
            evidence4.addAssociateId(adminId);

            steveEvidence.addAssociateId(steveId);
            steveEvidence1.addAssociateId(steveId);

            evidenceRepository.save(evidence);
            evidenceRepository.save(evidence1);
            evidenceRepository.save(evidence2);
            evidenceRepository.save(evidence3);
            evidenceRepository.save(evidence4);

            skillFrequencyService.updateAllSkillFrequenciesForUser(adminId);
            skillFrequencyService.updateAllSkillFrequenciesForUser(steveId);

            evidenceRepository.save(steveEvidence);
            evidenceRepository.save(steveEvidence1);
        } catch (Exception exception) {
            logger.error("Error occurred loading default evidence");
            logger.error(exception.getMessage());
        }
    }
}
