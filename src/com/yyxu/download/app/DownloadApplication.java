package com.yyxu.download.app;

import com.yyxu.download.services.DownloadManager;

import android.app.Application;

public class DownloadApplication extends Application {

    private DownloadManager mDownloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Object getSystemService(String name) {
        if (DownloadManager.DOWNLOAD_MANAGER.equals(name)) {
            if (mDownloadManager == null) {
                mDownloadManager = new DownloadManager(this);
            }
            return mDownloadManager;
        }
        return super.getSystemService(name);
    }

}
