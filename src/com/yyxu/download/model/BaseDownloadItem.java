package com.yyxu.download.model;

import android.os.Parcel;

/**
 * Video that you have try to download, no matter downloading has been finished
 * or not.
 * 
 * @author yongan.qiu@gmail.com
 */
public abstract class BaseDownloadItem extends VideoItem {

    protected String mSavePath;

    protected int mFileLength;

    public BaseDownloadItem(String name, String url, String thumbUrl,
            String savePath, int fileLength) {
        super(name, url, thumbUrl);
        mSavePath = savePath;
        mFileLength = fileLength;
    }

    public BaseDownloadItem(Parcel source) {
        super(source);
        mSavePath = source.readString();
        mFileLength = source.readInt();
    }

    /**
     * Return the path you save this video.
     * 
     * @return the path you save this video
     */
    public String getSavePath() {
        return mSavePath;
    }

    /**
     * Return the length of the video file you try to download.
     * 
     * @return the length of the video file
     */
    public int getFileLength() {
        return mFileLength;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mSavePath);
        dest.writeInt(mFileLength);
    }

}
