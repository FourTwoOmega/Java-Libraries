package org.fto.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.http.client.HttpResponseException;

public class HTTPResponses {

    private static Map<Integer, String> responseMessages = new HashMap<Integer, String>();
    static {
        // HTTP 1XX: Informational
        responseMessages.put(100, "Continue");
        responseMessages.put(101, "Switching Protocols");
        responseMessages.put(102, "Processing");

        // HTTP 2XX: Success
        responseMessages.put(200, "OK");
        responseMessages.put(201, "Created");
        responseMessages.put(202, "Accepted");
        responseMessages.put(203, "Non-Authoritative Information");
        responseMessages.put(204, "No Content");
        responseMessages.put(205, "Reset Content");
        responseMessages.put(206, "Partial Content");
        responseMessages.put(207, "Multi-Status");
        responseMessages.put(208, "Already Reported");
        responseMessages.put(226, "IM Used");

        // HTTP 3XX: Redirection
        responseMessages.put(300, "Multiple Choices");
        responseMessages.put(301, "Moved Permanently");
        responseMessages.put(302, "Found");
        responseMessages.put(303, "See Other");
        responseMessages.put(304, "Not Modified");
        responseMessages.put(305, "Use Proxy");
        responseMessages.put(306, "Switch Proxy");
        responseMessages.put(307, "Temporary Redirect");
        responseMessages.put(308, "Permanent Redirect");
        
        // HTTP 4XX: Error
        responseMessages.put(400, "Bad Request");
        responseMessages.put(401, "Unauthorized");
        responseMessages.put(402, "Payment Required");
        responseMessages.put(403, "Forbidden");
        responseMessages.put(404, "Not Found");
        responseMessages.put(405, "Method Not Allowed");
        responseMessages.put(406, "Not Acceptable");
        responseMessages.put(407, "Proxy Authentication Required");
        responseMessages.put(408, "Request Timeout");
        responseMessages.put(409, "Conflict");
        responseMessages.put(410, "Gone");
        responseMessages.put(411, "Length Required");
        responseMessages.put(412, "Precondition Failed");
        responseMessages.put(413, "Payload Too Large");
        responseMessages.put(414, "URI Too Long");
        responseMessages.put(415, "Unsupported Media Type");
        responseMessages.put(416, "Range Not Satisfiable");
        responseMessages.put(417, "Expectation Failed");
        responseMessages.put(418, "I'm a teapot");
        responseMessages.put(419, "Authentication Timeout");
        responseMessages.put(420, "Method Failure");
        responseMessages.put(421, "Misdirected Request");
        responseMessages.put(422, "Unprocessaable Entity");
        responseMessages.put(423, "Locked");
        responseMessages.put(424, "Failed Dependency");
        responseMessages.put(426, "Upgrade Required");
        responseMessages.put(428, "Precondition Required");
        responseMessages.put(429, "Too Many Requests");
        responseMessages.put(431, "Request Header Fields Too Large");
        responseMessages.put(440, "Login Timeout");
        responseMessages.put(444, "No Response");
        responseMessages.put(449, "Retry With");
        responseMessages.put(450, "Blocked by Windows Parental Controls");
        responseMessages.put(451, "Unavailable for Legal Reasons");
        responseMessages.put(494, "Request Header Too Large");
        responseMessages.put(495, "Cert Error");
        responseMessages.put(496, "No Cert");
        responseMessages.put(497, "HTTP to HTTPS");
        responseMessages.put(498, "Token Expired or Invalid");
        responseMessages.put(499, "Client Closed Request");
        
        // HTTP 5XX: Server Error
        responseMessages.put(500, "Internal Server Error");
        responseMessages.put(501, "Not Implemented");
        responseMessages.put(502, "Bad Gateway");
        responseMessages.put(503, "Service Unavailable");
        responseMessages.put(504, "Gateway Timeout");
        responseMessages.put(505, "HTTP Version Not Supported");
        responseMessages.put(506, "Variant Also Negotiates");
        responseMessages.put(507, "Insufficient Storage");
        responseMessages.put(508, "Loop Detected");
        responseMessages.put(509, "Bandwidth Limit Exceeded");
        responseMessages.put(510, "Not Extended");
        responseMessages.put(511, "Network Authentication Required");
        responseMessages.put(520, "Unknown Error");
        responseMessages.put(522, "Origin Connection Time-out");
        responseMessages.put(598, "Network Read Timeout Error");
        responseMessages.put(599, "Netowrk Connect Timeout Error");

        responseMessages = Collections.unmodifiableMap(responseMessages);
    }
    
    public static final String getResponseMessage(final Integer responseCode) {
        return responseMessages.get(responseCode);
    }
    
    /**
     * Checks if an HTTP response code is one of a provided allowable set of HTTP response codes
     * 
     * @author Ken Kauffman
     * @param responseCode - The response code to be checked
     * @param allowableCodes - The set of allowable HTTP response codes 
     * @return True if the response code is one of the set of allowable codes; false if not
     */
    public static final Boolean isAcceptableResponse(final Integer responseCode, final Set<Integer> allowableCodes) {
        Boolean result = false;

        for(Integer allowableCode : allowableCodes) {
            if(allowableCode.equals(responseCode)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Throws an exception if the given response code does not match a given set of acceptable response codes
     * 
     * @author Ken Kauffman
     * @param responseCode - The response code to be checked
     * @param responseMessage - The associated message of the response code (e.g. "OK" for HTTP 200)
     * @param allowableCodes - The set of allowable HTTP response codes 
     * @throws HttpResponseException if response code is not one of the allowable codes
     */
    public static final void checkForAcceptableResponse(final Integer responseCode, final Set<Integer> allowableCodes)
            throws HttpResponseException {
        // Check the code. If the code is not what was expected...
        if(!isAcceptableResponse(responseCode, allowableCodes)) {
            throw new HttpResponseException(responseCode, "Unexpected HTTP response code! Found \"" + responseCode.toString() + "/" + getResponseMessage(responseCode) + "\" instead.");
        }
    }

    /**
     * Returns the response body of a URLConnection object as a String
     * 
     * @author Ken Kauffman
     * @param conn - The URLConnection (or HttpURLConnection) object whose response body is desired
     * @return The response body as a String
     * @throws IOException if the BufferedReader object encounters issues
     */
    public static final String getResponseBody(final URLConnection conn) throws IOException {
        final BufferedReader response = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder tmp = new StringBuilder();
        String line;
        while ((line = response.readLine()) != null) {
            tmp.append(line);
        }
        response.close();

        return tmp.toString();
    }

    /**
     * Converts the key-value pair into an HTTP CGI argument
     * 
     * For cgiKey = "key", cgiValue = "cgi value", and initial = true, output will be the following
     * ?key=cgi%20value
     * 
     * For the same example, but initial = false, the output will be the following
     * &key=cgi%20value
     * 
     * @author Ken Kauffman
     * @param cgiKey - The key of the CGI argument
     * @param cgiValue - The value of the CGI argument
     * @param initial - Flag to indicate whether this will be the first CGI argument appended to a URL or not
     * @return The String containing the converted and percent-escaped CGI argument
     * @throws UnsupportedEncodingException
     */
    public static final String convertToCGIParam(final String cgiKey, final String cgiValue, final Boolean initial)
            throws UnsupportedEncodingException {
        
        return (initial ? "?" : "&") + cgiKey + "=" + URLEncoder.encode(cgiValue, "UTF-8");
    }

    /**
     * Converts the key-value pair into an HTTP CGI argument and appends to a provided URL String
     * 
     * @author Ken Kauffman
     * @param base - The URL String onto which to append the CGI argument
     * @param cgiKey - The key of the CGI argument
     * @param cgiValue - The value of the CGI argument
     * @return The URL String with the properly formatted CGI argument appended
     * @throws UnsupportedEncodingException
     */
    public static final String addCGIParam(final String base, final String cgiKey, final String cgiValue) throws UnsupportedEncodingException {
        final String result;
        final Boolean initial;

        try {
            // This regex is checking for an existing initial CGI argument
            final String regex = ".*?.*=.*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(base);
            if (matcher.matches()) {
                initial = false;
            } else {
                initial = true;
            }
        } catch (PatternSyntaxException pse) {
            throw pse;
        }

        result = base + convertToCGIParam(cgiKey, cgiValue, initial);

        return result;
    }

    /**
     * Converts the Map into a set of HTTP CGI arguments and appends them to a provided URL String
     * 
     * @author Ken Kauffman
     * @param base - The URL String onto which to append the CGI arguments
     * @param cgiParams - The Map containing the key-value pairs of CGI arguments to be appended onto the provided URL String
     * @return The URL String with the properly formatted CGI arguments appended
     * @throws UnsupportedEncodingException
     */
    public static final String addCGIParams(final String base, final Map<String, String> cgiParams)
            throws UnsupportedEncodingException {
        String result = base;

        for(Map.Entry<String, String> cgiArg : cgiParams.entrySet()) {
            result = addCGIParam(result, cgiArg.getKey(), cgiArg.getValue());
        }

        return result;
    }
}
