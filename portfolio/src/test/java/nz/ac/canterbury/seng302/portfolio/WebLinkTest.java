package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.Evidence;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.EvidenceRepository;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLink;
import nz.ac.canterbury.seng302.portfolio.model.domain.evidence.WebLinkRepository;
import nz.ac.canterbury.seng302.portfolio.model.dto.WebLinkDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WebLinkTest {

    @Autowired
    EvidenceRepository evidenceRepository;

    @Autowired
    WebLinkRepository webLinkRepository;

    private String WEBLINK_ADDRESS = "https://www.canterbury.ac.nz/";


    @Test
    void createTestEvidenceSingleWebLink() throws MalformedURLException {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        WebLinkDTO webLinkDTO = new WebLinkDTO("name", WEBLINK_ADDRESS);
        WebLink webLink = new WebLink(evidence, webLinkDTO);
        evidence.addWebLink(webLink);
        evidenceRepository.save(evidence);
        webLinkRepository.save(webLink);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getWebLinks().iterator().next().getUrl(), evidence.getWebLinks().iterator().next().getUrl());

    }


    @Test
    void createTestEvidenceMultipleWebLinks() throws MalformedURLException {
        Evidence evidence = new Evidence(1, "test", LocalDate.now(), "test");
        WebLinkDTO webLinkDTO1 = new WebLinkDTO("name", WEBLINK_ADDRESS);
        WebLinkDTO webLinkDTO2 = new WebLinkDTO("name", WEBLINK_ADDRESS);
        WebLinkDTO webLinkDTO3 = new WebLinkDTO("name", WEBLINK_ADDRESS);

        WebLink webLink = new WebLink(evidence, webLinkDTO1);
        WebLink webLink2 = new WebLink(evidence, webLinkDTO2);
        WebLink webLink3 = new WebLink(evidence, webLinkDTO3);
        evidence.addWebLink(webLink);
        evidence.addWebLink(webLink2);
        evidence.addWebLink(webLink3);
        evidenceRepository.save(evidence);
        webLinkRepository.save(webLink);
        webLinkRepository.save(webLink2);
        webLinkRepository.save(webLink3);

        Evidence evidence1 = evidenceRepository.findAllByUserIdOrderByOccurrenceDateDesc(1).get(0);
        Assertions.assertEquals(evidence1.getTitle(), evidence.getTitle());
        Assertions.assertEquals(evidence1.getWebLinks().iterator().next().getUrl(), evidence.getWebLinks().iterator().next().getUrl());
        Assertions.assertEquals(evidence1.getWebLinks().size(), evidence.getWebLinks().size());

    }

}
