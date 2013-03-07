
package com.yyxu.download.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yyxu.download.R;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.services.DownloadClient;
import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadProgressData;

public class DownloadListAdapter extends BaseAdapter {

    private Context mContext;
    private List<DownloadingItem> mDownloads = new ArrayList<DownloadingItem>();
    private HashMap<String, DownloadProgressData> mProgressMap = new HashMap<String, DownloadProgressData>();
    private DownloadManager mDownloadManager;
    private DownloadClient mDownloadClient;

    public DownloadListAdapter(Context context, DownloadClient transport, DownloadManager downloadManager) {
        mContext = context;
        mDownloadClient = transport;
        mDownloadManager = downloadManager;
        mDownloads.addAll(mDownloadManager.getAllDownloadings());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDownloads.size();
    }

    public void updateProgress(DownloadingItem item, DownloadProgressData progress) {
        if (/*mProgressMap.containsKey(item.getUrl())*/true) {
            mProgressMap.put(item.getUrl(), progress);
        }
    }

    public void addItem(DownloadingItem item) {
        mDownloads.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(DownloadingItem item) {
        mDownloads.remove(item);
        mProgressMap.remove(item.getUrl());
        notifyDataSetChanged();
    }

    @Override
    public DownloadingItem getItem(int position) {
        return mDownloads.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.download_list_item, null);
        }
        DownloadingItem item = getItem(position);

        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.setData(item, mProgressMap.get(item.getUrl()));

        viewHolder.downloadingButton.setOnClickListener(new DownloadBtnListener(item, viewHolder));
        viewHolder.pausedButton.setOnClickListener(new DownloadBtnListener(item, viewHolder));
        viewHolder.deleteButton.setOnClickListener(new DownloadBtnListener(item, viewHolder));

        return convertView;
    }

    private class DownloadBtnListener implements View.OnClickListener {
        private DownloadingItem item;
        private ViewHolder mViewHolder;

        public DownloadBtnListener(DownloadingItem item, ViewHolder viewHolder) {
            this.item = item;
            this.mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_downloading:
                case R.id.btn_pending:
                    mDownloadManager.pauseDownload(mDownloadClient.getIDownloadClient(), item);

                    mViewHolder.downloadingButton.setVisibility(View.GONE);
                    mViewHolder.pausedButton.setVisibility(View.VISIBLE);
                    mViewHolder.pendingButton.setVisibility(View.GONE);
                    break;
                case R.id.btn_paused:
                    mDownloadManager.resumeDownload(mDownloadClient.getIDownloadClient(), item);

                    boolean downloading = item.getState() == DownloadingItem.STATE_DOWNLOADING;
                    mViewHolder.downloadingButton.setVisibility(downloading ? View.VISIBLE : View.GONE);
                    mViewHolder.pausedButton.setVisibility(View.GONE);
                    mViewHolder.pendingButton.setVisibility(downloading ? View.GONE : View.VISIBLE);
                    break;
                case R.id.btn_delete:
                    mDownloadManager.deleteDownload(mDownloadClient.getIDownloadClient(), item);
                    break;
            }
        }
    }
}
