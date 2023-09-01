package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.RegexPattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

class AccessTokenRegexTest {
    private final Pattern tokenPattern = RegexPattern.GITLAB_TOKEN.getPattern();

    private void assertPasses(String[] expectedPasses) {
        for (String test : expectedPasses) {
            if (!tokenPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to match token regex, but didn't");
            }
        }
    }

    private void assertFails(String[] expectedFails) {
        for (String test : expectedFails) {
            if (tokenPattern.matcher(test).matches()) {
                Assertions.fail("Expected " + test + " to not match weblink regex, but did");
            }
        }
    }

    @Test
    void testValidTokens() {
        String[] expectedPasses = {
                "MysE3EYxRooxpDijMpHW",
                "YiAm5W7WCyTLxbpJxiLH",
                "MLXjrLKbUjE4_UrMKKRm",
                "exy3JCugzkfb5LGExoe8",
                "-Cxzo7tXNqTPL-YckzTc",
                "pp8zCj4kNw2vZ3X9pdtp",
                "ZuWJb17haaLZeWPs7AZ2",
                "HXLhdVXAR6nKTsTSdda4",
        };
        assertPasses(expectedPasses);
    }

    @Test
    void testTooShortTokens() {
        String[] expectedFails = {
                "Mys",
                "YiAm5W7WCyTLxbpJxiL",
                "MLXKKR",
                "exy3JCugzkfb5LGExoe",
                "-Cxzo7tXNqTPL-YckzT",
                "zCj4kNw2vZ3X9pdt",
                "17haaLZeWPs7AZ",
                "HXLhdVXAR6nKTsTSdda",
        };
        assertFails(expectedFails);
    }

    @Test
    void testInvalidCharacters() {
        String[] expectedFails = {
                "$$$E3EYxRooxpDijMpHW",
                "@@@MysE3EYxRooxpDijMpH@",
                "MysE3EYxRooxpDijMpH++"
        };
        assertFails(expectedFails);
    }
}
