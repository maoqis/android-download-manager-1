
package com.yyxu.download.error;

public class DownloadException extends Exception {

    private static final long serialVersionUID = 1L;

//    public static final int ERROR_UNKNOWN_ERROR = 0;
//    public static final int ERROR_ALREADY_DOWNLOADED = 1;
//    public static final int ERROR_ALREADY_DOWNLOADING = 2;
//    public static final int ERROR_SDCARD_NOT_FOUND = 3;
//    public static final int ERROR_SDCARD_NOT_WRITABLE = 4;
//    public static final int ERROR_STORAGE_NOT_ENOUGH = 5;
//    public static final int ERROR_DOWNLOADING_LIST_FULL = 6;
//    public static final int ERROR_NETWORK_NOT_AVAILABLE = 7;
//    public static final int ERROR_NETWORK_ERROR = 8;
//    public static final int ERROR_FETCH_FILE_LENGTH_FAIL = 9;
//    public static final int ERROR_CREATE_FILE_FAIL = 10;
//    public static final int ERROR_DOWNLOAD_INIT_FAIL = 11;
//    public static final int ERROR_DOWNLOAD_PARAMS_ERROR = 12;
//    public static final int ERROR_TEMP_FILE_LOST = 13;
//    public static final int ERROR_IO_ERROR = 14;

    public int errorCode;

    public DownloadException(int errorCode) {
        this.errorCode = errorCode;
    }

    public String errorString() {
        return String.valueOf(errorCode);
    }
}
