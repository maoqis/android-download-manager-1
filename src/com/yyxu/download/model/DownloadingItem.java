package com.yyxu.download.model;

/**
 * Video that you are downloading, not finished yet.
 * 
 * @author yongan.qiu@gmail.com
 */
public class DownloadingItem extends BaseDownloadItem {

    private long mStartTime;

    private int mCompletedLength;

    public DownloadingItem(String name, String url, String thumbUrl,
            String savePath, int fileLength, long startTime) {
        this(name, url, thumbUrl, savePath, fileLength, startTime, 0);
    }
    public DownloadingItem(String name, String url, String thumbUrl,
            String savePath, int fileLength, long startTime, int completedLength) {
        super(name, url, thumbUrl, savePath, fileLength);
        mStartTime = startTime;
        mCompletedLength = completedLength;
    }

    /**
     * Return the time you start download this video.
     * 
     * @return the time you start download this video
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Return the completed length of the video file you are downloading.
     * 
     * @return the completed length of the video file
     */
    public int getCompletedLength() {
        return mCompletedLength;
    }
}
