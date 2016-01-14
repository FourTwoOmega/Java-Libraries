package org.fto.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * This class serves as a way for all other classes to access basic Properties and Path object functions. This class
 * does not require the properties file to exist or the Properties object to be populated.
 *
 * @author Ken Kauffman
 */
public class PropFile {
    final private File file;
    final private Properties props;

    /**
     * Reads in a file to store as a Properties object and appends another Properties object to it in memory only
     * 
     * @author Ken Kauffman
     * @param file The file to store as a Properties object
     * @param otherProps The Properties object to be appended to the values read in from the file (can be null)
     * @throws IOException
     */
    public PropFile(final File file, final Properties otherProps)
            throws IOException {
        this.file = file;
        
        final Properties tmpProps;
        if(Files.exists(this.file.toPath())) {
            tmpProps = readPropertiesFile(this.file.toPath());
        } else {
            tmpProps = new Properties();
        }
        
        if(otherProps != null && !otherProps.isEmpty()) {
            this.props = mergeProperties(tmpProps, otherProps);
        } else {
            this.props = tmpProps;
        }
    }
    
    /**
     * Reads in a file to store as a Properties object and appends another Properties object to it in memory only
     * 
     * @author Ken Kauffman
     * @param file The file to store as a Properties object
     * @param otherProps The Properties object to be appended to the values read in from the file (can be null)
     * @throws IOException
     */
    public PropFile(final Path file, final Properties otherProps)
            throws IOException {
        this(file.toFile(), otherProps);
    }

    /**
     * Reads in a file and stores as a Properties object
     * 
     * @author Ken Kauffman
     * @param file The file to store as a Properties object
     * @throws FileNotFoundException
     * @throws IOException
     */
    public PropFile(final Path file) throws IOException {
        this(file, null);
    }

    /**
     * Reads in properties from a file at a given path and returns them as a
     * Properties object
     * 
     * @author Ken Kauffman
     * @param file The properties file to be read
     * @return The key-value pairs from the input properties file
     */
    final public static Properties readPropertiesFile(final Path file) throws IOException {        
        Properties properties = new Properties();
        
        try (FileInputStream fileInput = new FileInputStream(file.toFile())) {
            properties.load(fileInput);
            fileInput.close();
        } catch (NullPointerException npe) {
            throw new FileNotFoundException("Properties file not found! Path: " + file.toAbsolutePath().toString());
        } catch (IOException ioe) {
            throw ioe;
        } 

        return properties;
    }

    /**
     * Merges 2 Properties objects without overwriting any key-value pairs.
     * 
     * @author Ken Kauffman
     * @param props1 First Properties object to be merged
     * @param props2 Second Properties object to be merged
     * @return Merged Properties object
     */
    final public static Properties mergeProperties(final Properties props1, final Properties props2) {
        Properties result = props1;
        
        // As long as at least one input is not empty...
        if(!props1.isEmpty() || !props2.isEmpty()) {
            // ...for each key-value pair in defaultProps...
            for(Entry<Object, Object> prop : props2.entrySet()) {
                // ...append each property if it does not already have that key.
                result.putIfAbsent(prop.getKey(), prop.getValue());
            }
        } else {
            result = new Properties();
        }
        
        return result;
    }

    /**
     * Merges 2 PropFile objects without overwriting any key-value pairs.
     * 
     * @author Ken Kauffman
     * @param propFile1 First PropFile object to be merged; this object's Path object will be used to instantiate the return value's Path object
     * @param propFile2 Second PropFile object to be merged; this object Path object will not appear in the return value
     * @return Merged PropFile object
     * @throws FileNotFoundException
     * @throws IOException
     */
    final public static PropFile mergeProperties(final PropFile propFile1, final PropFile propFile2) throws FileNotFoundException, IOException {
        return new PropFile(propFile1.toPath(), mergeProperties(propFile1.toProperties(), propFile2.toProperties()));
    }

    /**
     * Merges a PropFile object and a Properties object without overwriting any key-value pairs.
     * 
     * @author Ken Kauffman
     * @param propFile PropFile object to be merged
     * @param props Properties object to be merged
     * @return New PropFile object that includes non-duplicate keys from the input Properties object
     * @throws FileNotFoundException
     * @throws IOException
     */
    final public static PropFile mergeProperties(final PropFile propFile, final Properties props) throws FileNotFoundException, IOException {
        return new PropFile(propFile.toPath(), mergeProperties(propFile.toProperties(), props));
    }

    /**
     * Writes a Properties object to a properties file at a specified path
     * 
     * @author Ken Kauffman
     * @param properties The Properties object to be written to file
     * @param file The file to which the Properties object shall be written
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    final public static void writePropertiesFile(final Properties properties, final Path file) throws FileNotFoundException, IOException {
        properties.store(new FileOutputStream(file.toFile(), true), "Auto-Generate Criterion Properties");
    }
    
    /**
     * Writes a PropFile object to file utilizing its own private class variables
     * 
     * @author Ken Kauffman
     * @param propFile Object containing both a Properties and a Path object to be written to file
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    final public static void writePropertiesFile(final PropFile propFile) throws FileNotFoundException, IOException {
        writePropertiesFile(propFile.toProperties(), propFile.toPath());
    }
    
    /**
     * Returns the absolute path of the file used to instantiate this class as a String
     * 
     * @author Ken Kauffman
     * @return Absolute path of Path object contained in this class as a String
     */
    final public String getAbsolutePath() {
        return this.file.getAbsolutePath().toString();
    }

    /**
     * Returns the File object with which this class was instantiated
     * 
     * @author Ken Kauffman
     * @return File as Path object from private class variable
     */
    final public File toFile() {
        return this.file;
    }

    /**
     * Returns the File object with which this class was instantiated as a Path object
     * 
     * @author Ken Kauffman
     * @return File as Path object from private class variable
     */
    final public Path toPath() {
        return this.file.toPath();
    }

    /**
     * Returns the specified property, and if the key is not found, an exception is thrown
     * 
     * @author Ken Kauffman
     * @param key The key with which to retrieve the value from the Properties object private class variable
     * @return
     */
    final public String getProperty(final String key) throws NoSuchElementException {
        return this.getProperty(key, null);
    }
    
    /**
     * Returns the specified property, and if the key is not found, returns the specified default value
     *
     * @author Ken Kauffman
     * @param key The Key of the property to return
     * @param defaultValue The default value to return if the specified key is not found; a null value indicates no
     * default value and will throw an Exception if the key is not found
     * @return Config file from private class variable
     * @throws NoSuchElementException
     */
    final public String getProperty(final String key, final String defaultValue)
            throws NoSuchElementException {
        final String value;
        try {
            if(defaultValue == null) {
                value = this.props.getProperty(key);
            } else {
                value = this.props.getProperty(key, defaultValue);
            }

            if (value.isEmpty()) {
                throw new NoSuchElementException(key + " is not defined in \"" + this.file.getAbsolutePath() + "\"");
            }
        } catch (NullPointerException npe) {
            throw new NoSuchElementException(key + " is not defined in \"" + this.file.getAbsolutePath() + "\"");
        }

        return value;
    }

    /**
     * Indicates whether the Properties object stored in this class contains the specified key
     * 
     * @author Ken Kauffman
     * @param key Key with which to check Properties object
     * @return Boolean value as to whether the Properties object contains the specified key
     */
    final public Boolean containsKey(final String key) {
        return this.props.containsKey(key);
    }

    /**
     * Indicates whether the Properties object stored in this class contains the specified value
     * 
     * @author Ken Kauffman
     * @param value Value with which to check Properties object
     * @return Boolean value as to whether the Properties object contains the specified value
     */
    final public Boolean containsValue(final String value) {
        return this.props.containsValue(value);
    }
    
    /**
     * Returns the Properties object with which this class was instantiated
     * 
     * @author Ken Kauffman
     * @return Properties object from private class variable
     */
    final public Properties toProperties() {
        return this.props;
    }
}
