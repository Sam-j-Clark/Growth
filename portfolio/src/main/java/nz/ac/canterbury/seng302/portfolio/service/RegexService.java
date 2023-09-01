package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.CheckException;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

/**
 * The service bean responsible for validating inputs through a regex
 */
@Service
public class RegexService {


    /**
     * Checks the given input against the general allowed characters regex
     *
     * @param pattern The regex pattern the string will be checked against
     * @param valueToCheck The string input by the user to be checked against the regex
     * @param minLength The min length of the input after trimming occurs, set to 0 if no min length
     * @param maxLength The max length of the input after trimming occurs
     * @param inputName The name of the input for specific error messages
     */
    public void checkInput(RegexPattern pattern, String valueToCheck, Integer minLength, Integer maxLength, String inputName) {
        if (valueToCheck == null) {
            if (minLength == 0) {
                return;
            } else {
                throw new CheckException("Required field " + inputName + " is missing");
            }
        }
        String trimmedString = valueToCheck.trim();
        checkMinLength(trimmedString, minLength, inputName);
        checkMaxLength(trimmedString, maxLength, inputName);
        Matcher matcher = pattern.getPattern().matcher(trimmedString);
        if (! matcher.matches())  {
            throw new CheckException(inputName + pattern.getRequirements());
        }
    }


    /**
     * Helper function to check the input string meets the minimum length requirements
     *
     * @param valueToCheck - The string to check the length of
     * @param minLength - An integer value that is the min length of the string
     * @param inputName - The name of the input i.e, sprint description
     */
    private void checkMinLength(String valueToCheck, Integer minLength, String inputName) {
        if (valueToCheck.length() < minLength) {
            throw new CheckException(inputName + " is shorter than the minimum length of " +
                    minLength +
                    (minLength > 1 ? " characters" : " character"));
        }
    }


    /**
     * Helper function to check the input string meets the maximum length requirements.
     *
     *  @param valueToCheck - The string to check the length of
     * @param maxLength - An integer value that is the max length of the string
     * @param inputName - The name of the input i.e, sprint description
     */
    private void checkMaxLength(String valueToCheck, Integer maxLength, String inputName) {
        if (valueToCheck.length() > maxLength) {
            throw new CheckException(inputName + " is longer than the maximum length of " + maxLength + " characters");
        }
    }

}
