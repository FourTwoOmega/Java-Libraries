package org.fto.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Functions as a wrapper for Apache's Commons CSV classes so user does not need to import them and to simplify and
 * standardize CSV files.
 * 
 * @author Ken Kauffman
 */
public class CSVFile {
    // CSV_CONVENTION must be one of the CSVFormat types.
    private final static CSVFormat CSV_CONVENTION = CSVFormat.RFC4180;

    public final static String RECORD_SEPARATOR = CSV_CONVENTION.getRecordSeparator();
    public final static char DELIMITER_CHAR = CSV_CONVENTION.getDelimiter();
    public final static String DELIMITER = Character.toString(DELIMITER_CHAR);

    private final File CSV_FILE;
    private final List<String> CSV_HEADER;
    private final CSVFormat CSV_FORMAT;
    private final CSVParser CSV_PARSER;
    private final CSVPrinter CSV_PRINTER;

    /**
     * Constructor for CSVFile; creates the specified file if it does not exist.
     * 
     * @author Ken Kauffman
     * @param fileLocation - Location or desired location of the CSV file
     * @param csvHeader - The header for the CSV file (required)
     * @throws FileNotFoundException if file is not there upon creation of FileReader or FileWriter objects
     * @throws IOException for various file-related reasons
     */
    public CSVFile(final File fileLocation, final List<String> csvHeader)
            throws FileNotFoundException, IOException {
        // Create the specified file if it does not already exist
        if(!Files.exists(fileLocation.toPath())) {
            Files.createFile(fileLocation.toPath());
        }

        CSV_FILE = fileLocation;
        CSV_HEADER = Collections.unmodifiableList(csvHeader);
        CSV_FORMAT = CSV_CONVENTION.withHeader(CSV_HEADER.toArray(new String[CSV_HEADER.size()]));
        CSV_PARSER = new CSVParser(new FileReader(CSV_FILE), CSV_FORMAT);
        CSV_PRINTER = new CSVPrinter(new FileWriter(CSV_FILE), CSV_FORMAT);
    }

    /**
     * Constructor for CSVFile; allows passing of CSV header as String array instead of List of String objects
     * 
     * @author Ken Kauffman
     * @param fileLocation - Location or desired location of the CSV file
     * @param csvHeader - The header for the CSV file (required)
     * @throws FileNotFoundException if file is not there upon creation of FileReader or FileWriter objects
     * @throws IOException for various file-related reasons
     */
    public CSVFile(final File fileLocation, final String[] csvHeader)
            throws FileNotFoundException, IOException {
        this(fileLocation, Arrays.asList(csvHeader));
    }

    /**
     * Constructor for CSVFile; allows passing of CSV header as delimited String object instead of a List or array
     * 
     * @author Ken Kauffman
     * @param fileLocation - Location or desired location of the CSV file
     * @param csvHeader - The header for the CSV file (required) as comma-delimited String
     * @throws FileNotFoundException if file is not there upon creation of FileReader or FileWriter objects
     * @throws IOException for various file-related reasons
     */
    public CSVFile(final File fileLocation, final String csvHeader)
            throws FileNotFoundException, IOException {
        this(fileLocation, csvHeader.split(DELIMITER));
    }

    /**
     * Constructor for CSVFile; omits the passing of CSV header and assumes that the specified file already has one
     * 
     * @author Ken Kauffman
     * @param fileLocation - Location of the CSV file that already contains a header; first row is assumed to be the header
     * @throws IOException for various file-related reasons
     */
    public CSVFile(final File fileLocation) throws IOException {
        this(fileLocation, readCSVHeader(fileLocation));
    }

    /**
     * Reads in the CSV header from a specified CSV file
     * 
     * @author Ken Kauffman
     * @param csvFile - File whose header is to be returned
     * @return The CSV header as a List of String objects
     * @throws IOException if file does not exist at time of reading
     */
    public static final List<String> readCSVHeader(final File csvFile) throws IOException {
        List<String> result;

        CSVParser csvParser = new CSVParser(new FileReader(csvFile), CSV_CONVENTION);
        result = new ArrayList<String>(csvParser.getHeaderMap().keySet());

        csvParser.close();

        return result;
    }

    /**
     * Reads in the CSV header from the file used to instantiate this class
     * 
     * @author Ken Kauffman
     * @return The CSV header as a List of String objects
     * @throws IOException if file does not exist at time of reading
     */
    public final List<String> readCSVHeader() throws IOException {
        return new ArrayList<String>(CSV_PARSER.getHeaderMap().keySet());
    }

    /**
     * Reads in entire CSV file to a List of Lists of Strings
     * 
     * @author Ken Kauffman
     * @return Contents of CSV file where each row is a List of Strings encapsulated by another List object
     * @throws FileNotFoundException if file is not there upon creation of FileReader or FileWriter objects
     * @throws IOException if the reader has already been closed and for various other file-related reasons
     */
    public final List<List<String>> readAll() throws FileNotFoundException, IOException {
        if(CSV_PARSER.isClosed()) {
            throw new IOException("The reader is already closed!");
        }

        final List<List<String>> result = new ArrayList<List<String>>();
        final List<CSVRecord> data = CSV_PARSER.getRecords();

        // Read in each row
        for(CSVRecord datum : data) {
            final Iterator<String> i = datum.iterator();
            final List<String> row = new ArrayList<String>();

            // Read each element from the row
            while(i.hasNext()) {
                // Store element in new List of Strings representing the row
                row.add(i.next());
            }
            
            // Add row to a List representing the CSV data
            result.add(row);
        }

        return result;
    }

    /**
     * Writes a single, specified line to the CSV file
     * 
     * @author Ken Kauffman
     * @param csvLine - A List of String objects to write as a single row to the CSV file
     * @throws IOException for various file-related reasons
     */
    public final void writeLine(final List<String> csvLine) throws IOException {
        CSV_PRINTER.printRecord(csvLine);
    }

    /**
     * Writes a single, specified line to the CSV file
     * 
     * @author Ken Kauffman
     * @param csvLine - An array of String objects to write as a single row to the CSV file
     * @throws IOException for various file-related reasons
     */
    public final void writeLine(final String[] csvLine) throws IOException {
        writeLine(Arrays.asList(csvLine));
    }

    /**
     * Writes a single, specified line to the CSV file
     * 
     * @author Ken Kauffman
     * @param csvLine - A delimited String object to write as a single row to the CSV file
     * @throws IOException for various file-related reasons
     */
    public final void writeLine(final String csvLine) throws IOException {
        writeLine(csvLine.split(DELIMITER));
    }

    /**
     * Writes multiple, specified lines to the CSV file
     * 
     * @author Ken Kauffman
     * @param csvLines - A List of Lists of String objects to write as rows to the CSV file
     * @throws IOException for various file-related reasons
     */
    public final void writeLines(final List<List<String>> csvLines) throws IOException {
        CSV_PRINTER.printRecords(csvLines);
    }

    /**
     * Returns the index of the column name in the header
     * 
     * @author Ken Kauffman
     * @param columnName - Name of column whose index is desired
     * @return Index of the desired column as an Integer object
     */
    public final Integer getCSVHeaderIndex(final String columnName) {
        return CSV_HEADER.indexOf(columnName);
    }

    /**
     * Returns the column name at the index in the header
     * 
     * @author Ken Kauffman
     * @param index - Index of column whose name is desired
     * @return Column name of desired index as String object
     */
    public final String getCSVHeaderColumnName(final Integer index) {
        return CSV_HEADER.get(index);
    }

    /**
     * Flushes the CSVPrinter (and nested FileWriter) object
     * 
     * @author Ken Kauffman
     * @throws IOException for various file-related reasons
     */
    public final void flushWriter() throws IOException {
        CSV_PRINTER.flush();
    }

    /**
     * Closes the CSVPrinter (and nested FileWriter) object
     * 
     * @author Ken Kauffman
     * @throws IOException for various file-related reasons
     */
    public final void closeWriter() throws IOException {
        CSV_PRINTER.close();
    }

    /**
     * Closes the CSVParser (and nested FileReader) object if not already closed; silent if already closed
     * 
     * @author Ken Kauffman
     * @throws IOException for various file-related reasons
     */
    public final void closeReader() throws IOException {
        if(!CSV_PARSER.isClosed()) {
            CSV_PARSER.close();
        }
    }
    
    /**
     * Returns the absolute path of the File object held by this class.
     * 
     * @author Ken Kauffman
     * @return The absolute path of the File object held by this class
     */
    public final String getAbsolutePath() {
        return CSV_FILE.getAbsolutePath();
    }
}
