package nz.ac.canterbury.seng302.portfolio.service;

import java.util.regex.Pattern;

/**
 * The enum to register the options for regex checks on user input
 */
public enum RegexPattern {

    // Enum values defined

    /** Regex that is all unicode letters, numbers, punctuation, modifier/currency/math symbols and whitespace */
    GENERAL_UNICODE(Pattern.compile("[\\p{L}\\p{Nd}\\p{P}\\p{Sc}\\p{Sk}\\p{Sm}\\s]*", Pattern.CASE_INSENSITIVE),
            " can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace."),

    /** Regex that is all unicode letters, numbers, punctuation & modifier/currency/math symbols.
     * Intended for usernames and passwords */
    GENERAL_UNICODE_NO_SPACES(Pattern.compile("[\\p{L}\\p{Nd}\\p{P}\\p{Sc}\\p{Sk}\\p{Sm}]*", Pattern.CASE_INSENSITIVE),
            " can only contain letters, numbers, punctuation and symbols (but not emojis)."),

    /** Regex that is all unicode letters, punctuation, modifier symbols and whitespace.
     * Must include at least one letter. */
    FIRST_LAST_NAME(Pattern.compile("[\\p{L}\\p{P}\\s]*[\\p{L}]+[\\p{L}\\p{P}\\s]*", Pattern.CASE_INSENSITIVE),
            " must include at least one letter. Can also include punctuation and whitespace."),

   /** Regex that is all unicode letters, punctuation, modifier symbols and whitespace */
    MIDDLE_NAME(Pattern.compile("([\\p{L}\\p{P}\\s]*[\\p{L}]+[\\p{L}\\p{P}\\s]*)?", Pattern.CASE_INSENSITIVE),
           " must be empty or contain at least one letter. Can also include punctuation and whitespace."),

    /** Restricts to valid email format, e.g., example@email.com */
    EMAIL(Pattern.compile("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+$"),
            " must be of a valid email format, e.g. example@email.com."),

    /** Regex that is a valid hex colour code **/
    HEX_COLOUR(Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"), " must be a valid hex colour."),

    /** Regex to check the titles of occasions, this should be checked. */
    OCCASION_TITLE(Pattern.compile("(\\w+\\s?)+"), " can only contain letters, numbers and spaces and must not start with whitespace."),

    WEBLINK(WeblinkRegex.getWeblinkPattern(), " must be a valid URL."),

    /** Regex to check contains at least one letter. */
    SKILL(Pattern.compile("(\\p{P}\\p{L}\\p{Nl}\\p{Nd})*[A-Za-z]+(\\p{P}\\p{L}\\p{Nl}\\p{Nd})*"), " can only contain letters, numbers, and punctuation. Must contain at least one letter."),

    /** Regex to check access tokens for git repos added to groups */
    GITLAB_TOKEN(Pattern.compile("[A-Za-z0-9-_]{20}"),
            " should be a 20 character long string consisting of numbers, letters, hyphens, and underscores");

    // Enum attribute declaration

    /** The enums pattern attribute, the regex pattern used for checks.*/
    private final Pattern pattern;

    /** The string representation of the requirements for tool tips and error messages. */
    private final String requirements;


    /**
     * Required constructor to initialise the Enum values with their parameters.
     *
     * @param pattern - Declares the pattern attribute
     * @param requirements - Declares the requirements attribute
     */
    RegexPattern(Pattern pattern, String requirements) {
        this.pattern = pattern;
        this.requirements = requirements;
    }


    /**
     * Gets the pattern object used to validate input.
     *
     * @return The regex pattern for comparisons.
     */
    public Pattern getPattern() {
        return pattern;
    }


    /**
     * Gets the pattern string so that input can be validated on the frontend too.
     * Note this may say unused, this is because the value is retrieved in a thymeleaf th:pattern field
     * not used in Java
     *
     * @return the String that was compiled into the regex.
     */
    public String getPatternString() {
        return pattern.toString();
    }


    /**
     * Gets the requirements, used for error messages and tooltips.
     *
     * @return A string representing the requirements
     */
    public String getRequirements() {
        return requirements;
    }
}
