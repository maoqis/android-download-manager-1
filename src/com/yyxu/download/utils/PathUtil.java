package com.yyxu.download.utils;

import java.io.File;

import android.os.Environment;

public class PathUtil {

    private static final String DATA_DIRECTORY_PATH;

    private static final String CACHES_DIRECTORY_PATH;

    private static final String VIDEOS_DIRECTORY_PATH;

    private static final String TEMP_SUFFIX = ".qpx";

    static {
        String storageDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        if (!storageDir.endsWith(File.separator)) {
            storageDir += File.separator;
        }
        DATA_DIRECTORY_PATH = storageDir + ".qipaoxian";
        CACHES_DIRECTORY_PATH = DATA_DIRECTORY_PATH + File.separator + "caches";
        VIDEOS_DIRECTORY_PATH = DATA_DIRECTORY_PATH + File.separator + "videos";
    }

    public static String getDataDirectoryPath() {
        return DATA_DIRECTORY_PATH;
    }

    public static String getCachesDirectoryPath() {
        return CACHES_DIRECTORY_PATH;
    }

    public static String getVideosDirectoryPath() {
        return VIDEOS_DIRECTORY_PATH;
    }

    /**
     * Get the local cache file path for the remote file.
     * 
     * @param remotePath the path of the remote file
     * @return the local cache file path
     */
    public static String getCacheFilePath(String remotePath) {
        return getCachesDirectoryPath() + File.separator
                + PathUtil.getLastPath(remotePath);
    }

    /**
     * Get the local video file path for the remote file.
     * 
     * @param name the name of the remote file
     * @return the local video file path
     */
    public static String getVideoFilePath(String name) {
        return getVideosDirectoryPath() + File.separator + name;
    }

    /**
     * Get the local video temp file path for the remote file.
     * 
     * @param name the name of the remote file
     * @return the local video temp file path
     */
    public static String getVideoTempFilePath(String name) {
        return getVideosDirectoryPath() + File.separator + name + TEMP_SUFFIX;
    }

    /**
     * Get the last path after last '/'.
     * 
     * @param path the origin long path
     * @return the last path
     */
    private static String getLastPath(String path) {
        int lastSeparator = path.lastIndexOf(File.separator);
        if (lastSeparator < 0) {
            return path;
        } else {
            return path.substring(lastSeparator + 1);
        }
    }
}
