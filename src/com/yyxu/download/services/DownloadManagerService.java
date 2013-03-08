
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

    private List<String> mPendingRequest;

    private List<DownloadingItem> mDownloadingItems;
    private List<DownloadingItem> mPausedItems;
    private List<DownloadingItem> mPendingItems;

    public DownloadManagerService(Context context) {
        super();
        mContext = context;

        mPendingRequest = new ArrayList<String>();

        mDownloadingItems = new ArrayList<DownloadingItem>();
        mPausedItems = new ArrayList<DownloadingItem>();
        mPendingItems = new ArrayList<DownloadingItem>();

        // Load all downloadings here.
        List<DownloadingItem> allDownloadingItems = ModelUtil.loadDownloadings(mContext, Downloading.START_TIME, true);
        for (DownloadingItem item : allDownloadingItems) {
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
        return ModelUtil.loadDownloadings(mContext, Downloading.START_TIME, true);
    }

    @Override
    public List<DownloadedItem> getAllDownloadeds() {
        return ModelUtil.loadDownloadeds(mContext, Downloaded.FINISH_TIME, true);
    }

    @Override
    public int addDownload(final IDownloadClient client, VideoItem video) {
        if (DEBUG) {
            Log.i(TAG, "addDownload: " + video);
        }

        final String url = video.getUrl();

        for (String requestUrl : mPendingRequest) {
            if (requestUrl.equals(url)) {
                return DownloadManager.ERROR_DUPLICATE_DOWNLOAD_REQUEST;
            }
        }

        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            return DownloadManager.ERROR_NETWORK_NOT_AVAILABLE;
        }

        // Check if is downloaded and file still exists.
        if (ModelUtil.hasDownloaded(mContext, url)) {
            if (new File(PathUtil.getVideoFilePath(video.getName(), url)).exists()) {
                return DownloadManager.ERROR_ALREADY_DOWNLOADED;
            } else {
                ModelUtil.deleteDownloaded(mContext, url);
            }
        }

        if (ModelUtil.hasDownloading(mContext, url)) {
            return DownloadManager.ERROR_ALREADY_ADDED;
        }

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(mContext, "未发现SD卡", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_SDCARD_NOT_FOUND;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(mContext, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_SDCARD_NOT_WRITABLE;
        }

        if (ModelUtil.getDownloadingsCount(mContext) >= MAX_DOWNLOAD) {
            Toast.makeText(mContext, "已达最大下载数", Toast.LENGTH_LONG).show();
            return DownloadManager.ERROR_DOWNLOADING_LIST_FULL;
        }

        new CreateLoaclFileTask(new CreateLoaclFileTask.Callback() {
            
            @Override
            public void onError(DownloadException exception) {
                // TODO better way to show error?
                Toast.makeText(mContext, exception.errorString(),
                        Toast.LENGTH_SHORT).show();
                mPendingRequest.remove(url);
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

                if (state == DownloadingItem.STATE_DOWNLOADING) {
                    // Start a download task.
                    DownloadTask task = createDownloadTask(client, result);
                    mDownloadingTasks.add(task);
                    task.execute((Void) null);
                    mDownloadingItems.add(result);
                } else { // Pending.
                    mPendingItems.add(result);
                }

                mPendingRequest.remove(url);

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
    public int pauseDownload(IDownloadClient client, DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "pauseDownload: " + item);
        }

        return pauseDownloadInner(client, item);
    }

    @Override
    public int resumeDownload(IDownloadClient client, DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "resumeDownload: " + item);
        }

        String url = item.getUrl();

        // Can only resume paused download.
        DownloadingItem itemInServer = findInPaused(url);
        if (itemInServer == null) {
            return DownloadManager.ERROR_ALREADY_RESUMED;
        }

        ensureDownloadTaskRemoved(url);
        removeFrom(url, mPausedItems);

        if (mDownloadingItems.size() < MAX_DOWNLOADING) {
            itemInServer.updateState(DownloadingItem.STATE_DOWNLOADING);
            mDownloadingItems.add(itemInServer);
            ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_DOWNLOADING, url);

            // Start a download task.
            DownloadTask task = createDownloadTask(client, itemInServer);
            mDownloadingTasks.add(task);
            task.execute((Void)null);
        } else { // Pending.
            itemInServer.updateState(DownloadingItem.STATE_PENDING);
            mPendingItems.add(itemInServer);
            ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_PENDING, url);
        }

        try {
            client.onDownloadingStateChanged(itemInServer.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return DownloadManager.ERROR_NO_ERROR;
    }

    @Override
    public int deleteDownload(IDownloadClient client, DownloadingItem item) {
        if (DEBUG) {
            Log.i(TAG, "deleteDownload: " + item);
        }

        String url = item.getUrl();

        DownloadingItem itemInServer = findInDownloading(url);
        if (itemInServer == null) {
            itemInServer = findInPaused(url);
            if (itemInServer == null) {
                itemInServer = findInPending(url);
                if (itemInServer == null) {
                    return DownloadManager.ERROR_DOWNLOAD_NOT_FOUND;
                }
            }
        }

        if (itemInServer.getState() == DownloadingItem.STATE_DOWNLOADING) {
            ensureDownloadTaskRemoved(url);
            removeFrom(url, mDownloadingItems);
        } else if (itemInServer.getState() == DownloadingItem.STATE_PAUSED) {
            removeFrom(url, mPausedItems);
        } else { // Pending.
            removeFrom(url, mPendingItems);
        }

        // Delete temp file.
        File file = new File(PathUtil.getVideoTempFilePath(itemInServer.getName(), url));
        if (file.exists()) {
            file.delete();
        }

        // Totally remove this download.
        ModelUtil.deleteDownloading(mContext, url);

        try {
            client.onDownloadingDeleted(itemInServer.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return DownloadManager.ERROR_NO_ERROR;
    }

    @Override
    public int pauseAllDownloads(IDownloadClient client) {
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

        return DownloadManager.ERROR_NO_ERROR;
    }

    private int pauseDownloadInner(IDownloadClient client, DownloadingItem item) {
        String url = item.getUrl();
        DownloadingItem itemInServer = findInDownloading(url);
        if (itemInServer == null) {
            itemInServer = findInPending(url);
        }
        if (itemInServer == null) {
            // Can only pause downloading or pending download.
            return DownloadManager.ERROR_ALREADY_PAUSED;
        }

        // Cancel task if downloading, and remove from old.
        if (itemInServer.getState() == DownloadingItem.STATE_DOWNLOADING) {
            ensureDownloadTaskRemoved(url);
            removeFrom(url, mDownloadingItems);
        } else { // Pending.
            removeFrom(url, mPendingItems);
        }

        // Update state and add to new.
        itemInServer.updateState(DownloadingItem.STATE_PAUSED);
        mPausedItems.add(itemInServer);
        ModelUtil.updataDownloadingState(mContext, DownloadingItem.STATE_PAUSED, url);

        try {
            client.onDownloadingStateChanged(itemInServer.copy());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return DownloadManager.ERROR_NO_ERROR;
    }

    private DownloadingItem findInDownloading(String url) {
        return findInList(url, mDownloadingItems);
    }

    private DownloadingItem findInPaused(String url) {
        return findInList(url, mPausedItems);
    }

    private DownloadingItem findInPending(String url) {
        return findInList(url, mPendingItems);
    }

    private DownloadingItem findInList(String url, List<DownloadingItem> items) {
        for (DownloadingItem item : items) {
            if (item.getUrl().equals(url)) {
                return item;
            }
        }
        return null;
    }

    private void removeFrom(String url, List<DownloadingItem> from) {
        for (DownloadingItem item : from) {
            if (item.getUrl().equals(url)) {
                from.remove(item);
                return;
            }
        }
    }

    private void ensureDownloadTaskRemoved(String url) {
        DownloadTask oldTask = findDownloadTask(url);
        if (oldTask != null) {
            oldTask.onCancelled();
            mDownloadingTasks.remove(oldTask);
        }
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
            task.getIDownloadClient().onDownloadingProgressUpdate(task.getDownloadingItem(),
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
        DownloadingItem item = task.getDownloadingItem();

        if (DEBUG) {
            Log.i(TAG, "onFinishDownload: " + item);
        }

        // Remove task from list.
        mDownloadingTasks.remove(task);

        // Remove download totally.
        removeFrom(item.getUrl(), mDownloadingItems);
        ModelUtil.deleteDownloading(mContext, item.getUrl());

        try {
            task.getIDownloadClient().onDownloadingDeleted(item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Add to DB as downloaded.
        ModelUtil.addOrUpdateDownloaded(
                mContext,
                new DownloadedItem(item.getName(), item.getUrl(), item.getThumbUrl(), item
                        .getSavePath(), (int) item.getFileLength(), System.currentTimeMillis()));
    }

    @Override
    public void onDownloadError(DownloadTask task, DownloadException error) {
        pauseDownloadInner(task.getIDownloadClient(), task.getDownloadingItem());

        if (error != null) {
            // Handle errors.
            Toast.makeText(mContext, "Error: " + error.errorString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

}
