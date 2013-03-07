package com.yyxu.download.services;

import android.os.AsyncTask;
import android.os.RemoteException;

import com.yyxu.download.error.DownloadException;
import com.yyxu.download.error.FileNotCreatedException;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.utils.HttpUtil;
import com.yyxu.download.utils.PathUtil;

class CreateLoaclFileTask extends AsyncTask<VideoItem, Void, DownloadingItem> {

    private Callback mCallback;

    private int mErrorCode = DownloadManager.ERROR_UNKNOWN_ERROR;

    public CreateLoaclFileTask(Callback callback) {
        super();
        mCallback = callback;
    }

    @Override
    protected DownloadingItem doInBackground(VideoItem... params) {
        if (params == null || params.length < 1) {
            mErrorCode = DownloadManager.ERROR_DOWNLOAD_PARAMS_ERROR;
            return null;
        }
        long fileLength = -1;
        VideoItem video = params[0];
        try {
            fileLength = HttpUtil.getRemoteFileLength(video.getUrl());
            HttpUtil.createLocalTempFile(
                    PathUtil.getVideoTempFilePath(video.getName(), video.getUrl()), fileLength);
            return new DownloadingItem(video, (int) fileLength, System.currentTimeMillis());
        } catch (RemoteException e) {
            e.printStackTrace();
            mErrorCode = DownloadManager.ERROR_FETCH_FILE_LENGTH_FAIL;
        } catch (FileNotCreatedException e) {
            e.printStackTrace();
            mErrorCode = DownloadManager.ERROR_CREATE_FILE_FAIL;
        }
        return null;
    }

    @Override
    protected void onPostExecute(DownloadingItem result) {
        if (result == null) {
            mCallback.onError(new DownloadException(mErrorCode));
        } else {
            mCallback.onDone(result);
        }
    }

    @Override
    protected void onCancelled() {
        mCallback.onError(new DownloadException(DownloadManager.ERROR_DOWNLOAD_INIT_FAIL));
    }

    public interface Callback {
        public void onError(DownloadException exception);
        public void onDone(DownloadingItem fileLength);
    }
}
