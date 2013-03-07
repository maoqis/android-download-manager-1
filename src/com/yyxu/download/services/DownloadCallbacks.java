
package com.yyxu.download.services;

import com.yyxu.download.error.DownloadException;

interface DownloadCallbacks {

    void onDownloadProgressUpdate(DownloadTask task, long completedLength, long averageSpeed);

    void onPostDownload(DownloadTask task);

    void onPreDownload(DownloadTask task);

    void onDownloadError(DownloadTask task, DownloadException error);
}
