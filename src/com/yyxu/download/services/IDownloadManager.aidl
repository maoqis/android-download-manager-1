package com.yyxu.download.services;

import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.DownloadedItem;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.services.IDownloadManager;
import com.yyxu.download.services.IDownloadClient;

interface IDownloadManager {

    List<DownloadingItem> getAllDownloadings();
    List<DownloadedItem> getAllDownloadeds();

    int addDownload(in IDownloadClient client, in VideoItem video);
    void resumeDownload(IDownloadClient client, in DownloadingItem item);
    void deleteDownload(IDownloadClient client, in DownloadingItem item);
    void pauseDownload(IDownloadClient client, in DownloadingItem item);
    void pauseAllDownloads(IDownloadClient client);
}
