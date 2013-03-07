package com.yyxu.download.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Keeps important informations of an remote video, such as name, url etc.
 * 
 * @author yongan.qiu@gmail.com
 */
public class VideoItem implements Parcelable {

    public static final String KEY_NAME = "name";

    public static final String KEY_URL = "url";

    public static final String KEY_THUMB_URL = "thumb_url";

    protected String mName;

    protected String mUrl;

    protected String mThumbUrl;

    public VideoItem(String name, String url, String thumbUrl) {
        mName = name;
        mUrl = url;
        mThumbUrl = thumbUrl;
    }

    public VideoItem(Parcel source) {
        mName = source.readString();
        mUrl = source.readString();
        mThumbUrl = source.readString();
    }

    /**
     * Return name of this video.
     * 
     * @return name of this video
     */
    public String getName() {
        return mName;
    }

    /**
     * Return remote url of this video.
     * 
     * @return remote url of this video
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Return thumbnail url of this video.
     * 
     * @return thumbnail url of this video
     */
    public String getThumbUrl() {
        return mThumbUrl;
    }

    /**
     * Create a content values based on fields of this item.
     * 
     * @return the content values created
     */
    public ContentValues createValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, mName);
        values.put(KEY_URL, mUrl);
        values.put(KEY_THUMB_URL, mThumbUrl);
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mUrl);
        dest.writeString(mThumbUrl);
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel source) {
            return new VideoItem(source);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    @Override
    public String toString() {
        return "VideoItem[name=" + mName + " url=" + mUrl + " thumbUrl=" + mThumbUrl + "]";
    };

    /**
     * Copy a new instance form itself.
     * 
     * @return a new instance copied
     */
    public VideoItem copy() {
        return new VideoItem(mName, mUrl, mThumbUrl);
    }

    /**
     * Create a content values based on provided fields.
     * 
     * @param name the name of this video
     * @param url the remote url of this video
     * @param thumbUrl the remote url of the thumbnail of this video
     * @return the content values created
     */
//    public static ContentValues createValues(String name, String url, String thumbUrl) {
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, name);
//        values.put(KEY_URL, url);
//        values.put(KEY_THUMB_URL, thumbUrl);
//        return values;
//    }

    /**
     * Construct an item from cursor.
     * 
     * @param cursor the cursor from cursor
     * @return a new item
     */
//    public static VideoItem fromCursor(Cursor cursor) {
//        return new VideoItem(cursor.getString(cursor.getColumnIndex(KEY_NAME)),
//                cursor.getString(cursor.getColumnIndex(KEY_URL)),
//                cursor.getString(cursor.getColumnIndex(KEY_THUMB_URL)));
//    }
}
