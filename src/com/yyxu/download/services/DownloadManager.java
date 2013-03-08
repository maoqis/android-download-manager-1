package com.yyxu.download.services;

import java.util.List;

import android.content.Context;
import android.os.RemoteException;

import com.yyxu.download.model.DownloadedItem;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.VideoItem;

public class DownloadManager {

    protected final String TAG = "DownloadManager";

    public static final String DOWNLOAD_MANAGER = "download_qpx";

    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_ALREADY_DOWNLOADED = 1;
    public static final int ERROR_ALREADY_ADDED = 2;
    public static final int ERROR_SDCARD_NOT_FOUND = 3;
    public static final int ERROR_SDCARD_NOT_WRITABLE = 4;
    public static final int ERROR_STORAGE_NOT_ENOUGH = 5;
    public static final int ERROR_DOWNLOADING_LIST_FULL = 6;
    public static final int ERROR_NETWORK_NOT_AVAILABLE = 7;
    public static final int ERROR_NETWORK_ERROR = 8;
    public static final int ERROR_FETCH_FILE_LENGTH_FAIL = 9;
    public static final int ERROR_CREATE_FILE_FAIL = 10;
    public static final int ERROR_DOWNLOAD_INIT_FAIL = 11;
    public static final int ERROR_DOWNLOAD_PARAMS_ERROR = 12;
    public static final int ERROR_TEMP_FILE_LOST = 13;
    public static final int ERROR_IO_ERROR = 14;
    public static final int ERROR_DUPLICATE_DOWNLOAD_REQUEST = 15;
    public static final int ERROR_UNKNOWN_ERROR = 16;
    public static final int ERROR_NETWORK_TIME_OUT = 17;
    public static final int ERROR_ALREADY_RESUMED = 18;
    public static final int ERROR_ALREADY_PAUSED = 19;
    public static final int ERROR_DOWNLOAD_NOT_FOUND = 19;

    private static DownloadManager sInstance;

    private IDownloadManager sServices;

    public static DownloadManager getDefault(Context context) {
        if (sInstance == null) {
            sInstance = new DownloadManager(context, (IDownloadManager) context
                    .getApplicationContext().getSystemService(DOWNLOAD_MANAGER));
        }
        return sInstance;
    }

    private DownloadManager(Context context, IDownloadManager service) {
        sServices = service;
    }

    public List<DownloadingItem> getAllDownloadings() {
        try {
            return sServices.getAllDownloadings();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DownloadedItem> getAllDownloadeds() {
        try {
            return sServices.getAllDownloadeds();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int addDownload(IDownloadClient client, VideoItem video) {
        try {
            return sServices.addDownload(client, video);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ERROR_UNKNOWN_ERROR;
    }

    public int resumeDownload(IDownloadClient client, DownloadingItem item) {
        try {
            return sServices.resumeDownload(client, item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ERROR_UNKNOWN_ERROR;
    }

    public int deleteDownload(IDownloadClient client, DownloadingItem item) {
        try {
            return sServices.deleteDownload(client, item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ERROR_UNKNOWN_ERROR;
    }

    public int pauseDownload(IDownloadClient client, DownloadingItem item) {
        try {
            return sServices.pauseDownload(client, item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ERROR_UNKNOWN_ERROR;
    }

    public int pauseAllDownloads(IDownloadClient client) {
        try {
            return sServices.pauseAllDownloads(client);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ERROR_UNKNOWN_ERROR;
    }
}
