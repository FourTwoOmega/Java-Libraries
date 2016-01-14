package org.fto.files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.apache.commons.io.FileUtils;

/**
 * Contains a Trie to be used with the Aho-Corasick lookup algorithm. Methods to
 * interface with the dictionary have been simplified through this class.
 *
 * @author Ken Kauffman
 *
 */
public class DictionaryFile {

    // The original file from whence the newline-separated dictionary came
    final private Path file;

    // The dictionary where the data from the newline-separated dictionary file is stored
    final private Trie dictionary;

    /**
     * Constructor for DictionaryFile
     *
     * @author Ken Kauffman
     * @param textFile The path to the text file containing the desired
     * newline-separated dictionary
     * @throws IOException
     */
    public DictionaryFile(final Path textFile) throws IOException {
        this.file = textFile;
        this.dictionary = setDictionary(this.file);
    }

    /**
     * Constructor for DictionaryFile
     *
     * @author Ken Kauffman
     * @param url The URL from which to download the desired newline-separated
     * dictionary
     * @param downloadDir The directory to which to download the
     * newline-separated dictionary
     * @throws IOException
     * @throws URISyntaxException
     */
    public DictionaryFile(final URL url, final Path downloadDir) throws IOException, URISyntaxException {
        final String urlFileName = url.getFile();
        final Integer lastSlash = urlFileName.lastIndexOf("/");
        final Integer nameLength = urlFileName.length();
        final String downloadFileName = urlFileName.substring(lastSlash, nameLength);
        final Path fullDownloadPath = Paths.get(downloadDir.toAbsolutePath().toString(), downloadFileName);

        this.file = downloadFile(url, fullDownloadPath);
        this.dictionary = setDictionary(this.file);
    }

    /**
     * Downloads a file from a URL to the specified path
     *
     * @author Ken Kauffman
     * @param url The URL pointing to a newline-separated dictionary file
     * @param destFile The location to which to download the data at the URL
     * @return For convenience, the file passed into this method is returned
     * @throws IOException
     */
    final public static Path downloadFile(final URL url, final Path destFile) throws IOException {
        FileUtils.copyURLToFile(url, destFile.toFile());

        if (!Files.exists(destFile)) {
            throw new FileNotFoundException("File failed to download.");
        }

        return destFile;
    }

    /**
     * Reads in the newline-separated file into the Trie dictionary object
     *
     * @author Ken Kauffman
     * @param textFile The newline-separated file to be read into a Trie object
     * @return The Trie object containing the dictionary from the input file
     * @throws IOException
     */
    final private Trie setDictionary(final Path textFile) throws IOException {
        final Trie result;

        // The ".onlyWholeWords()" prevents partial matches.
        // For example, without this call, "sugarcane" would match "sugar".
        TrieBuilder t = Trie.builder().removeOverlaps().caseInsensitive();
        // TODO: Implement a method that returns results with every character used, if possible (deprecates
        //+ removeOverlaps() for our purposes)
        // Read the newline-separated file into the Trie object
        for (String line : Files.readAllLines(textFile)) {
            t.addKeyword(line);
        }
        result = t.build();
        return result;
    }

    /**
     * Searches the dictionary for a specified word, and only returns values
     * greater than a specified length
     *
     * @author Ken Kauffman
     * @param word The String with which to query the dictionary
     * @param minLength The minimum length of search results
     * @return The list of search results meeting the desired parameters
     */
    final public List<String> query(final String word, final Integer minLength)
    		throws IllegalArgumentException {
    	// Argument checking
    	if(minLength < 0) {
    		throw new IllegalArgumentException("Must specify a non-negative integer! Found: " + minLength);
    	}

        List<String> results = new ArrayList<String>();

        // Search dictionary and store results
        final Collection<Emit> emitMatches = this.dictionary.parseText(word);

        // Iterate through all trimmed emit matches
        for (Emit emittedMatch : emitMatches) {
            // If the trimmed match is greater than or equal to the minimum required length...
            if (emittedMatch.size() >= minLength) {
                // ...add it to the results ArrayList.
                results.add(emittedMatch.getKeyword());
            }
        }
        return results;
    }

    /**
     * Searches the dictionary for a specified word
     *
     * @author Ken Kauffman
     * @param word The String with which to query the dictionary
     * @return The list of search results
     */
    final public List<String> query(final String word) {
        return query(word, 0);
    }

    /**
     * Returns the file used to instantiate this class
     *
     * @author Ken Kauffman
     * @return The file used to instantiate this class as a Path object
     */
    final public Path toPath() {
        return this.file;
    }

    /**
     * Returns the Trie object used by this class
     *
     * @author Ken Kauffman
     * @return The Trie object used by this class
     */
    final public Trie toTrie() {
        return this.dictionary;
    }
}
