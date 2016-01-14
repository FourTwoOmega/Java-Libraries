package org.fto.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Holds methods for operations on regular expressions
 *
 * @author Ken Kauffman
 */
final public class RegexMethods {

    /**
     * Evaluates a given String against a given regular expression
     * 
     * @author Ken Kauffman
     * @param str - The String to be evaluated against the given regular expression
     * @param regex - The regular expression against which the given String is to be evaluated
     * @return True if given String matches given regular expression; False if it does not
     */
    public static final Boolean evaluate(final String str, final String regex)
            throws PatternSyntaxException {
        final Boolean result;

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                result = true;
            } else {
                result = false;
            }
        } catch (PatternSyntaxException pse) {
            throw pse;
        }

        return result;
    }
}
