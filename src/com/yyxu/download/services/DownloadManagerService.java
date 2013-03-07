
package com.yyxu.download.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.yyxu.download.error.DownloadException;
import com.yyxu.download.model.DatabaseModel.Downloaded;
import com.yyxu.download.model.DatabaseModel.Downloading;
import com.yyxu.download.model.DownloadedItem;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.ModelUtil;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.PathUtil;
import com.yyxu.download.utils.StorageUtils;

public class DownloadManagerService extends IDownloadManager.Stub implements DownloadCallbacks {

    private static final boolean DEBUG = true;

    protected final String TAG = "DownloadManager";

    private static final int MAX_DOWNLOADING = 3;

    private static final int MAX_DOWNLOAD = 4;

    private Context mContext;

    private List<DownloadTask> mDownloadingTasks;

    private List<DownloadedItem> mAllDownloadeds;

    private List<DownloadingItem> mAllItems;
    private List<DownloadingItem> mDownloadingItems;
    private List<DownloadingItem> mPausedItems;
    private List<DownloadingItem> mPendingItems;

    public DownloadManagerService(Context context) {
        super();
        mContext = context;

        mAllDownloadeds = new ArrayList<DownloadedItem>();

        mAllItems = new ArrayList<DownloadingItem>();
        mDownloadingItems = new ArrayList<DownloadingItem>();
        mPausedItems = new ArrayList<DownloadingItem>();
        mPendingItems = new ArrayList<DownloadingItem>();

        // Load all downloadeds here.
        mAllDownloadeds = ModelUtil.loadDownloadeds(mContext, Downloaded.FINISH_TIME, true);

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

    @Override
    public List<DownloadingItem> getAllDownloadings() {
        return mAllItems;
    }

    @Override
    public List<DownloadedItem> getAllDownloadeds() {
        return mAllDownloadeds;
    }

    @Override
    public int addDownload(final IDownloadClient client, VideoItem video) {
        if (DEBUG) {
            Log.i(TAG, "addDownload: " + video);
        }

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            return DownloadManager.ERROR_NETWORK_NOT_AVAILABLE;
        }

        // Check if is downloaded and file still exists.
        if (ModelUtil.hasDownloaded(mContext, video.getUrl())) {
            if (new File(PathUtil.getVideoFilePath(video.getName(), video.getUrl())).exists()) {
                return DownloadManager.ERROR_ALREADY_DOWNLOADED;
            } else {
                ModelUtil.deleteDownloaded(mContext, video.getUrl());
            }
        }

        if (ModelUtil.hasDownloading(mContext, video.getUrl())) {
            return DownloadManager.ERROR_ALREADY_DOWNLOADING;
        }

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(mContext, "未发现SD卡", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_SDCARD_NOT_FOUND;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(mContext, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_SDCARD_NOT_WRITABLE;
        }

        if (mAllItems.size() >= MAX_DOWNLOAD) {
            Toast.makeText(mContext, "已达最大下载数", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_DOWNLOADING_LIST_FULL;
        }

        new CreateLoaclFileTask(new CreateLoaclFileTask.Callback() {
            
            @Override
            public void onError(DownloadException exception) {
                Toast.makeText(mContext, exception.errorString(),
                        Toast.LENGTH_SHORT).show();
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
                    DownloadTask task = createDownloadTask(client, result);
                    mDownloadingTasks.add(task);
                    task.execute((Void) null);
                    mDownloadingItems.add(result);
                } else { // Pending.
                    mPendingItems.add(result);
                }

                try {
                    client.onDownloadingAdded(result.copy());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).execute(video);
        return DownloadManager.ERROR_NO_ERROR;
    }

    @Override
    public synchronized void pauseDownload(IDownloadClient client, DownloadingItem item) {
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

        try {
            client.onDownloadingStateChanged(item.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void resumeDownload(IDownloadClient client, DownloadingItem item) {
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
            DownloadTask task = createDownloadTask(client, item);
            mDownloadingTasks.add(task);
            task.execute((Void)null);
        } else { // Pending.
            item.updateState(DownloadingItem.STATE_PENDING);
            mPendingItems.add(item);
            ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_PENDING, item.getUrl());
        }

        try {
            client.onDownloadingStateChanged(item.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void deleteDownload(IDownloadClient client, DownloadingItem item) {
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

        try {
            client.onDownloadingDeleted(item.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void pauseAllDownloads(IDownloadClient client) {
        if (DEBUG) {
            Log.i(TAG, "pauseAllDownloads.");
        }

        // Cancel all downloading task and clear tasks list.
        for (DownloadTask task : mDownloadingTasks) {
            task.onCancelled();
        }
        mDownloadingTasks.clear();

        List<DownloadingItem> changedItems = new ArrayList<DownloadingItem>();

        // Update all downloading downloads to paused state,
        // and transfer them to paused list.
        for (DownloadingItem item : mDownloadingItems) {
            item.updateState(DownloadingItem.STATE_PAUSED);
            changedItems.add(item.copy());
        }
        mPausedItems.addAll(mDownloadingItems);
        mDownloadingItems.clear();
        ModelUtil.updataAllDownloadingState(mContext, DownloadingItem.STATE_DOWNLOADING, DownloadingItem.STATE_PAUSED);

        // Update all pending downloads to paused state,
        // and transfer them to paused list.
        for (DownloadingItem item : mPendingItems) {
            item.updateState(DownloadingItem.STATE_PAUSED);
            changedItems.add(item.copy());
        }
        mPausedItems.addAll(mPendingItems);
        mPendingItems.clear();
        ModelUtil.updataAllDownloadingState(mContext, DownloadingItem.STATE_PENDING, DownloadingItem.STATE_PAUSED);

        try {
            client.onDownloadingsStateChanged(changedItems);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onFinishDownload(DownloadTask task) {
        DownloadingItem item = task.getDownloadingItem();

        if (DEBUG) {
            Log.i(TAG, "onFinishDownload: " + item);
        }

        // Remove task from list.
        mDownloadingTasks.remove(task);

        // Remove download totally.
        mDownloadingItems.remove(item);
        mAllItems.remove(item);
        ModelUtil.deleteDownloading(mContext, item.getUrl());

        try {
            task.getDownloadClient().onDownloadingDeleted(item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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
    private DownloadTask createDownloadTask(IDownloadClient client, DownloadingItem item) {
        return new DownloadTask(mContext, client, item, this);
    }

    @Override
    public void onDownloadProgressUpdate(DownloadTask task, long completedLength, long averageSpeed) {
        try {
            task.getDownloadClient().onDownloadingProgressUpdate(task.getDownloadingItem(),
                    new DownloadProgressData(completedLength, averageSpeed));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreDownload(DownloadTask task) {
    }

    @Override
    public void onPostDownload(DownloadTask task) {
        onFinishDownload(task);
    }

    @Override
    public void onDownloadError(DownloadTask task, DownloadException error) {
        if (error != null) {
            Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

}
