package com.yyxu.download.services;

import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.services.DownloadProgressData;

interface IDownloadClient {
    void onDownloadingAdded(in DownloadingItem download);
    void onDownloadingStateChanged(in DownloadingItem download);
    void onDownloadingsStateChanged(in List<DownloadingItem> downloads);
    void onDownloadingDeleted(in DownloadingItem download);
    void onDownloadingProgressUpdate(in DownloadingItem download, in DownloadProgressData progress);
}
