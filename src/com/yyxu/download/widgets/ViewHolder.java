
package com.yyxu.download.widgets;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yyxu.download.R;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.services.DownloadingProgressData;

public class ViewHolder {

    public static final int KEY_URL = 0;
    public static final int KEY_SPEED = 1;
    public static final int KEY_PROGRESS = 2;
    public static final int KEY_IS_PAUSED = 3;

    public TextView titleText;
    public ProgressBar progressBar;
    public TextView speedText;
    public Button deleteButton;

    public Button downloadingButton;
    public Button pausedButton;
    public Button pendingButton;

    public ViewHolder(View parentView) {
        if (parentView != null) {
            titleText = (TextView) parentView.findViewById(R.id.title);
            speedText = (TextView) parentView.findViewById(R.id.speed);
            progressBar = (ProgressBar) parentView
                    .findViewById(R.id.progress_bar);
            deleteButton = (Button) parentView.findViewById(R.id.btn_delete);

            downloadingButton = (Button) parentView.findViewById(R.id.btn_downloading);
            pausedButton = (Button) parentView.findViewById(R.id.btn_paused);
            pendingButton = (Button) parentView.findViewById(R.id.btn_pending);
        }
    }

    public void setData(DownloadingItem item, DownloadingProgressData progress) {
        titleText.setText(item.getName());
        if (progress != null) {
            speedText.setText(String.valueOf(progress.averageSpeed));
            progressBar.setMax(item.getFileLength());
            progressBar.setProgress((int) progress.completedLength);
        } else {
            speedText.setText("0/0"); // TODO
            progressBar.setProgress((int) item.getCompletedLength());
        }

        setupButton(item);
    }

    private void setupButton(DownloadingItem item) {
        switch (item.getState()) {
            case DownloadingItem.STATE_DOWNLOADING:
                downloadingButton.setVisibility(View.VISIBLE);
                pausedButton.setVisibility(View.GONE);
                pendingButton.setVisibility(View.GONE);
                break;
            case DownloadingItem.STATE_PAUSED:
                downloadingButton.setVisibility(View.GONE);
                pausedButton.setVisibility(View.VISIBLE);
                pendingButton.setVisibility(View.GONE);
                break;
            case DownloadingItem.STATE_PENDING:
                downloadingButton.setVisibility(View.GONE);
                pausedButton.setVisibility(View.GONE);
                pendingButton.setVisibility(View.VISIBLE);
                break;
        }
    }
}
