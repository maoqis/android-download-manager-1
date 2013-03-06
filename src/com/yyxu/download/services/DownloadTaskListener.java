
package com.yyxu.download.services;

public interface DownloadTaskListener {

    public void updateProcess(DownloadTask task, DownloadingProgressData progress);

    public void finishDownload(DownloadTask task);

    public void preDownload(DownloadTask task);

    public void errorDownload(DownloadTask task, Throwable error);
}
