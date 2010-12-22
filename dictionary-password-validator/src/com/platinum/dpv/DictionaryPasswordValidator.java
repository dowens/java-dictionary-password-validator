package com.platinum.dpv;

import com.platinum.dpv.util.BloomFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A singleton class which validates if a password uses dictionary words.
 * It also provides a way to look up dictionary words for validity.
 *
 * License: Apache 2.0
 *
 * @author jlucier
 */
public class DictionaryPasswordValidator {

    // Config vars
    private static final float ACCURACY = 17f;     // 0.05% false positive rate
    private static final String JAR_DICTIONARY_FILE = "dictionaries/en_US.dic";
    private static final String ABSOLUTE_DICTIONARY_FILE = "conf/" + JAR_DICTIONARY_FILE;
    private static final int MIN_WORD_CHAR_LENGTH = 4;

    // Singleton
    private static DictionaryPasswordValidator instance;

    // Class-specific vars
    private BloomFilter bloomFilter = null;
    private int totalWords = 0;
    private int bitSetSize = 0;
    private Pattern characterPattern = null;

    private DictionaryPasswordValidator() {
        // No code needed here...
    }

    /**
     * Grab the DictionaryPasswordValidator.
     * @return DictionaryPasswordValidator instance (or null on error)
     * @throws DictionaryPasswordFileException
     */
    public static synchronized DictionaryPasswordValidator getInstance() throws DictionaryPasswordFileException {

        if (instance == null) {

            instance = new DictionaryPasswordValidator();

            instance.characterPattern = Pattern.compile("[a-zA-Z]+");

            try {
                instance.initalizeDictionary();
            } catch (DictionaryPasswordFileException e) {

                // Don't initalize if we have problems reading!
                instance.characterPattern = null;
                instance = null;

                // Let it keep passing back
                throw e;
            }

        }
        return instance;
    }

    /**
     * An initializer method.  This just populates the Bloom Filter using
     * the list of words in the /conf/dictionary directory
     * @throws DictionaryPasswordFileException
     */
    private void initalizeDictionary() throws DictionaryPasswordFileException {


        // Count and calculate the bit set size
        this.totalWords = countWords();
        this.bitSetSize = (int) (ACCURACY * this.totalWords);

        // Construct our Bloom Filter
        this.bloomFilter = new BloomFilter(this.bitSetSize, this.totalWords);

        long startTime = System.currentTimeMillis();

        // Populate the Bloom Filter
        populateFilter();

        long stopTime = System.currentTimeMillis();

        printStats(startTime, stopTime);

    }

    /**
     * Prints the stats for the dictionary and the Bloom Filter to the logger
     * at the INFO level
     * @param startTime
     * @param stopTime
     */
    void printStats(long startTime, long stopTime) {

        StringBuilder sBuilder = new StringBuilder();

        DecimalFormat dFormat = new DecimalFormat("#.##");

        sBuilder.append("\n\nDictionaryPasswordValidator:\n");
        sBuilder.append("- Successfully populated the bloom filter with ");
        sBuilder.append(this.totalWords);
        sBuilder.append(" words.\n");
        sBuilder.append("- The bit set is ");
        sBuilder.append(this.bitSetSize);
        sBuilder.append(" (");
        sBuilder.append(((this.bitSetSize / 1024)));
        sBuilder.append("kb) in size.\n");
        sBuilder.append("- It took ");
        sBuilder.append(dFormat.format(((stopTime - startTime) * 0.001)));
        sBuilder.append(" seconds to populate the bloom filter.\n");
        sBuilder.append("- The current false-positive rate for the bloom filter is: ");
        sBuilder.append(dFormat.format(100 * this.bloomFilter.getFalsePositiveProbability()));
        sBuilder.append("%\n\n");

        Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.INFO, sBuilder.toString());

    }

    /**
     * Counts all the words in the dictionary
     * @return Number of words in the dictionary files
     * @throws DictionaryPasswordFileException
     */
    private int countWords() throws DictionaryPasswordFileException {


        InputStream fStream = null;
        InputStreamReader iStream = null;
        BufferedReader bReader = null;
        int total = 0;


        try {

            // Grab the dictionary file
            File dictionaryFile = new File(ABSOLUTE_DICTIONARY_FILE);
            if (dictionaryFile == null || dictionaryFile.exists() == false) {
                fStream = this.getClass().getClassLoader().getResourceAsStream(JAR_DICTIONARY_FILE);
            } else {
                fStream = new FileInputStream(dictionaryFile);
            }


            iStream = new InputStreamReader(fStream);
            bReader = new BufferedReader(iStream);
            String strLine = null;

            //Read File Line By Line
            while ((strLine = bReader.readLine()) != null) {

                if (strLine.length() > MIN_WORD_CHAR_LENGTH) {
                    total++;
                }
            }

        } catch (IOException ex) {

            Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.SEVERE, null, ex);
            throw new DictionaryPasswordFileException("Error reading file line by line", ex);

        } finally {

            // Close the input streams
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }

            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }

            if (fStream != null) {
                try {
                    fStream.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }


        return total;

    }

    /**
     * Populates the Bloom filter with the words stored in the /conf/dictionary
     * directory.
     * @throws DictionaryPasswordFileException
     */
    private void populateFilter() throws DictionaryPasswordFileException {

        InputStream fStream = null;
        InputStreamReader iStream = null;
        BufferedReader bReader = null;


        try {

            // Grab the dictionary file
            File dictionaryFile = new File(ABSOLUTE_DICTIONARY_FILE);
            if (dictionaryFile == null || dictionaryFile.exists() == false) {
                fStream = this.getClass().getClassLoader().getResourceAsStream(JAR_DICTIONARY_FILE);
            } else {
                fStream = new FileInputStream(dictionaryFile);
            }


            iStream = new InputStreamReader(fStream);
            bReader = new BufferedReader(iStream);
            String strLine = null;

            //Read File Line By Line
            while ((strLine = bReader.readLine()) != null) {

                if (strLine.length() >= MIN_WORD_CHAR_LENGTH) {
                    try {
                        bloomFilter.add(strLine.toLowerCase());
                    } catch (UnsupportedEncodingException e) {
                        // A badly formatted word... ignore
                        Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.INFO, "Invalid character encoding on: " + strLine, e);
                    }
                }
            }

        } catch (IOException ex) {

            Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.SEVERE, null, ex);
            throw new DictionaryPasswordFileException("Error reading file line by line", ex);

        } finally {

            // Close the input streams
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }

            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }

            if (fStream != null) {
                try {
                    fStream.close();
                } catch (IOException e) {
                    //TODO: Logger
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Returns true or false whether or not the passed in word exists in the
     * dictionary
     * @param word
     * @return true/false
     */
    public boolean isDictionaryWord(String word) {
        try {
            return this.bloomFilter.contains(word.toLowerCase());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Returns true or false whether or not the password contains dictionary
     * keywords
     * @param password
     * @return true/false
     */
    public boolean isPasswordDictionaryBased(String password) {

        StringBuilder pwCharsOnly = new StringBuilder();

        // Strip out all characters except A-Z and remove capitalization.
        Matcher matcher = this.characterPattern.matcher(password.toLowerCase());
        while (matcher.find()) {
            pwCharsOnly.append(matcher.group());
        }


        // Now, loop through all possible combinations.
        // Start with the minWordCharLen and grow till we're equal to
        // the length of pwCharsOnly

        int pwLength = pwCharsOnly.length();
        int strWidth = MIN_WORD_CHAR_LENGTH;
        int position = 0;
        String compareStr = null;
        while (strWidth < pwLength) {

            // Set the substr beginning as baseline
            position = 0;
            while ((position + strWidth) <= pwLength) {

                compareStr = pwCharsOnly.substring(position, (position + strWidth));
                try {
                    if (this.bloomFilter.contains(compareStr) == true) {
                        return true;
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(DictionaryPasswordValidator.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Increment the position until we reach the end of pwCharsOnly
                position++;
            }

            // Increase the width of the word now
            strWidth++;
        }

        // Looks like no matches were found
        return false;
    }
}
