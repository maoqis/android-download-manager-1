package com.yyxu.download.model;

import android.net.Uri;

/**
 * Defines all models of database, like tables, columns, etc.
 * 
 * @author yongan.qiu@gmail.com
 */
public class DatabaseModel {

    public static final String AUTHORITY = "com.qipaoxian.client";

    public static final String PARAMETER_NOTIFY = "notify";

    public static final String DB_FILE = "qipaoxianclient.db";

    /**
     * All tables names are defined here.
     */
    public static class Tables {
        public static final String HISTORY = "history";
        public static final String FAVORITE = "favorite";
        public static final String DOWNLOADED = "downloaded";
        public static final String DOWNLOADING = "downloading";
    }

    /**
     * Base columns of an video-like item.
     */
    public interface Video {

        /**
         * ID column.
         * <p>Type: INTEGER</p>
         */
        public static final String _ID = "_id";

        /**
         * The name of the video.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * The remote url of the video, like "http://www.qipaoxian.com/test.mp4".
         * <P>Type: TEXT</P>
         */
        public static final String URL = "url";

        /**
         * The remote url of the video thumbnail, like "http://www.qipaoxian.com/test.jpg".
         * <P>Type: TEXT</P>
         */
        public static final String THUMB_URL = "thumb_url";
    }

    /**
     * Videos we ever played, just save their summary infomations to database,
     * so we can easyly find out them and play again some time.
     */
    public static class History implements Video {
        /**
         * The content:// style URI for all histories.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.HISTORY);

        /**
         * The content:// style URI for all histories, with no notification when
         * histories change.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.HISTORY + "?"
                            + PARAMETER_NOTIFY + "=false");

        /**
         * The play time of history record(last played time of the video).
         * <P>Type: INTEGER</P>
         */
        public static final String PLAY_TIME = "played_time";

        /**
         * SQL to create a new histories table, used when the app first opened.
         */
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.HISTORY +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                NAME + " TEXT NOT NULL," + 
                URL + " TEXT NOT NULL," + 
                THUMB_URL + " TEXT NOT NULL," + 
                PLAY_TIME + " INTEGER NOT NULL DEFAULT 0);";
    }

    /**
     * Favorite videos.
     */
    public static class Favorite implements Video{
        /**
         * The content:// style URI for all favorites.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.FAVORITE);

        /**
         * The content:// style URI for all favorites, with no notification when
         * favorites change.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.FAVORITE + "?"
                            + PARAMETER_NOTIFY + "=false");

        /**
         * The favorite time of favorite record.
         * <P>Type: INTEGER</P>
         */
        public static final String FAVORITE_TIME = "favorite_time";

        /**
         * SQL to create a new favorites table, used when the app first opened.
         */
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.FAVORITE +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                NAME + " TEXT NOT NULL," + 
                URL + " TEXT NOT NULL," + 
                THUMB_URL + " TEXT NOT NULL," + 
                FAVORITE_TIME + " INTEGER NOT NULL DEFAULT 0);";
    }

    /**
     * Base columns of an download-like item.
     */
    public interface BaseDownload extends Video {
        /**
         * The save path of local video.
         * <P>Type: INTEGER</P>
         */
        public static final String SAVE_PATH = "save_path";

        /**
         * The file length of video.
         * <P>Type: INTEGER</P>
         */
        public static final String FILE_LENGTH = "file_length";
    }

    /**
     * Downloaded videos.
     */
    public static class Downloaded implements BaseDownload {
        /**
         * The content:// style URI for all downloaded videos.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.DOWNLOADED);

        /**
         * The content:// style URI for all downloaded videos, with no notification when
         * downloaded videos change.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.DOWNLOADED + "?"
                            + PARAMETER_NOTIFY + "=false");

        /**
         * The finish time of downloaded video.
         * <P>Type: INTEGER</P>
         */
        public static final String FINISH_TIME = "finish_time";

        /**
         * SQL to create a new downloaded table, used when the app first opened.
         */
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.DOWNLOADED +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                NAME + " TEXT NOT NULL," + 
                URL + " TEXT NOT NULL," + 
                THUMB_URL + " TEXT NOT NULL," + 
                SAVE_PATH + " TEXT NOT NULL," +
                FILE_LENGTH + " INTEGER NOT NULL DEFAULT 0," +
                FINISH_TIME + " INTEGER NOT NULL DEFAULT 0);";
    }

    /**
     * Downloading videos, we can pause, resume or cancel any download task
     * while downloading.
     */
    public static class Downloading implements BaseDownload {
        /**
         * The content:// style URI for all downloading videos.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + Tables.DOWNLOADING);

        /**
         * The content:// style URI for all downloading videos, with no notification when
         * downloading videos change.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + Tables.DOWNLOADING + "?"
                            + PARAMETER_NOTIFY + "=false");

        /**
         * The start time of downloading video.
         * <P>Type: INTEGER</P>
         */
        public static final String START_TIME = "start_time";

        /**
         * The completed length of downloading video.
         * <P>Type: INTEGER</P>
         */
        public static final String COMPLETED_LENGTH = "completed_length";

        /**
         * SQL to create a new downloading table, used when the app first opened.
         */
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + Tables.DOWNLOADING +" (" + 
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
                NAME + " TEXT NOT NULL," + 
                URL + " TEXT NOT NULL," + 
                THUMB_URL + " TEXT NOT NULL," + 
                SAVE_PATH + " TEXT NOT NULL," +
                FILE_LENGTH + " INTEGER NOT NULL DEFAULT 0," +
                START_TIME + " INTEGER NOT NULL DEFAULT 0," + 
                COMPLETED_LENGTH + " INTEGER NOT NULL DEFAULT 0);";
    }
}
