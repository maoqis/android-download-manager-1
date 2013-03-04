package com.yyxu.download.model;

/**
 * Video that you have try to download, no matter downloading has been finished
 * or not.
 * 
 * @author yongan.qiu@gmail.com
 */
public class BaseDownloadItem extends VideoItem {

    private String mSavePath;

    private int mFileLength;

    public BaseDownloadItem(String name, String url, String thumbUrl,
            String savePath, int fileLength) {
        super(name, url, thumbUrl);
        mSavePath = savePath;
        mFileLength = fileLength;
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
}
