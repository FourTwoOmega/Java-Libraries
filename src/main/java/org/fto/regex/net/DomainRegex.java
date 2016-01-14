package org.fto.regex.net;

import org.fto.regex.RegexMethods;

/**
 * Holds regular expressions related to domains
 *
 * @author Ken Kauffman
 */
public class DomainRegex {
    
    // Maximum possible length of domain
    public static final Integer MAX_DOMAIN_LENGTH = 253;

    // TODO: The only thing the Subdomain Regex does not catch that it should is 2 or more consecutive dots in the middle
    public static final String SUBDOMAIN_REGEX      = "[\\w\\-\\.]*[^\\.][\\.]";	// Also accounts for www.
    public static final String DOMAIN_NAME_REGEX    = "[A-Za-z0-9\\-]{1,63}";
    // TODO: Create an exhaustive list of all valid ICANN-registered TLDs
    public static final String DOMAIN_TLD_REGEX     = "\\.(xn--[A-Za-z0-9]{1,59}|[A-Za-z]{2,63})";

    public static final String DOMAIN_REGEX = "(" + SUBDOMAIN_REGEX + ")?(" + DOMAIN_NAME_REGEX + ")" + DOMAIN_TLD_REGEX;

    /**
     * Utilizes a regular expression to indicate whether the given string is a valid domain
     * 
     * @author Ken Kauffman
     * @param str - The String object to be tested
     * @return Boolean value: true if input is valid domain, false if not
     */
    public static final Boolean isDomain(final String str) {
        return RegexMethods.evaluate(str, DOMAIN_REGEX);
    }
}
