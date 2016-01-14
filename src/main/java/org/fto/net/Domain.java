package org.fto.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.fto.regex.net.IPRegex;

/**
 * Holds analysis information about a domain; a domain in this case can have a
 * subdomain prepended or it can be an IP address
 *
 * @author Ken Kauffman
 */
public class Domain {
    private static final Logger log = LogManager.getLogger(Domain.class);

    // URL source from whence URL came
    private final String source;

    // URL from whence domain came
    private final URL url;

    // Domain String derived from URL
    private final String domain;

    // Flag to indicate whether this domain is really an IP
    private final Boolean domainIsIP;

    // Flag to indicate whether this domain is really an effective TLD
    private final Boolean domainIsTLD;

    // Top-Level Domain of full Domain
    private final String TLD;

    // Mid-Level Domain of full Domain
    private final String MLD;

    // Subdomain of full Domain
    private final String subDomain;

    // Results of analyses
    private final Map<String, Integer> analysisData;

    // Hosting site if any
    private final String host;

    // Type of hosting site, if any.
    private final String hostType;

    // Underlying IP address of the domain. This will match the domain if it is already an IP address.
    private final String ipAddress;

    // Establishes whether this is production or test. This is used to limit lengthy operations during test.
    private final boolean collectIPAddress;

    /**
     * Constructor for Domain; this allows both a URL source and a url to be
     * specified
     *
     * @author Ken Kauffman
     * @param source URL source
     * @param url URL or domain to store
     * @param tldList Object containing a set of valid TLDs
     * @param host If this domain is attached to a hosting site, this is the
     * hosting site.
     * @param hostType Identifies the type of hosting site if applicable.
     * @param collectIPAddress
     * @throws MalformedURLException
     */
    public Domain(final String source, final String url, final TLDList tldList, final String host, final String hostType, final boolean collectIPAddress) throws MalformedURLException {
        this.collectIPAddress = collectIPAddress;

        // Source or list from whence the URL came
        this.source = source;

        // Store original URL for later analysis
        this.url = this.setURL(url.toLowerCase().trim());

        // Convert URL to domain (without www.) upon instantiation
        this.domain = getDomainFromURL();

        // Check if domain is an IP
        this.domainIsIP = IPRegex.isIPv4(this.domain);

        // Pull out TLD (Top-Level Domain) for easier analysis later. Do not do this for IPs.
        if (!domainIsIP) {
            this.TLD = tldList.getTLDFromDomain(domain);
        } else {
            this.TLD = "";
        }

        // Check if domain is a TLD (or effective TLD)
        this.domainIsTLD = isDomainTLD();

        // Pull out MLD (Mid-Level Domain) for easier analysis later
        this.MLD = getMLDFromDomain();

        // Pull out sub-domain for easier analysis later
        this.subDomain = getSubDomainFromDomain();

        // Initialize analysis data; data will be added later
        this.analysisData = new LinkedHashMap<String, Integer>();

        this.host = host;
        this.hostType = hostType;

        this.ipAddress = getIPAddressFromDomain();
    }

    /**
     * Returns the IP address that is associated with the specific domain. In
     * the case that the domain is an IP, we skip this function and just set the
     * IP to the domain.
     *
     * If the IP failed to be gathered, the returned string will by empty.
     *
     * @return
     */
    private String getIPAddressFromDomain() {
        String hostIP = "";

        // Only collect IPS if this is a production run. The process is too lengthy for test runs.
        if (collectIPAddress) {
            if (domainIsIP) {
                hostIP = domain;
            } else {
                try {
                    hostIP = java.net.InetAddress.getByName(getURLHost()).getHostAddress();
                } catch (UnknownHostException ex) {
                    log.warn("Could not get an IP address for: " + domain);
                }
            }
        }
        return hostIP;
    }

    /**
     * Constructor for Domain; this allows both a URL source and a url to be
     * specified
     *
     * Assumes host data is empty and that this is a production run.
     *
     * @author Ken Kauffman
     * @param source URL source
     * @param url URL or domain to store
     * @param tldList Object containing a set of valid TLDs
     * @throws MalformedURLException
     */
    public Domain(final String source, final String url, final TLDList tldList) throws MalformedURLException {
        this(source, url, tldList, "", "", true);
    }

    /**
     * Constructor for Domain
     *
     * Assumes source and host data is empty and that this is a production run.
     *
     * @author Ken Kauffman
     * @param url URL or domain to store
     * @param tldList Object containing a set of valid TLDs
     */
    public Domain(final String url, final TLDList tldList) throws MalformedURLException {
        this("", url, tldList);
    }

    /**
     * Sets the url private class variable
     *
     * @author Ken Kauffman
     * @param str Incoming URL or domain
     * @return URL object made from input
     * @throws MalformedURLException
     */
    final private URL setURL(final String str) throws MalformedURLException {
        final URL result;

        // A URL object must contain a protocol or it throws a MalformedURLException
        if (str.contains("://")) {
            result = new URL(str);
        } else {
            result = new URL("http://" + str);
        }

        return result;
    }

    /**
     * Takes URL from private class variable and returns the domain (with www.
     * removed)
     *
     * @author Ken Kauffman
     * @return The domain from the input URL, including subdomain
     * @throws MalformedURLException
     */
    final private String getDomainFromURL() throws MalformedURLException {
        final String result;

        // If the domain contains an "@" character, getURLHost() will return an empty String
        final String urlHost = this.getURLHost();

        if (urlHost.contains("www.")) {
            final Integer indexOfWwwDot = urlHost.indexOf("www.");
            result = urlHost.substring(indexOfWwwDot);
        } else {
            result = urlHost;
        }

        this.validDomainCheck(result);
        return result;
    }

    /**
     * Ensures the given domain is valid
     *
     * @author Ken Kauffman
     * @param domain The domain to be checked
     * @throws MalformedURLException
     */
    final private void validDomainCheck(final String domain) throws MalformedURLException {
        // TODO: Currently, this just checks for a dot character. This can be improved at some point.
        try {
        	if(domain.isEmpty()) {
        		throw new MalformedURLException("Domain is empty!");
        	}
        	if(!domain.contains(".")) {
        		throw new MalformedURLException("Domain does not contain a dot character. String: \"" + domain + "\"");
        	}
        } catch (AssertionError ae) {
            throw new MalformedURLException(ae.getMessage());
        } catch (NullPointerException npe) {
            throw new MalformedURLException("Domain is null!");
        }
    }

    /**
     * Checks if the domain is an effective TLD
     *
     * @author Ken Kauffman
     * @return True if the domain is an effective TLD
     */
    final private Boolean isDomainTLD() {
        final Boolean result;
        if (domain.equals(TLD)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * Takes in a domain and returns its MLD
     *
     * @author Ken Kauffman
     * @return The input domain's MLD
     */
    final private String getMLDFromDomain() {
        final String result;

        // String result will be empty if this object stores an IP or effective TLD
        if (!domainIsIP && !domainIsTLD) {
            int lengthWithoutTLD = domain.length() - (TLD.length() + 1);
            String domainWithoutTLD = "";
            if (domain.length() > lengthWithoutTLD) {
                domainWithoutTLD = domain.substring(0, lengthWithoutTLD);
            }

            // If the domain minus the TLD still contains a dot...
            if (domainWithoutTLD.contains(".")) {
                // ...there is at least 1 subdomain to remove, so remove them all.
                final Integer indexOfLastDot = domainWithoutTLD.lastIndexOf(".") + 1;
                result = domainWithoutTLD.substring(indexOfLastDot, domainWithoutTLD.length());
            } else {
                // Otherwise, we have our MLD.
                result = domainWithoutTLD;
            }
        } else {
            result = "";
        }

        return result;
    }

    /**
     * Takes in a domain and returns is subdomain
     *
     * @author Ken Kauffman
     * @return The domain's subdomain(s) all as a single String; if no
     * subdomain, an empty String is returned
     */
    final private String getSubDomainFromDomain() {
        String result = "";

        // String result will be empty if this object stores an IP, effective TLD, or has no subdomain
        if (!domainIsIP && !domainIsTLD && !domain.startsWith(MLD)) {
            // Subdomain is everything before MLD
            if (!domain.startsWith(MLD)) {
                int MLDIndex = domain.indexOf("." + MLD);
                result = domain.substring(0, MLDIndex);
            }
        } else {
            result = "";
        }

        return result;
    }

    /**
     * Returns the URL source from the URL used to instantiate this class
     *
     * @author Ken Kauffman
     * @return URL source from private class variable
     */
    final public String getSource() {
        return this.source;
    }

    /**
     * Returns the URL used to instantiate this class
     *
     * @author Ken Kauffman
     * @return URL from private class variable
     */
    final public String getURL() {
        return this.url.toString();
    }

    /**
     * Returns "authority" from original URL; this is the subdomain, host, and
     * port number
     *
     * @author Ken Kauffman
     * @return Authority from private class variable of type java.net.URL
     */
    final public String getAuthority() {
        return this.url.getAuthority();
    }

    /**
     * Returns protocol from original URL (http, https, ftp, etc.)
     *
     * @author Ken Kauffman
     * @return Protocol from private class variable of type java.net.URL
     */
    final public String getProtocol() {
        return this.url.getProtocol();
    }

    /**
     * Returns host from original URL; this includes the subdomain and "www.",
     * if applicable. Use getDomain() if "www." is not desired.
     *
     * @author Ken Kauffman
     * @return Protocol from private class variable of type java.net.URL
     */
    final public String getURLHost() {
        return this.url.getHost();
    }

    /**
     * Returns the domain derived from the URL used to instantiate this class
     *
     * @author Ken Kauffman
     * @return Domain from private class variable
     */
    final public String getDomain() {
        return this.domain;
    }

    /**
     * Returns port number from original URL
     *
     * @author Ken Kauffman
     * @return Port number from private class variable of type java.net.URL
     */
    final public Integer getPort() {
        return this.url.getPort();
    }

    /**
     * Returns default port number based on protocol from original URL
     *
     * @author Ken Kauffman
     * @return Default port number of protocol of URL from private class
     * variable of type java.net.URL
     */
    final public Integer getDefaultPort() {
        return this.url.getDefaultPort();
    }

    /**
     * Returns path from original URL (everything after the domain and port)
     *
     * @author Ken Kauffman
     * @return Path from private class variable of type java.net.URL
     */
    final public String getPath() {
        return this.url.getPath();
    }

    /**
     * Returns file name from original URL
     *
     * @author Ken Kauffman
     * @return File name from private class variable of type java.net.URL
     */
    final public String getFileName() {
        return this.url.getFile();
    }

    /**
     * Indicates whether the domain held by this class is an IP
     *
     * @author Ken Kauffman
     * @return Indication as to whether the domain held by this class is an IP
     * as a Boolean
     */
    final public Boolean isIP() {
        return this.domainIsIP;
    }

    /**
     * Indicates whether the domain held by this class is a TLD or an effective
     * TLD
     *
     * @author Ken Kauffman
     * @return Indication as to whether the domain held by this class is a TLD
     * (or an effective TLD) as a Boolean
     */
    final public Boolean isTLD() {
        return this.domainIsTLD;
    }

    /**
     * Returns the subdomain derived from the domain
     *
     * @author Ken Kauffman
     * @return Subdomain from private class variable
     */
    final public String getSubdomain() {
        return this.subDomain;
    }

    /**
     * Returns the MLD derived from the domain
     *
     * @author Ken Kauffman
     * @return MLD from private class variable
     */
    final public String getMLD() {
        return this.MLD;
    }

    /**
     * Returns the TLD derived from the domain
     *
     * @author Ken Kauffman
     * @return TLD from private class variable
     */
    final public String getTLD() {
        return this.TLD;
    }

    /**
     * Returns whether the host is a whitelisted or blacklisted host domain
     *
     * @author Travis Williams
     * @return Host type from private class variable
     */
    public String getHostType() {
        return hostType;
    }

    /**
     * Returns the whitelisted or blacklisted host domain
     *
     * @author Travis Williams
     * @return Host from private class variable
     */
    public String getHost() {
        return host;
    }

    /**
     * If the subDomain is empty, returns the MLD. If the subDomain is not
     * empty, returns 'subDomain.MLD'
     *
     * @return
     */
    public String getDomainWithoutTLD() {
        if (subDomain.isEmpty()) {
            return getMLD();
        } else {
            return getSubdomain() + "." + getMLD();
        }
    }

    /**
     * Returns the smallest domain that we want to block without blocking a
     * blacklisted or whitelisted host domain
     *
     * @author Travis Williams
     * @return Host from private class variable
     */
    public String getBlockableDomain() {
        // In the basic case, we want to block the MLD.TLD combo.
        String blockableDomain = MLD + "." + TLD;

        // If this domain is a host, we want to instead block the next level upwards i.e. (last section of subDomain).MLD.TLD
        if (!host.isEmpty() && blockableDomain.equalsIgnoreCase(host) && !subDomain.isEmpty()) {
            blockableDomain = subDomain.substring(subDomain.lastIndexOf(".") + 1) + "." + blockableDomain;
        }

        return blockableDomain;
    }

    /**
     * Returns the LinkedHashMap containing the analysis data
     *
     * @author Ken Kauffman
     * @param key The name of the analysis criterion
     * @return Analysis datum from private class variable
     */
    final public synchronized Integer getAnalysisDatum(final String key) {
        return this.analysisData.get(key);
    }

    /**
     * Adds a key-value pair to the analysis data LinkedHashMap
     *
     * @author Ken Kauffman
     * @param key The name of the analysis criterion
     * @param value The value of the analysis criterion
     */
    final public synchronized void setAnalysisDatum(final String key, final Integer value) {
        analysisData.put(key, value);
    }

    /**
     * Generic hashcode generated using the domain string.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.domain);
        return hash;
    }

    /**
     * Generic equals generated using the domain string.
     *
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Domain other = (Domain) obj;
        if (!Objects.equals(this.domain, other.domain)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the IP address (or empty if it couldn't be found) of the domain.
     *
     * @return
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Allows the user to request the sub domain from a specific position.
     *
     * We assume that, when calling this function with a '1', the user is asking
     * for the 'first' sub domain. To that end, the functionality is modified to
     * fit that logic.
     *
     * @return
     */
    public String getNthSubDomain(int position) {
        String nthSubdomain = "";
        /**
         * Check to see if we are asking for a sub domain that even exists.
         */
        if (getNumSubDomains() >= position) {
            String[] splitSubDomains = subDomain.split("\\.");

            /**
             * This handles re-mapping the array to start at '1' rather than '0'
             * for the first element. This also handles inverting the indexes to
             * read more logically for this scenario rather than how arrays
             * usually work.
             */
            int logicalPosition = splitSubDomains.length - position;

            /**
             * Ensures we are still bounded by the array indexes.
             */
            if (logicalPosition >= 0 && logicalPosition < splitSubDomains.length) {
                nthSubdomain = splitSubDomains[logicalPosition];
            }
        }
        return nthSubdomain;
    }

    /**
     * Get number of sub domains.
     *
     * @return
     */
    public int getNumSubDomains() {
        if (subDomain.isEmpty()) {
            return 0;
        } else if (subDomain.contains(".")) {
            /**
             * Count the number of periods that exist and add 1. This is the
             * number of sub domains.
             */
            return StringUtils.countMatches(subDomain, ".") + 1;
        } else {
            /**
             * Since the sub domain is not empty but doesn't contain a period,
             * we know that there is only 1 sub domain.
             */
            return 1;
        }
    }

    /**
     * Return full map of analysis data.
     *
     * @return
     */
    public synchronized Map<String, Integer> getAnalysisData() {
        return analysisData;
    }
}
