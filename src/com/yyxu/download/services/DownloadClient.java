
package com.yyxu.download.services;

import java.util.List;

import com.yyxu.download.model.DownloadingItem;

public abstract class DownloadClient {

    private Transport mTransport = new Transport();

    public abstract void onDownloadingAdded(DownloadingItem download);

    public abstract void onDownloadingStateChanged(DownloadingItem download);

    public abstract void onDownloadingsStateChanged(List<DownloadingItem> download);

    public abstract void onDownloadingDeleted(DownloadingItem download);

    public abstract void onDownloadingProgressUpdate(DownloadingItem download,
            DownloadProgressData progress);

    public IDownloadClient getIDownloadClient() {
        return mTransport;
    }

    class Transport extends IDownloadClient.Stub {

        @Override
        public void onDownloadingAdded(DownloadingItem download) {
            DownloadClient.this.onDownloadingAdded(download);
        }

        @Override
        public void onDownloadingStateChanged(DownloadingItem download) {
            DownloadClient.this.onDownloadingStateChanged(download);
        }

        @Override
        public void onDownloadingsStateChanged(List<DownloadingItem> download) {
            DownloadClient.this.onDownloadingsStateChanged(download);
        }

        @Override
        public void onDownloadingDeleted(DownloadingItem download) {
            DownloadClient.this.onDownloadingDeleted(download);
        }

        @Override
        public void onDownloadingProgressUpdate(DownloadingItem download,
                DownloadProgressData progress) {
            DownloadClient.this.onDownloadingProgressUpdate(download, progress);
        }

    }

}
