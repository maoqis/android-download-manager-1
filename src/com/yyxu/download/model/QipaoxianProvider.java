
package com.yyxu.download.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.yyxu.download.model.DatabaseModel.Tables;

public class QipaoxianProvider extends ContentProvider {

    private static final String TAG = QipaoxianProvider.class.getSimpleName();

    // Database instance
    private static SQLiteDatabase mSQLiteDatabase = null;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int HISTORY = 1;
    private static final int FAVORITE = 2;
    private static final int DOWNLOADED = 3;
    private static final int DOWNLOADING = 4;

    static {
        sURIMatcher.addURI(DatabaseModel.AUTHORITY, "history", HISTORY);
        sURIMatcher.addURI(DatabaseModel.AUTHORITY, "favorite", FAVORITE);
        sURIMatcher.addURI(DatabaseModel.AUTHORITY, "downloaded", DOWNLOADED);
        sURIMatcher.addURI(DatabaseModel.AUTHORITY, "downloading", DOWNLOADING);
    }

    @Override
    public boolean onCreate() {
        init();
        return mSQLiteDatabase != null;
    }

    private void init() {
        if (mSQLiteDatabase == null) {
            try {
                mSQLiteDatabase = getContext().openOrCreateDatabase(DatabaseModel.DB_FILE, SQLiteDatabase.OPEN_READONLY, null);
            } catch (SQLiteException e) {
                Log.e(TAG, "Database open fail.");
            }
        }
        Cursor cursor = null;
        try {
            // To see whether tables exist. If not, create them.
            cursor = mSQLiteDatabase.query(DatabaseModel.Tables.HISTORY, null, null, null, null, null, null);
        } catch (SQLException e) {
            Log.i(TAG, "Tables not exists, so creat it.");
            mSQLiteDatabase.execSQL(DatabaseModel.History.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(DatabaseModel.Favorite.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(DatabaseModel.Downloaded.SQL_CREATE_TABLE);
            mSQLiteDatabase.execSQL(DatabaseModel.Downloading.SQL_CREATE_TABLE);
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (mSQLiteDatabase == null) {
            return null;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return null;
        }

        return mSQLiteDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (mSQLiteDatabase == null) {
            return null;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return null;
        }

        long rowId = mSQLiteDatabase.insert(table, null, values);
        if (rowId < 0) {
            Log.e(TAG, "insert failded!");
            return null;
        }

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }
        mSQLiteDatabase.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (mSQLiteDatabase.insert(table, null, values[i]) < 0) {
                    return 0;
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
        } finally {
            mSQLiteDatabase.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }

        int count = mSQLiteDatabase.delete(table, selection, selectionArgs);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mSQLiteDatabase == null) {
            return 0;
        }

        String table = uri2Table(uri);
        if (table == null) {
            return 0;
        }

        int count = mSQLiteDatabase.update(table, values, selection, selectionArgs);
        if (count > 0) {
            sendNotify(uri);
        }

        return count;
    }

    /**
     * Find out the table name for the uri.
     */
    private String uri2Table(Uri uri) {
        int match = sURIMatcher.match(uri);
        String table = null;
        switch (match) {
            case HISTORY:
                table = Tables.HISTORY;
                break;
            case FAVORITE:
                table = Tables.FAVORITE;
                break;
            case DOWNLOADED:
                table = Tables.DOWNLOADED;
                break;
            case DOWNLOADING:
                table = Tables.DOWNLOADING;
                break;
            default:
                break;
        }
        return table;
    }

    /**
     * Notify that data have been changed. If you set a parameter "notify" with
     * false value, notification won't happen.
     * 
     * @param uri of which the data have been changed
     */
    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(DatabaseModel.PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

}
