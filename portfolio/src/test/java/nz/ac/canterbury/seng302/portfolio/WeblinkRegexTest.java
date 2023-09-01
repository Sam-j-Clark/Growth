package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.WeblinkRegex;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WeblinkRegexTest {

    private final Pattern weblinkPattern = WeblinkRegex.getWeblinkPattern();

    private final List<String> expectedPasses = new ArrayList<>();
    private final List<String> expectedFails = new ArrayList<>();


    private void assertPasses() {
        for (String test : expectedPasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to match weblink regex, but didn't");
            }
        }
    }

    private void assertFails() {
        for (String test : expectedFails) {
            if (weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }


    @Test
    public void regexProtocolPasses() {
        expectedPasses.add("example");
        expectedPasses.add("www.canterbury.ac.nz/");
        expectedPasses.add("http://example");
        expectedPasses.add("http://example.com"); // from RFC documentation
        expectedPasses.add("http://example.com/"); // from RFC documentation
        expectedPasses.add("http://example.com:/"); // from RFC documentation
        expectedPasses.add("http://example.com:80/"); // from RFC documentation
        expectedPasses.add("https://www.google.com");

        assertPasses();
    }

    @Test
    public void regexDomainPasses() {
        expectedPasses.add("i_am_a_website.com");
        expectedPasses.add("CAPITALLETTERS.com");
        expectedPasses.add("www.google.com");
        expectedPasses.add("xn--j6h.com"); // Punycode encoded emoji
        expectedPasses.add("www.canterbury.ac.nz");
        expectedPasses.add("csse.canterbury.ac.nz");
        expectedPasses.add("http://132.181.106.9");

        assertPasses();
    }

    @Test
    public void regexPortPasses() {
        expectedPasses.add("http://example:");
        expectedPasses.add("http://example:80");
        expectedPasses.add("http://example:80000");

        assertPasses();
    }

    @Test
    public void regexPathPasses() {
        expectedPasses.add("https://example/");
        expectedPasses.add("http://example/path");
        expectedPasses.add("http://example:80000/path");
        expectedPasses.add("http://www.w3.org/Addressing/"); // from RFC documentation
        expectedPasses.add("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        expectedPasses.add("http://localhost:9000/portfolio?projectId=1");
        expectedPasses.add("http://hello/path/lots-of_chars_T0~.-()test");
        expectedPasses.add("https://learn.canterbury.ac.nz/login/index.php");
        expectedPasses.add("https://stackoverflow.com/questions/13009670/prolog-recursive-list-construction");

        assertPasses();
    }

    @Test
    public void regexQueryPasses() {
        expectedPasses.add("http://example?a=a");
        expectedPasses.add("http://example?sam=nerd");
        expectedPasses.add("http://example?llamas=biggerThanFrogs");
        expectedPasses.add("http://example?sam=nerd&april=100TimesAsSwag&harrison=27");

        assertPasses();
    }

    @Test
    public void regexFragmentPasses() {
        expectedPasses.add("https://example#");
        expectedPasses.add("https://example#letters");
        expectedPasses.add("https://example#numb3ers");
        expectedPasses.add("https://example#34sdfg-';");
        expectedPasses.add("http://www.ics.uci.edu/pub/ietf/uri/historical.html#WARNING"); // from RFC documentation

        assertPasses();
    }

    @Test
    public void regexProtocolFails() {
        expectedFails.add(".");
        expectedFails.add("ftp://ftp.is.co.za/rfc/rfc1808.txt");
        expectedFails.add("urn:oasis:names:specification:docbook:dtd:xml:4.1.2");
        expectedFails.add("tel:+1-816-555-1212");
        expectedFails.add("telnet://192.0.2.16:80/");
        expectedFails.add("mailto:John.Doe@example.com");
        expectedFails.add("news:comp.infosystems.www.servers.unix");
        expectedFails.add("://example");
        expectedFails.add("htt://example");
        expectedFails.add("htp://example");
        expectedFails.add("http:/example");
        expectedFails.add("http:///example");
        expectedFails.add("https:///example");
        expectedFails.add("hps://");
        expectedFails.add("https:/example");

        assertFails();
    }

    @Test
    public void regexDomainFails() {
        expectedFails.add(".example");
        expectedFails.add("http://.example");
        expectedFails.add("i am a website.com");
        expectedFails.add("'quote'.com");
        expectedFails.add("$$$money$$$.com");
        expectedFails.add("@.com");
        expectedFails.add("!.com");
        expectedFails.add("♨️.com");
        expectedFails.add("https://www.<script>Something naughty!</script>place.com");

        assertFails();
    }

    @Test
    public void regexPortFails() {
        expectedFails.add("example:800000");
        expectedFails.add("http://example:800000");
        expectedFails.add("example.com:port");
        expectedFails.add("example.com:@#$%^&");

        assertFails();
    }

    @Test
    public void regexPathFail() {
        expectedFails.add("https://");
        expectedFails.add("https://example?");

        assertFails();
    }

    @Test
    public void questionablePassCases() {
        List<String> questionablePasses = new ArrayList<>();

        questionablePasses.add("https//:example"); // does not pass on frontend
        questionablePasses.add("e/xample.com");
        questionablePasses.add("http//example"); // does not pass on frontend

        for (String test : questionablePasses) {
            if (! weblinkPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to match weblink regex, but didn't");
            }
        }
    }
}