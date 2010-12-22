/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platinum.dpv;

/**
 *
 * @author jlucier
 */
public class DictionaryPasswordFileException extends Exception {

    public DictionaryPasswordFileException(Exception e) {
        super(e);
    }

    public DictionaryPasswordFileException(String message, Exception ex) {
        super(message, ex);
    }
}
