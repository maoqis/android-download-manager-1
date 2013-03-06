
package com.yyxu.download.app;

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
import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadManager.OnDownloadingChanged;
import com.yyxu.download.services.DownloadingProgressData;
import com.yyxu.download.utils.StorageUtils;
import com.yyxu.download.utils.Utils;
import com.yyxu.download.widgets.DownloadListAdapter;

public class DownloadListActivity extends Activity {

    private static final String TAG = "DownloadActivity";

    private DownloadManager mDownloadManager;

    private ListView mList;
    private Button mAddDownloadButton;
    private Button mPauseAllButton;

    private DownloadListener mListener;

    private DownloadListAdapter mAdapter;

    private int urlIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_list_activity);

        mDownloadManager = (DownloadManager) getApplicationContext().getSystemService(
                DownloadManager.DOWNLOAD_MANAGER);
        mDownloadManager.start();

        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(this, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(this, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        mList = (ListView) findViewById(R.id.download_list);
        mAdapter = new DownloadListAdapter(this, mDownloadManager);
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
                mDownloadManager.addDownload(video);
            }
        });

        mPauseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManager.pauseAllDownloads();
            }
        });

        mListener = new DownloadListener();
        mDownloadManager.registerDownloadChangedListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDownloadManager.unregisterDownloadChangedListener(mListener);
        mDownloadManager.stop();
    }

    class DownloadListener implements OnDownloadingChanged {
        
        @Override
        public void onDownloadingsStateChanged() {
            Log.i(TAG, "onDownloadingsStateChanged()");
            mAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onDownloadingStateChanged(DownloadingItem download) {
            Log.i(TAG, "onDownloadingStateChanged(): " + download);
            mAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onDownloadingProgressUpdate(DownloadingItem download,
                DownloadingProgressData progress) {
            Log.i(TAG, "onDownloadingProgressUpdate(): " + download);
            mAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onDownloadingDeleted(DownloadingItem download) {
            Log.i(TAG, "onDownloadingDeleted(): " + download);
            mAdapter.removeItem(download);
        }
        
        @Override
        public void onDownloadingAdded(DownloadingItem download) {
            Log.i(TAG, "onDownloadingAdded(): " + download);
            mAdapter.addItem(download);
        }
    }
}
