
package com.yyxu.download.app;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.yyxu.download.R;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.VideoItem;
import com.yyxu.download.services.DownloadClient;
import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadProgressData;
import com.yyxu.download.utils.StorageUtils;
import com.yyxu.download.utils.Utils;
import com.yyxu.download.widgets.DownloadListAdapter;

public class DownloadListActivity extends Activity {

    private static final String TAG = "DownloadActivity";

    private DownloadManager mDownloadManager;

    private ListView mList;
    private Button mAddDownloadButton;
    private Button mPauseAllButton;

    private DownloadClient mDownloadClient;

    private DownloadListAdapter mAdapter;

    private int urlIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_list_activity);

        mDownloadManager = DownloadManager.getDefault(this);
        mDownloadClient = new DefaultDownloadClient();

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(this, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(this, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        mList = (ListView) findViewById(R.id.download_list);
        mAdapter = new DownloadListAdapter(this, mDownloadClient, mDownloadManager);
        mList.setAdapter(mAdapter);

        mAddDownloadButton = (Button) findViewById(R.id.btn_add);
        mPauseAllButton = (Button) findViewById(R.id.btn_pause_all);

        mAddDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoItem video = Utils.videos[urlIndex];
                urlIndex++;
                if (urlIndex >= Utils.videos.length) {
                    urlIndex = 0;
                }
                int result = mDownloadManager.addDownload(mDownloadClient.getIDownloadClient(), video);
                if (result != DownloadManager.ERROR_NO_ERROR) {
                    Toast.makeText(DownloadListActivity.this, "Error happened: " + result, Toast.LENGTH_LONG).show();
                }
            }
        });

        mPauseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = mDownloadManager.pauseAllDownloads(mDownloadClient.getIDownloadClient());
                if (result != DownloadManager.ERROR_NO_ERROR) {
                    Toast.makeText(DownloadListActivity.this, "Error happened: " + result, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDownloadManager.pauseAllDownloads(mDownloadClient.getIDownloadClient());
    }

    class DefaultDownloadClient extends DownloadClient {

        @Override
        public void onDownloadingAdded(DownloadingItem download) {
            Log.i(TAG, "onDownloadingAdded(): " + download);
            mAdapter.addItem(download);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDownloadingStateChanged(DownloadingItem download) {
            Log.i(TAG, "onDownloadingStateChanged(): " + download);
            mAdapter.updateItemState(download.getUrl(), download.getState());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDownloadingsStateChanged(List<DownloadingItem> downloads)
                {
            Log.i(TAG, "onDownloadingsStateChanged()");
            for (DownloadingItem item : downloads) {
                mAdapter.updateItemState(item.getUrl(), item.getState());
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDownloadingDeleted(DownloadingItem download) {
            Log.i(TAG, "onDownloadingDeleted(): " + download);
            mAdapter.removeItem(download.getUrl());
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDownloadingProgressUpdate(DownloadingItem download,
                DownloadProgressData progress) {
            Log.i(TAG, "onDownloadingProgressUpdate(): " + download);
            mAdapter.updateProgress(download.getUrl(), progress);
            mAdapter.notifyDataSetChanged();
        }
        
    }

}
