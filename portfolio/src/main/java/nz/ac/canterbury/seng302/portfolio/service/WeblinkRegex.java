package nz.ac.canterbury.seng302.portfolio.service;


import java.util.regex.Pattern;

public class WeblinkRegex {

    private static final String[] ALLOWED_PROTOCOLS = {"http", "https"};

    //from rfc https://www.ietf.org/rfc/rfc2396.txt
    private static final String ESCAPED = "(%[\\da-f][\\da-f])";
    private static final String RESERVED = ";\\/\\?:@&=\\+\\$,";
    private static final String UNRESERVED = "-\\w\\.!~\\*'\\(\\)";
    private static final String URIC = "[" + UNRESERVED + RESERVED + "]" + "|" + ESCAPED;

    private static final String PATH_CHARS = "[-\\w\\.~!\\$&'\\(\\)\\*\\+,;=:@\\/]" + "|" + ESCAPED;

    /**
     * The protocol can be any of the options in the ALLOWED_PROTOCOL array above.
     * If a domain name is present, it must be followed by "://".
     */
    private static final String PROTOCOL = "((%s)://)".formatted(String.join("|", ALLOWED_PROTOCOLS));

    /**
     * The domain includes numbers, letters, and the special characters :-~_. .
     * The "." character is only allowed after a non "." character has been used.
     * There must be at least one domain character in the weblink.
     */
    private static final String DOMAIN = "(([\\w~_-])+(\\.([\\w~-])+)*)";

    /**
     * The port starts with a ":" character.
     * The colon is followed by between 0 and 5 digits.
     * In theory, the max is 65535, but we have not been that specific.
     * Optional.
     */
    private static final String PORT = "(:(\\d{0,5}))?";

    /**
     * The path starts with a "/", and is followed by any number of PATH_CHARS characters and "/" path delimiters.
     * Optional.
     */
    private static final String PATH = "(/(%s)*)*".formatted(PATH_CHARS);

    /**
     * The query starts with a "?". It must be followed by at least one key-value assignment, and optionally more.
     * Each additional key-value assignment is prefaced by an "&" character.
     * Optional.
     */
    private static final String QUERY = "(\\?(((%s)+=(%<s)+)+(&(%<s)+=(%<s)+)*)+)?".formatted(URIC);

    /**
     * The fragment starts with a "#" character.
     * It can contain numbers, letters, and special characters as defined in the URIC.
     * Optional.
     */
    private static final String FRAGMENT = "(#(%s)*)?".formatted(URIC);


    /**
     * The compiled weblink pattern.
     * If the protocol field is present, the domain, port, path, query, and fragment fields can also be present.
     * Otherwise, only the domain and path are allowed. In this case, it is a web address.
     */
    private static final Pattern WEBLINK = Pattern.compile(
            "^" +
            PROTOCOL +
            DOMAIN +
            PORT +
            PATH +
            QUERY +
            FRAGMENT +
            "$|^" +
            DOMAIN +
            PATH +
            "$"
    );

    private WeblinkRegex() {}

    public static Pattern getWeblinkPattern() {
        return WEBLINK;
    }
}
