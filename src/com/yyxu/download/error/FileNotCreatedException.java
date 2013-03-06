package com.yyxu.download.error;

public class FileNotCreatedException extends Exception {

    private static final long serialVersionUID = -6485514205203414825L;

    /**
     * Constructs a new {@code FileNotCreatedException} with its stack trace filled in.
     */
    public FileNotCreatedException() {
    }

    /**
     * Constructs a new {@code FileNotCreatedException} with its stack trace and detail
     * message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public FileNotCreatedException(String detailMessage) {
        super(detailMessage);
    }
}
