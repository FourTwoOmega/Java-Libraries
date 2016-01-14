package org.fto.regex.net;

import org.fto.regex.RegexMethods;

/**
 * Holds regular expressions related to IP addresses
 *
 * @author Ken Kauffman
 */
final public class IPRegex {

    // Any integer from 0 to 255
    public static final String INT_ZERO_TO_255_REGEX = "25[0-5]|2[0-4]\\d|[01]?\\d?\\d";

    // 0.0.0.0 to 255.255.255.255
    public static final String IPV4_REGEX = "(" + INT_ZERO_TO_255_REGEX + ")\\." +
                                            "(" + INT_ZERO_TO_255_REGEX + ")\\." +
                                            "(" + INT_ZERO_TO_255_REGEX + ")\\." +
                                            "(" + INT_ZERO_TO_255_REGEX + ")";
    
    // TODO: Implement regex for IPv6 addresses
    private static final String IPV6_REGEX = null;
    
    // TODO: Change when IPV6_REGEX is implemented
//    public static final String IP_REGEX = "((" + IPV4_REGEX + ")|(" + IPV6_REGEX + "))";
    public static final String IP_REGEX = IPV4_REGEX;

    /**
     * Utilizes a regular expression to indicate whether the given string is a valid IPv4 IP
     * 
     * @author Ken Kauffman
     * @param str - The String object to be tested
     * @return Boolean value: true if input is valid IPv4, false if not
     */
    public static final Boolean isIPv4(final String str) {
        return RegexMethods.evaluate(str, IPV4_REGEX);
    }

    /**
     * Utilizes a regular expression to indicate whether the given string is a valid IPv6 IP
     * 
     * @author Ken Kauffman
     * @param str - The String object to be tested
     * @return Boolean value: true if input is valid IPv6, false if not
     */
    // TODO: Make public when IPV6_REGEX is implemented and remove @SuppressWarnings
    @SuppressWarnings("unused")
    private static final Boolean isIPv6(final String str) {
        return RegexMethods.evaluate(str, IPV6_REGEX);
    }
    
    /**
     * Utilizes a regular expression to indicate whether the given string is a valid IP
     * 
     * @author Ken Kauffman
     * @param str - The String object to be tested
     * @return Boolean value: true if input is valid IP, false if not
     */
    // TODO: Make public when IPV6_REGEX is implemented and remove @SuppressWarnings
    @SuppressWarnings("unused")
    private static final Boolean isIP(final String str) {
        return RegexMethods.evaluate(str, IP_REGEX);
    }
}
