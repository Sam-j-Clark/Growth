package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Category;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CategoriesTest {

    @Autowired
    EvidenceRepository evidenceRepository;

    @Test
    void CategoryCanBeAdded() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        evidence.addCategory(Category.QUANTITATIVE);
        evidenceRepository.save(evidence);

        Evidence evidenceOptional = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);

        Assertions.assertEquals(1, evidenceOptional.getCategories().size());
        Assertions.assertTrue(evidenceOptional.getCategories().contains(Category.QUANTITATIVE));
    }


    @Test
    void MultipleCategoriesCanBeAdded() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        evidence.addCategory(Category.QUANTITATIVE);
        evidence.addCategory(Category.SERVICE);
        evidenceRepository.save(evidence);

        Evidence evidenceOptional = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);

        Assertions.assertEquals(2, evidenceOptional.getCategories().size());
        Assertions.assertTrue(evidenceOptional.getCategories().contains(Category.QUANTITATIVE));
        Assertions.assertTrue(evidenceOptional.getCategories().contains(Category.SERVICE));
    }


    @Test
    void GetEvidenceByUserIdAndCategories() {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        evidence.addCategory(Category.QUANTITATIVE);
        evidenceRepository.save(evidence);

        List<Evidence> evidenceExists = evidenceRepository.findAllByUserIdAndCategoriesContainingOrderByOccurrenceDateDesc(1, Category.QUANTITATIVE);
        List<Evidence> evidenceDoesntExist1 = evidenceRepository.findAllByUserIdAndCategoriesContainingOrderByOccurrenceDateDesc(1, Category.SERVICE);
        List<Evidence> evidenceDoesntExist2 = evidenceRepository.findAllByUserIdAndCategoriesContainingOrderByOccurrenceDateDesc(2, Category.QUANTITATIVE);
        List<Evidence> evidenceDoesntExist3 = evidenceRepository.findAllByUserIdAndCategoriesContainingOrderByOccurrenceDateDesc(2, Category.SERVICE);

        Assertions.assertEquals(1, evidenceExists.size());
        Assertions.assertTrue(evidenceExists.get(0).getCategories().contains(Category.QUANTITATIVE));
        Assertions.assertEquals(0, evidenceDoesntExist1.size());
        Assertions.assertEquals(0, evidenceDoesntExist2.size());
        Assertions.assertEquals(0, evidenceDoesntExist3.size());
    }
}
