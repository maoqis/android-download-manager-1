
package com.yyxu.download.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.yyxu.download.model.DatabaseModel.Downloaded;
import com.yyxu.download.model.DatabaseModel.Downloading;
import com.yyxu.download.model.DatabaseModel.Video;

public class ModelUtil {

    private static final String WHERE_BY_URL = Video.URL + "=?";
    private static final String WHERE_BY_STATE_OF_DOWNLOADING = Downloading.STATE + "=?";

    private static final String[] DOWNLOADINGS_PROJECTION = new String[] {
            Downloading._ID,
            Downloading.NAME,
            Downloading.URL,
            Downloading.THUMB_URL,
            Downloading.SAVE_PATH,
            Downloading.FILE_LENGTH,
            Downloading.START_TIME,
            Downloading.COMPLETED_LENGTH,
            Downloading.STATE
    };

    private static final int DOWNLOADING_INDEX_OF_NAME = 1;
    private static final int DOWNLOADING_INDEX_OF_URL = 2;
    private static final int DOWNLOADING_INDEX_OF_THUMB_URL = 3;
    private static final int DOWNLOADING_INDEX_OF_SAVE_PATH = 4;
    private static final int DOWNLOADING_INDEX_OF_FILE_LENGTH = 5;
    private static final int DOWNLOADING_INDEX_OF_START_TIME = 6;
    private static final int DOWNLOADING_INDEX_OF_COMPLETED_LENGTH = 7;
    private static final int DOWNLOADING_INDEX_OF_STATE = 8;

    private static final String[] DOWNLOADEDS_PROJECTION = new String[] {
            Downloaded._ID,
            Downloaded.NAME,
            Downloaded.URL,
            Downloaded.THUMB_URL,
            Downloaded.SAVE_PATH,
            Downloaded.FILE_LENGTH,
            Downloaded.FINISH_TIME
    };

    private static final int DOWNLOADED_INDEX_OF_NAME = 1;
    private static final int DOWNLOADED_INDEX_OF_URL = 2;
    private static final int DOWNLOADED_INDEX_OF_THUMB_URL = 3;
    private static final int DOWNLOADED_INDEX_OF_SAVE_PATH = 4;
    private static final int DOWNLOADED_INDEX_OF_FILE_LENGTH = 5;
    private static final int DOWNLOADED_INDEX_OF_FINISH_TIME = 6;

    /******* Downloading *******/

    public static boolean hasDownloading(Context context, String url) {
        Cursor cursor = context.getContentResolver().query(
                Downloading.CONTENT_URI, null, WHERE_BY_URL,
                new String[] {
                    url
                }, null);
        boolean hasDownloading = false;
        if (cursor != null) {
            try {
                hasDownloading = (cursor.getCount() > 0);
            } finally {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return hasDownloading;
    }

    public static boolean addOrUpdateDownloading(Context context, DownloadingItem downloading) {
        ContentResolver resolver = context.getContentResolver();

        // Delete old history with same url first.
        resolver.delete(Downloading.CONTENT_URI, WHERE_BY_URL,
                new String[] {
                    downloading.getUrl()
                });

        // Insert new history.
        ContentValues values = new ContentValues();
        values.put(Downloading.NAME, downloading.getName());
        values.put(Downloading.URL, downloading.getUrl());
        values.put(Downloading.THUMB_URL, downloading.getThumbUrl());
        values.put(Downloading.SAVE_PATH, downloading.getSavePath());
        values.put(Downloading.FILE_LENGTH, downloading.getFileLength());
        values.put(Downloading.START_TIME, downloading.getStartTime());
        values.put(Downloading.COMPLETED_LENGTH, downloading.getCompletedLength());
        values.put(Downloading.STATE, downloading.getState());
        Uri uri = context.getContentResolver().insert(Downloading.CONTENT_URI, values);
        return (uri != null);
    }

    public static DownloadingItem loadDownloading(Context context, String url) {
        DownloadingItem downloading = null;
        Cursor cursor = context.getContentResolver().query(Downloading.CONTENT_URI, null,
                WHERE_BY_URL, new String[] {
                    url
                }, null);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                downloading = new DownloadingItem(
                        cursor.getString(DOWNLOADING_INDEX_OF_NAME),
                        cursor.getString(DOWNLOADING_INDEX_OF_URL),
                        cursor.getString(DOWNLOADING_INDEX_OF_THUMB_URL),
                        cursor.getString(DOWNLOADING_INDEX_OF_SAVE_PATH),
                        cursor.getInt(DOWNLOADING_INDEX_OF_FILE_LENGTH),
                        cursor.getLong(DOWNLOADING_INDEX_OF_START_TIME),
                        cursor.getInt(DOWNLOADING_INDEX_OF_COMPLETED_LENGTH),
                        cursor.getInt(DOWNLOADING_INDEX_OF_STATE));
            } finally {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return downloading;
    }

    public static List<DownloadingItem> loadDownloadings(Context context, String orderBy, boolean desc) {
        List<DownloadingItem> downloadings = new ArrayList<DownloadingItem>();
        String sortOrder = desc ? (orderBy + " DESC") : orderBy;

        Cursor cursor = context.getContentResolver().query(Downloading.CONTENT_URI,
                DOWNLOADINGS_PROJECTION, null, null, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    downloadings.add(new DownloadingItem(
                            cursor.getString(DOWNLOADING_INDEX_OF_NAME),
                            cursor.getString(DOWNLOADING_INDEX_OF_URL),
                            cursor.getString(DOWNLOADING_INDEX_OF_THUMB_URL),
                            cursor.getString(DOWNLOADING_INDEX_OF_SAVE_PATH),
                            cursor.getInt(DOWNLOADING_INDEX_OF_FILE_LENGTH),
                            cursor.getLong(DOWNLOADING_INDEX_OF_START_TIME),
                            cursor.getInt(DOWNLOADING_INDEX_OF_COMPLETED_LENGTH),
                            cursor.getInt(DOWNLOADING_INDEX_OF_STATE)
                            ));
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return downloadings;
    }

    public static boolean updataDownloading(Context context, int compeletedLength, String url) {
        ContentValues values = new ContentValues();
        values.put(Downloading.COMPLETED_LENGTH, compeletedLength);
        int updated = context.getContentResolver().update(
                Downloading.CONTENT_URI, values, WHERE_BY_URL,
                new String[] {
                    url
                });
        return (updated > 0);
    }

    public static boolean updataDownloadingState(Context context, int state, String url) {
        ContentValues values = new ContentValues();
        values.put(Downloading.STATE, state);
        int updated = context.getContentResolver().update(
                Downloading.CONTENT_URI, values, WHERE_BY_URL,
                new String[] {
                    url
                });
        return (updated > 0);
    }

    public static boolean updataAllDownloadingState(Context context, int oldState, int newState) {
        ContentValues values = new ContentValues();
        values.put(Downloading.STATE, newState);
        int updated = context.getContentResolver().update(
                Downloading.CONTENT_URI, values, WHERE_BY_STATE_OF_DOWNLOADING,
                new String[] {
                    String.valueOf(oldState)
                });
        return (updated > 0);
    }

    public static boolean deleteDownloading(Context context, String url) {
        int deleted = context.getContentResolver().delete(
                Downloading.CONTENT_URI, WHERE_BY_URL, new String[] {
                    url
                });
        return (deleted > 0);
    }

    /******* Downloaded *******/

    public static boolean hasDownloaded(Context context, String url) {
        Cursor cursor = context.getContentResolver().query(Downloaded.CONTENT_URI, null,
                WHERE_BY_URL, new String[] {
                    url
                }, null);
        boolean hasDownloaded = false;
        if (cursor != null) {
            try {
                hasDownloaded = (cursor.getCount() > 0);
            } finally {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return hasDownloaded;
    }

    public static boolean addOrUpdateDownloaded(Context context, DownloadedItem downloaded) {
        ContentResolver resolver = context.getContentResolver();

        // Delete old downloaded with same url first.
        resolver.delete(Downloaded.CONTENT_URI, WHERE_BY_URL, new String[] {
                downloaded.getUrl()
        });

        // Insert new downloaded.
        ContentValues values = new ContentValues();
        values.put(Downloaded.NAME, downloaded.getName());
        values.put(Downloaded.URL, downloaded.getUrl());
        values.put(Downloaded.THUMB_URL, downloaded.getThumbUrl());
        values.put(Downloaded.SAVE_PATH, downloaded.getSavePath());
        values.put(Downloaded.FILE_LENGTH, downloaded.getFileLength());
        values.put(Downloaded.FINISH_TIME, downloaded.getFinishTime());
        Uri uri = context.getContentResolver().insert(Downloaded.CONTENT_URI, values);
        return (uri != null);
    }

    public static List<DownloadedItem> loadDownloadeds(Context context, String orderBy, boolean desc) {
        List<DownloadedItem> downloadeds = new ArrayList<DownloadedItem>();
        String sortOrder = desc ? (orderBy + " DESC") : orderBy;

        Cursor cursor = context.getContentResolver().query(Downloaded.CONTENT_URI,
                DOWNLOADEDS_PROJECTION, null, null, sortOrder);
        try {
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    downloadeds.add(new DownloadedItem(
                            cursor.getString(DOWNLOADED_INDEX_OF_NAME),
                            cursor.getString(DOWNLOADED_INDEX_OF_URL),
                            cursor.getString(DOWNLOADED_INDEX_OF_THUMB_URL),
                            cursor.getString(DOWNLOADED_INDEX_OF_SAVE_PATH),
                            cursor.getInt(DOWNLOADED_INDEX_OF_FILE_LENGTH),
                            cursor.getLong(DOWNLOADED_INDEX_OF_FINISH_TIME)
                            ));
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return downloadeds;
    }

    public static boolean deleteDownloaded(Context context, String url) {
        int deleted = context.getContentResolver().delete(
                Downloaded.CONTENT_URI, WHERE_BY_URL, new String[] {
                    url
                });
        return (deleted > 0);
    }

}
