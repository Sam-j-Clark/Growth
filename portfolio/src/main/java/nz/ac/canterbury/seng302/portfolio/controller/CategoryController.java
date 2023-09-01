package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Category;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;


/**
 * Controller for all the Category based end points
 */
@Controller
public class CategoryController {

    /** For logging the controller for debugging. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /** The repository containing users pieces of evidence. */
    @Autowired
    private EvidenceRepository evidenceRepository;


    /**
     * Gets all the evidence associated with a user with the specified Category.
     *
     * @param userId - The userId of the user whose evidence is wanted
     * @param category - The String of category is requested
     * @return A ResponseEntity that contains a list of evidences associated with the Category.
     */
    @GetMapping("/evidenceLinkedToCategory")
    public ResponseEntity<Object> getEvidenceByCategory(@RequestParam("userId") Integer userId, @RequestParam String category) {
        logger.info("GET REQUEST /evidenceLinkedToCategory - attempt to get all evidence for category: {}", category);
        try {
            Optional<Category> optionalCategory = parseCategory(category);
            if (optionalCategory.isEmpty()) {
                logger.info("GET REQUEST /evidenceLinkedToCategory - category {} does not exist", category);
                return new ResponseEntity<>("Category does not exist", HttpStatus.NOT_FOUND);
            }

            List<Evidence> evidence = evidenceRepository.findAllByUserIdAndCategoriesContainingOrderByOccurrenceDateDesc(userId, optionalCategory.get());
            logger.info("GET REQUEST /evidenceLinkedToCategory - found and returned {} evidences for category: {}", evidence.size() ,category);
            return new ResponseEntity<>(evidence, HttpStatus.OK);

        } catch (Exception exception) {
            logger.error("GET REQUEST /evidenceLinkedToCategory - Internal Server Error attempt category: {}", category);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if the category is valid
     *
     * @param category - The Category is requested
     * @return A boolean value of the category is valid or not
     */
    public static Optional<Category> parseCategory(String category) {
        return switch (category) {
            case "Service", "SERVICE" -> Optional.of(Category.SERVICE);
            case "Quantitative", "QUANTITATIVE" -> Optional.of(Category.QUANTITATIVE);
            case "Qualitative", "QUALITATIVE" -> Optional.of(Category.QUALITATIVE);
            default -> Optional.empty();
        };
    }
}