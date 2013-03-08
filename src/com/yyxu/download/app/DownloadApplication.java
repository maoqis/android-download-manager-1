package com.yyxu.download.app;

import android.app.Application;
import android.os.IBinder;

import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadManagerService;
import com.yyxu.download.services.IDownloadManager;

public class DownloadApplication extends Application {

    private IBinder mDownloadManagerService;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Object getSystemService(String name) {
        if (DownloadManager.DOWNLOAD_MANAGER.equals(name)) {
            if (mDownloadManagerService == null) {
                mDownloadManagerService = new DownloadManagerService(this);
            }
            return IDownloadManager.Stub.asInterface(mDownloadManagerService);
        }
        return super.getSystemService(name);
    }

}
