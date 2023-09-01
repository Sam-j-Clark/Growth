package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Skill;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.SkillRepository;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for all the Skill based end points
 */
@Controller
public class SkillController {

    /** For logging the controller for debugging. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Holds persisted information about skills */
    private final SkillRepository skillRepository;

    /** Holds persisted information about evidence */
    private final EvidenceRepository evidenceRepository;

    /** For checking is a user exists and getting their details. */
    private final UserAccountsClientService userAccountsClientService;


    /**
     * Autowired constructor for injecting the required beans.
     *
     * @param skillRepository Holds persisted information about skills
     * @param evidenceRepository Holds persisted information about evidence
     * @param userAccountsClientService For checking is a user exists and getting their details.
     */
    @Autowired
    public SkillController(SkillRepository skillRepository,
                           EvidenceRepository evidenceRepository,
                           UserAccountsClientService userAccountsClientService) {
        this.skillRepository = skillRepository;
        this.evidenceRepository = evidenceRepository;
        this.userAccountsClientService = userAccountsClientService;
    }


    /**
     * Gets all the skills associated with a user with the supplied userId.
     *
     * @param userId - The userId of the user whose skills are requested
     * @return A ResponseEntity that contains a list of skills associated with the User.
     */
    @GetMapping("/skills")
    public ResponseEntity<Object> getSkillsByUserId(@RequestParam Integer userId) {
        logger.info("GET REQUEST /skills - attempt to get all skills for user: {}", userId);
        try {
            List<Skill> skills = skillRepository.findDistinctByEvidenceUserId(userId);
            if (skills.isEmpty()) {
                GetUserByIdRequest request = GetUserByIdRequest.newBuilder().setId(userId).build();
                UserResponse userResponse = userAccountsClientService.getUserAccountById(request);
                if (userResponse.getId() == -1) {
                    logger.info("GET REQUEST /skills - user {} does not exist", userId);
                    return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
                }
            }
            logger.info("GET REQUEST /skills - found and returned {} skills for user: {}", skills.size() ,userId);
            return new ResponseEntity<>(skills, HttpStatus.OK);

        } catch (Exception exception) {
            logger.error("GET REQUEST /skills - Internal Server Error attempt user: {}", userId);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Gets all the evidence associated with a user with the specified skill.
     *
     * @param skillName - The name of the skill whose pieces of evidence are requested - this ignores cases
     * @return A ResponseEntity that contains a list of evidences associated with the skill.
     */
    @GetMapping("/evidenceLinkedToSkill")
    public ResponseEntity<Object> getEvidenceBySkill(
            @RequestParam Integer userId,
            @RequestParam String skillName) {
        logger.info("GET REQUEST /evidenceLinkedToSkill - attempt to get all evidence for skill: {}", skillName);
        try {
            if (Objects.equals(skillName, "No Skill")) {
                List<Evidence> evidence = evidenceRepository.findAllByUserIdAndSkillsIsEmptyOrderByOccurrenceDateDesc(userId);
                logger.info("GET REQUEST /evidenceLinkedToSkill - No skill evidence retrieved");
                return new ResponseEntity<>(evidence, HttpStatus.OK);
            }
            Optional<Skill> skill = skillRepository.findDistinctByEvidenceUserIdAndNameIgnoreCase(userId, skillName);
            if (skill.isEmpty()) {
                logger.info("GET REQUEST /evidenceLinkedToSkill - skill {} does not exist", skillName);
                return new ResponseEntity<>("Skill does not exist", HttpStatus.NOT_FOUND);
            }
            List<Evidence> evidence = evidenceRepository.findAllByUserIdAndSkillsContainingOrderByOccurrenceDateDesc(userId, skill.get());
            logger.info("GET REQUEST /evidenceLinkedToSkill - found and returned {} evidences for skill: {}", evidence.size() ,skillName);
            return new ResponseEntity<>(evidence, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("GET REQUEST /evidenceLinkedToSkill - Internal Server Error attempt skill: {}", skillName);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
