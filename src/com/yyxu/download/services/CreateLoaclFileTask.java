package com.yyxu.download.services;

import android.os.AsyncTask;
import android.os.RemoteException;

import com.yyxu.download.error.FileNotCreatedException;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.utils.HttpUtil;
import com.yyxu.download.utils.PathUtil;

public class CreateLoaclFileTask extends AsyncTask<VideoItem, Void, DownloadingItem> {

    private Callback mCallback;
    private String mErrorMsg;

    public CreateLoaclFileTask(Callback callback) {
        super();
        mCallback = callback;
    }

    @Override
    protected DownloadingItem doInBackground(VideoItem... params) {
        if (params == null || params.length < 1) {
            mErrorMsg = "Download: video params error.";
            return null;
        }
        long fileLength = -1;
        VideoItem video = params[0];
        try {
            fileLength = HttpUtil.getRemoteFileLength(video.getUrl());
            HttpUtil.createLocalTempFile(PathUtil.getVideoTempFilePath(video.getName()), fileLength);
            DownloadingItem downloadInfo = new DownloadingItem(video, (int) fileLength,
                    System.currentTimeMillis());
            return downloadInfo;
        } catch (RemoteException e) {
            e.printStackTrace();
            mErrorMsg = "Download: network error.";
        } catch (FileNotCreatedException e) {
            e.printStackTrace();
            mErrorMsg = "Download: create file failed.";
        }
        return null;
    }

    @Override
    protected void onPostExecute(DownloadingItem result) {
        if (result == null) {
            mCallback.onError(mErrorMsg);
        } else {
            mCallback.onDone(result);
        }
    }

    @Override
    protected void onCancelled() {
        mCallback.onError("Task has been canceled.");
    }

    public interface Callback {
        public void onError(String errorMsg);
        public void onDone(DownloadingItem fileLength);
    }
}
