package com.platinum.dpv;

/**
 * DictionaryPasswordFileException -- An exception occurred while trying
 * to read the dictionary files in the lib.
 *
 * License: Apache 2.0
 *
 * @author jlucier
 *
 */
public class DictionaryPasswordConfigException extends Exception {

    static final long serialVersionUID = -2323214234543521312L;

    public DictionaryPasswordConfigException(Exception e) {
        super(e);
    }

    public DictionaryPasswordConfigException(String message) {
        super(message);
    }

    public DictionaryPasswordConfigException(String message, Exception ex) {
        super(message, ex);
    }
}
