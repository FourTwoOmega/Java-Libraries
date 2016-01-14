package org.fto.net;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Generates a TLD set from file and supplies methods to work over this set.
 *
 * @author Ken Kauffman
 */
public class TLDList {
    private static final Logger log = LogManager.getLogger(TLDList.class);

    // Set of TLDs.
    private final Set<String> tldSet;

    /**
     * Given a file that already exists, loads a TLD list into memory.
     *
     * @param tldFile
     * @throws IOException
     */
    public TLDList(String tldFile) throws IOException {
        tldSet = loadTLDSet(tldFile);
    }

    /**
     * Get all tlds from a file. This file is assumed to contain either just a
     * static listing of TLDs or have comma separated values where the first of
     * which is the TLD.
     *
     * @param tldFile
     * @return
     * @throws IOException
     */
    private Set<String> loadTLDSet(String tldFile) throws IOException {
        Set<String> tldContainer = new HashSet<String>();
        for (String line : Files.readAllLines(Paths.get(tldFile))) {
            String tld = line;
            if (line.contains(",")) {
                tld = line.split(",")[0];
            }

            tldContainer.add(tld);
        }
        return tldContainer;
    }

    /**
     * Downloads and formats a TLD list from a URL source.
     *
     * @param url
     * @param toFile
     * @throws MalformedURLException
     */
    public static void CollectTLDSet(String url, String toFile) throws MalformedURLException, IOException {
        URL tldURL = new URL(url);
        File tldFile = new File(toFile);

        FileUtils.copyURLToFile(tldURL, tldFile);

        ArrayList<String> formattedFileText = new ArrayList<String>();

        for (String line : Files.readAllLines(tldFile.toPath())) {
            String formattedLine = line.toLowerCase();
            // If line starts with a commented encoded TLD...
            if (formattedLine.startsWith("// xn--")) {
                // ...trim encoded TLD out of comment and add to set.
                final Integer indexOfxndashdash = formattedLine.indexOf("xn--");
                if (formattedLine.indexOf(" (") > 0) {
                    formattedLine = formattedLine.substring(indexOfxndashdash, formattedLine.indexOf(" ("));
                } else if (formattedLine.indexOf(" :") > 0) {
                    formattedLine = formattedLine.substring(indexOfxndashdash, formattedLine.indexOf(" :"));
                } else {
                    log.warn("Ignoring unknown formatted line:\n" + formattedLine);
                }
                // If line starts with wildcard...
            } else if (formattedLine.startsWith("*.")) {
                // ...pull off wildcard and add to set.
                formattedLine = formattedLine.replace("*.", "");
                // If the line is not empty and is not a comment...
            } else if (formattedLine.startsWith("//")) {
                formattedLine = "";
            }

            if (!formattedLine.isEmpty()) {
                formattedFileText.add(formattedLine);
            }
        }

    }

    /**
     * Takes in a domain and returns its TLD.
     *
     * @author Ken Kauffman
     * @param domain The domain to parse for a TLD
     * @return The input domain's TLD
     * @throws MalformedURLException
     */
    public String getTLDFromDomain(String domain) throws MalformedURLException {
        String tld = domain;
        /**
         * Check to see if the current string stored in result is in our TLD
         * list.
         */
        while (!tld.isEmpty() && !tldSet.contains(tld)) {
            /**
             * Result did not match a TLD, so we try and remove one sub-domain
             * but moving the pointer to after the next period.
             */
            if (tld.contains(".")) {
                tld = tld.substring(tld.indexOf(".") + 1);
            } /**
             * If we've reached the end of the string (no more periods), then we
             * just set the result string to blank as there were no TLDs found
             * in the list.
             */
            else {
                tld = "";
            }
        }

        /**
         * At this point, the result should contain the TLD. If it does not
         * (empty), we assign it a TLD by taking the last section of the domain.
         */
        if (tld.isEmpty()) {
            tld = domain.substring(domain.lastIndexOf(".") + 1);
        }

        return tld;
    }
}
