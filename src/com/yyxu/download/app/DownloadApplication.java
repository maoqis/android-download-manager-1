package com.yyxu.download.app;

import android.app.Application;

import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadManagerService;
import com.yyxu.download.services.IDownloadManager;

public class DownloadApplication extends Application {

    private IDownloadManager mDownloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Object getSystemService(String name) {
        if (DownloadManager.DOWNLOAD_MANAGER.equals(name)) {
            if (mDownloadManager == null) {
                mDownloadManager = IDownloadManager.Stub.asInterface(
                        new DownloadManagerService(this));
            }
            return mDownloadManager;
        }
        return super.getSystemService(name);
    }

}
