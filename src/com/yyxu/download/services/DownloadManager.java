
package com.yyxu.download.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.yyxu.download.model.DatabaseModel.Downloading;
import com.yyxu.download.model.DownloadedItem;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.ModelUtil;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.utils.StorageUtils;

public class DownloadManager {

    private static final boolean DEBUG = true;

    private static final String TAG = "DownloadManager";

    public static final String DOWNLOAD_MANAGER = "download_qpx";

    private static final int MAX_DOWNLOADING = 3;

    private static final int MAX_DOWNLOAD = 4;

    private Context mContext;

    private List<OnDownloadingChanged> mListeners;

    private List<DownloadTask> mDownloadingTasks;

    private List<DownloadingItem> mAllItems;
    private List<DownloadingItem> mDownloadingItems;
    private List<DownloadingItem> mPausedItems;
    private List<DownloadingItem> mPendingItems;

    private Boolean isRunning = false;

    public DownloadManager(Context context) {
        super();
        mContext = context;

        mListeners = new ArrayList<DownloadManager.OnDownloadingChanged>();

        mAllItems = new ArrayList<DownloadingItem>();
        mDownloadingItems = new ArrayList<DownloadingItem>();
        mPausedItems = new ArrayList<DownloadingItem>();
        mPendingItems = new ArrayList<DownloadingItem>();

        // Load all downloadings here.
        mAllItems = ModelUtil.loadDownloadings(mContext, Downloading.START_TIME, true);
        for (DownloadingItem item : mAllItems) {
            switch (item.getState()) {
                case DownloadingItem.STATE_DOWNLOADING:
                    mDownloadingItems.add(item);
                    break;
                case DownloadingItem.STATE_PAUSED:
                    mPausedItems.add(item);
                    break;
                case DownloadingItem.STATE_PENDING:
                    mPendingItems.add(item);
                    break;
            }
        }

        mDownloadingTasks = new ArrayList<DownloadTask>();
    }

    public void start() {
        isRunning = true;

        // Init download TODO
    }

    public void stop() {
        isRunning = false;
        pauseAllDownloads();
    }

    public void registerDownloadChangedListener(OnDownloadingChanged listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterDownloadChangedListener(OnDownloadingChanged listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    public void notifyDownloadAdded(DownloadingItem download) {
        for (OnDownloadingChanged listener : mListeners) {
            listener.onDownloadingAdded(download);
        }
    }

    public void notifyDownloadStateChanged(DownloadingItem download) {
        for (OnDownloadingChanged listener : mListeners) {
            listener.onDownloadingStateChanged(download);
        }
    }

    public void notifyDownloadsStateChanged() {
        for (OnDownloadingChanged listener : mListeners) {
            listener.onDownloadingsStateChanged();
        }
    }

    public void notifyDownloadDeleted(DownloadingItem download) {
        for (OnDownloadingChanged listener : mListeners) {
            listener.onDownloadingDeleted(download);
        }
    }

    public void notifyDownloadProgressUpdate(DownloadingItem download,
            DownloadingProgressData progress) {
        for (OnDownloadingChanged listener : mListeners) {
            listener.onDownloadingProgressUpdate(download, progress);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public List<DownloadingItem> getAllDownloads() {
        return mAllItems;
    }

    public void addDownload(VideoItem video) {
        if (DEBUG) {
            Log.i(TAG, "addDownload: " + video);
        }

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(mContext, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(mContext, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        if (mAllItems.size() >= MAX_DOWNLOAD) {
            Toast.makeText(mContext, "已达最大下载数", Toast.LENGTH_LONG).show();
            return;
        }

        new CreateLoaclFileTask(new CreateLoaclFileTask.Callback() {
            
            @Override
            public void onError(String errorMsg) {
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onDone(DownloadingItem result) {
                int state;
                if (mDownloadingItems.size() < MAX_DOWNLOADING) {
                    state = DownloadingItem.STATE_DOWNLOADING;
                } else {
                    state = DownloadingItem.STATE_PENDING;
                }
                result.updateState(state);

                // Add to DB.
                ModelUtil.addOrUpdateDownloading(mContext, result);
                mAllItems.add(result);

                if (state == DownloadingItem.STATE_DOWNLOADING) {
                    // Start a download task.
                    DownloadTask task = createDownloadTask(result);
                    mDownloadingTasks.add(task);
                    task.execute((Void) null);
                    mDownloadingItems.add(result);
                } else { // Pending.
                    mPendingItems.add(result);
                }

                notifyDownloadAdded(result);
            }
        }).execute(video);
    }

    public synchronized void pauseDownload(DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "pauseDownload: " + item);
        }

        // Can only pause downloading or pending download.
        if ((item.getState() & (DownloadingItem.STATE_DOWNLOADING | DownloadingItem.STATE_PENDING)) == 0) {
            throw new IllegalStateException(
                    "Can not resume a download that is neither in downloading state nor in pending state. "
                            + item);
        }

        // Cancel task if downloading, and remove from old.
        if (item.getState() == DownloadingItem.STATE_DOWNLOADING) {
            DownloadTask task = findDownloadTask(item.getUrl());
            if (task != null) {
                task.onCancelled();
                mDownloadingTasks.remove(task);
            }
            mDownloadingItems.remove(item);
        } else { // Pending.
            mPendingItems.remove(item);
        }

        // Update state and add to new.
        item.updateState(DownloadingItem.STATE_PAUSED);
        mPausedItems.add(item);
        ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_PAUSED, item.getUrl());

        notifyDownloadStateChanged(item);
    }

    public synchronized void resumeDownload(DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "resumeDownload: " + item);
        }

        // Can only resume paused download.
        if (item.getState() != DownloadingItem.STATE_PAUSED) {
            throw new IllegalStateException(
                    "Can not resume a download that is not in paused state. " + item);
        }

        mPausedItems.remove(item);
        if (mDownloadingItems.size() < MAX_DOWNLOADING) {
            item.updateState(DownloadingItem.STATE_DOWNLOADING);
            mDownloadingItems.add(item);
            ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_DOWNLOADING, item.getUrl());

            // Start a download task.
            DownloadTask task = createDownloadTask(item);
            mDownloadingTasks.add(task);
            task.execute((Void)null);
        } else { // Pending.
            item.updateState(DownloadingItem.STATE_PENDING);
            mPendingItems.add(item);
            ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_PENDING, item.getUrl());
        }

        notifyDownloadStateChanged(item);
    }

    public synchronized void deleteDownload(DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "deleteDownload: " + item);
        }

        // Cancel task if downloading, and remove.
        if (item.getState() == DownloadingItem.STATE_DOWNLOADING) {
            DownloadTask task = findDownloadTask(item.getUrl());
            if (task != null) {
                task.onCancelled();
                mDownloadingTasks.remove(task);
            }
            mDownloadingItems.remove(item);
        } else if (item.getState() == DownloadingItem.STATE_PAUSED) {
            mPausedItems.remove(item);
        } else { // Pending.
            mPendingItems.remove(item);
        }

        // Delete file.
        // TODO Is too fast that task not canceled yet.
        File file = new File(item.getSavePath());
        if (file.exists()) {
            file.delete();
        }

        // Totally remove this download.
        mAllItems.remove(item);
        ModelUtil.deleteDownloading(mContext, item.getUrl());

        notifyDownloadDeleted(item);
    }

    public synchronized void pauseAllDownloads() {
        if (DEBUG) {
            Log.i(TAG, "pauseAllDownloads.");
        }

        // Cancel all downloading task and clear tasks list.
        for (DownloadTask task : mDownloadingTasks) {
            task.onCancelled();
        }
        mDownloadingTasks.clear();

        // Update all downloading downloads to paused state,
        // and transfer them to paused list.
        for (DownloadingItem item : mDownloadingItems) {
            item.updateState(DownloadingItem.STATE_PAUSED);
        }
        mPausedItems.addAll(mDownloadingItems);
        mDownloadingItems.clear();
        ModelUtil.updataAllDownloadingState(mContext, DownloadingItem.STATE_DOWNLOADING, DownloadingItem.STATE_PAUSED);

        // Update all pending downloads to paused state,
        // and transfer them to paused list.
        for (DownloadingItem item : mPendingItems) {
            item.updateState(DownloadingItem.STATE_PAUSED);
        }
        mPausedItems.addAll(mPendingItems);
        mPendingItems.clear();
        ModelUtil.updataAllDownloadingState(mContext, DownloadingItem.STATE_PENDING, DownloadingItem.STATE_PAUSED);

        notifyDownloadsStateChanged();
    }

    public synchronized void onFinishDownload(DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "onFinishDownload: " + item);
        }

        // Remove task from list.
        DownloadTask task = findDownloadTask(item.getUrl());
        if (task != null) {
            mDownloadingTasks.remove(task);
        }

        // Remove download totally.
        mDownloadingItems.remove(item);
        mAllItems.remove(item);
        ModelUtil.deleteDownloading(mContext, item.getUrl());

        notifyDownloadDeleted(item);

        // Add to DB as downloaded.
        ModelUtil.addOrUpdateDownloaded(
                mContext,
                new DownloadedItem(item.getName(), item.getUrl(), item.getThumbUrl(), item
                        .getSavePath(), (int) item.getFileLength(), System.currentTimeMillis()));

    }

    private DownloadTask findDownloadTask(String url) {
        if (url == null) {
            return null;
        }
        for (DownloadTask task : mDownloadingTasks) {
            if (url.equals(task.getUrl())) {
                return task;
            }
        }
        return null;
    }

    /**
     * Create a new download task with default config
     */
    private DownloadTask createDownloadTask(DownloadingItem item) {

        DownloadTaskListener taskListener = new DefaultDownloadTaskListener();
        return new DownloadTask(mContext, item, taskListener);
    }

    private class DefaultDownloadTaskListener implements DownloadTaskListener {

        @Override
        public void updateProcess(DownloadTask task, DownloadingProgressData progress) {
            notifyDownloadProgressUpdate(task.getDownloadingItem(), progress);
        }

        @Override
        public void preDownload(DownloadTask task) {
        }

        @Override
        public void finishDownload(DownloadTask task) {
            onFinishDownload(task.getDownloadingItem());
        }

        @Override
        public void errorDownload(DownloadTask task, Throwable error) {
            if (error != null) {
                Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public interface OnDownloadingChanged {
        void onDownloadingAdded(DownloadingItem download);
        void onDownloadingStateChanged(DownloadingItem download);
        void onDownloadingsStateChanged();
        void onDownloadingDeleted(DownloadingItem download);
        void onDownloadingProgressUpdate(DownloadingItem download, DownloadingProgressData progress);
    }
}
