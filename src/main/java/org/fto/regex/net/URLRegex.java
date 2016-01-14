package org.fto.regex.net;

import org.fto.regex.RegexMethods;
import org.fto.regex.net.DomainRegex;
import org.fto.regex.net.IPRegex;

/**
 * Holds regular expressions related to URLs
 *
 * @author Ken Kauffman
 */
public class URLRegex {
    // Any integer from 1 to 65535
    public static final String INT_1_TO_65535_REGEX = "6553[0-5]|655[0-2]\\d|65[0-4]\\d\\d|6[0-4]\\d\\d\\d|[0-5]?\\d?\\d?\\d?\\d";

    public static final String IP_OR_DOMAIN_REGEX = "((" + IPRegex.IP_REGEX + ")|(" + DomainRegex.DOMAIN_REGEX + "))";

    // URL_OTHER_SCHEMES_REGEX Allows for schemes that are not http(s) or ftp(s)
    // The list of valid registered IANA schemes is very long, so this regex
    //+ will contain a list of those more commonly encountered.
    private static final String OTHER_SCHEMES_REGEX     = "[Rr][Tt][Mm][Pp]";
    public static final String HTTP_OR_FTP_REGEX        = "([Hh][Tt]|[Ff])[Tt][Pp][Ss]?";
    public static final String COLON_SLASH_SLASH_REGEX  = "(:|\\%3[Aa])(\\/|\\%2[Ff])(\\/|\\%2[Ff])";

    private static final String ALL_SCHEMES_REGEX = "((" + HTTP_OR_FTP_REGEX + ")|(" + OTHER_SCHEMES_REGEX + "))" + COLON_SLASH_SLASH_REGEX;

    public static final String TCP_PORT_NUMBER_REGEX       = "(\\:|\\%3[Aa])" + "(" + INT_1_TO_65535_REGEX + ")";
    public static final String VALID_CHAR_REGEX            = "[\\w\\-.`~\\|!*\'(){}<>;:@&=+$,\\/?%#\\[\\]\\ ]";
    public static final String ENCODED_CHAR_REGEX          = "\\%(0[89AaDd]|[2-6CcEeFf][0-9A-Fa-f]|" +
										                     "7[0-9A-Ea-e]|[Aa][23567BCDbcd]|[Bb][01245BCDFbcdf]|[Dd][0-689A-Fa-f])";
    public static final String VALID_CHARS_ENCODED_AND_NOT = "(" + VALID_CHAR_REGEX + ")|(" + ENCODED_CHAR_REGEX + ")";
    public static final String SLASH_REGEX                 = "(\\/|\\%2F)";
    public static final String HASH_SIGN_REGEX             = "(\\#|\\%23)";

    public static final String PAGE_REGEX          = SLASH_REGEX + "(" + VALID_CHARS_ENCODED_AND_NOT + ")*";
    public static final String PAGE_ANCHOR_REGEX   = HASH_SIGN_REGEX + "(" + VALID_CHARS_ENCODED_AND_NOT + ")*";

    public static final String PAGE_OR_ANCHOR_REGEX = "(" + PAGE_REGEX + ")|(" + PAGE_ANCHOR_REGEX + ")";

    public static final String URL_REGEX = "(" + ALL_SCHEMES_REGEX      + ")?" +    // http(s)/ftp(s)/etc + ://	[OPTIONAL]
                                                 IP_OR_DOMAIN_REGEX     +           // (subdomain.)domain.tld	[REQUIRED]
                                           "(" + TCP_PORT_NUMBER_REGEX  + ")?" +    // :65535 (port number)		[OPTIONAL]
                                           "(" + PAGE_OR_ANCHOR_REGEX   + ")?" +    // /path/to/page.html		[OPTIONAL]
                                           "(" + SLASH_REGEX+ ")?";                 // Trailing forward slash	[OPTIONAL]
    
    /**
     * Utilizes a regular expression to indicate whether the given string is a valid URL
     * 
     * @author Ken Kauffman
     * @param str - The String object to be tested
     * @return Boolean value: true if input is valid URL, false if not
     */
    public static final Boolean isURL(final String str) {
        return RegexMethods.evaluate(str, URL_REGEX);
    }
}
