package com.yyxu.download.services;

public class DownloadingProgressData {

    public long completedLength;
    public long averageSpeed;

    public DownloadingProgressData(long completedLength, long averageSpeed) {
        this.completedLength = completedLength;
        this.averageSpeed = averageSpeed;
    }

}
