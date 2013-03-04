package com.yyxu.download.model;

/**
 * Video you have downloaded.
 * 
 * @author yongan.qiu@gmail.com
 */
public class DownloadedItem extends BaseDownloadItem {

    private long mFinishTime;

    public DownloadedItem(String name, String url, String thumbUrl,
            String savePath, int fileLength, long finishTime) {
        super(name, url, thumbUrl, savePath, fileLength);
        mFinishTime = finishTime;
    }

    /**
     * Return the time this video is completely downloaded.
     * 
     * @return the time this video is completely downloaded.
     */
    public long getFinishTime() {
        return mFinishTime;
    }
}
