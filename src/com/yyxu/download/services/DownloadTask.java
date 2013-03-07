package com.yyxu.download.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.yyxu.download.error.DownloadException;
import com.yyxu.download.http.AndroidHttpClient;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.ModelUtil;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.PathUtil;

class DownloadTask extends AsyncTask<Void, Integer, Long> {

    private static final boolean DEBUG = true;
    private static final String TAG = "DownloadTask";

    public final static int TIME_OUT = 30000;
    private final static int BUFFER_SIZE = 1024 * 8; // 8KB

    private Context mContext;
    private IDownloadClient mDownloadClient;
    private DownloadCallbacks mDownloadCallbacks;

    private DownloadingItem mDownloadingItem;
    private File mFile;
    private File mTmpFile;

    private RandomAccessFile mOutputStream;

    private long mDownloadedLengthInThisTime;
    private long mCompletedLengthTillLastTime;

    private long mCompletePercent;
    private long mAverageSpeed;
    private long mStartTime;
    private DownloadException error = null;
    private boolean interrupt = false;

    private AndroidHttpClient client;

    DownloadTask(Context context, IDownloadClient downloadClient, DownloadingItem downloading, DownloadCallbacks listener) {
        super();
        mContext = context;
        mDownloadClient = downloadClient;
        mDownloadingItem = downloading;
        mCompletedLengthTillLastTime = mDownloadingItem.getCompletedLength();
        mFile = new File(downloading.getSavePath());
        mTmpFile = new File(PathUtil.getVideoTempFilePath(downloading.getName(),
                downloading.getUrl()));

        mDownloadCallbacks = listener;
    }

    public IDownloadClient getDownloadClient() {
        return mDownloadClient;
    }

    public DownloadingItem getDownloadingItem() {
        return mDownloadingItem;
    }

    public String getUrl() {

        return mDownloadingItem.getUrl();
    }

    public boolean isInterrupt() {

        return interrupt;
    }

    public long getDownloadPercent() {

        return mCompletePercent;
    }

    public long getDownloadSize() {

        return mDownloadedLengthInThisTime + mCompletedLengthTillLastTime;
    }

    public long getFileLength() {

        return mDownloadingItem.getFileLength();
    }

    public long getDownloadSpeed() {

        return this.mAverageSpeed;
    }

    public DownloadCallbacks getListener() {

        return this.mDownloadCallbacks;
    }

    @Override
    protected void onPreExecute() {

        mStartTime = System.currentTimeMillis();
        if (mDownloadCallbacks != null)
            mDownloadCallbacks.onPreDownload(this);
    }

    @Override
    protected Long doInBackground(Void... params) {

        long result = -1;
        try {
            result = download();
        } catch (NetworkErrorException e) {
            error = new DownloadException(DownloadManager.ERROR_NETWORK_ERROR);
        } catch (DownloadException e) {
            error = e;
        } catch (FileNotFoundException e) {
            error = new DownloadException(DownloadManager.ERROR_TEMP_FILE_LOST);
        } catch (IOException e) {
            error = new DownloadException(DownloadManager.ERROR_IO_ERROR);
        } finally {
            if (client != null) {
                client.close();
            }
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        if (progress.length > 1) {
            int fileLength = progress[1];
            if (fileLength < 0) {
                if (mDownloadCallbacks != null)
                    mDownloadCallbacks.onDownloadError(this, error);
            }
        } else {
            mDownloadedLengthInThisTime = progress[0];
            long completedLength = mDownloadedLengthInThisTime + mCompletedLengthTillLastTime;
            mDownloadingItem.updateCompletedLength((int) completedLength);
            mCompletePercent = (completedLength) * 100 / mDownloadingItem.getFileLength();

            long costTime = System.currentTimeMillis() - mStartTime;
            mAverageSpeed = mDownloadedLengthInThisTime / costTime;

            if (mDownloadCallbacks != null)
                mDownloadCallbacks.onDownloadProgressUpdate(this, completedLength, mAverageSpeed);
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        if (result == -1 || interrupt || error != null) {
            // Some error.
            if (mDownloadCallbacks != null) {
                mDownloadCallbacks.onDownloadError(this, error);
            }
        } else {
            // Finish download
            mTmpFile.renameTo(mFile);
            if (mDownloadCallbacks != null) {
                mDownloadCallbacks.onPostDownload(this);
            }
        }

    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        interrupt = true;
    }

    private long download() throws NetworkErrorException, IOException, DownloadException {
        // Check network.
        if (!NetworkUtils.isNetworkAvailable(mContext)) {
            throw new NetworkErrorException("Network blocked.");
        }

        // Check file length;
        client = AndroidHttpClient.newInstance("QiPaoXianClient");
        HttpGet httpGet = new HttpGet(mDownloadingItem.getUrl());
        HttpResponse response = client.execute(httpGet);
        long remoteFileLength = response.getEntity().getContentLength();
        client.close();
        if (mDownloadingItem.getFileLength() != remoteFileLength) {
            Log.w(TAG, "Remote file has change length!");
            return -1;
        }

        // Check file exist.
        if (mFile.exists() && remoteFileLength == mFile.length()) {
            if (DEBUG) {
                Log.w(TAG, "File already exists, skipping download.");
            }
            throw new DownloadException(DownloadManager.ERROR_ALREADY_DOWNLOADED);
        } else if (!mTmpFile.exists()) {
            throw new FileNotFoundException(mTmpFile.getAbsolutePath() + " Temp file not exist.");
        }

        client = AndroidHttpClient.newInstance("QiPaoXianClient");
        httpGet.addHeader("Range", "bytes=" + mDownloadingItem.getCompletedLength() + "-");
        response = client.execute(httpGet);

        // Start download.
        mOutputStream = new ProgressReportingRandomAccessFile(mTmpFile, "rw");

        publishProgress(0, (int) remoteFileLength);

        InputStream input = response.getEntity().getContent();
        int bytesCopied = copy(input, mOutputStream);

        if ((mCompletedLengthTillLastTime + bytesCopied) != mDownloadingItem.getFileLength()
                && !interrupt) {
            throw new IOException("Download incomplete: " + bytesCopied + " != "
                    + mDownloadingItem.getFileLength());
        }

        if (DEBUG) {
            Log.v(TAG, "Download completed successfully.");
        }

        return bytesCopied;

    }

    public int copy(InputStream input, RandomAccessFile out) throws IOException,
            NetworkErrorException {

        if (input == null || out == null) {
            return -1;
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        if (DEBUG) {
            Log.v(TAG, "length" + out.length());
        }

        int count = 0, n = 0;
        long errorBlockTimePreviousTime = -1, expireTime = 0;

        try {

            out.seek(mDownloadingItem.getCompletedLength());

            while (!interrupt) {
                n = in.read(buffer, 0, BUFFER_SIZE);
                if (n == -1) {
                    break;
                }
                out.write(buffer, 0, n);
                ModelUtil.updataDownloading(mContext, mDownloadingItem.getCompletedLength(), mDownloadingItem.getUrl());
                count += n;

                /*
                 * check network
                 */
                if (!NetworkUtils.isNetworkAvailable(mContext)) {
                    throw new NetworkErrorException("Network blocked.");
                }

                if (mAverageSpeed == 0) {
                    if (errorBlockTimePreviousTime > 0) {
                        expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
                        if (expireTime > TIME_OUT) {
                            throw new ConnectTimeoutException("connection time out.");
                        }
                    } else {
                        errorBlockTimePreviousTime = System.currentTimeMillis();
                    }
                } else {
                    expireTime = 0;
                    errorBlockTimePreviousTime = -1;
                }
            }
        } finally {
            client.close(); // must close client first
            client = null;
            out.close();
            in.close();
            input.close();
        }
        return count;

    }

    private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
        private int mWritedBytes = 0;

        public ProgressReportingRandomAccessFile(File file, String mode)
                throws FileNotFoundException {

            super(file, mode);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {

            super.write(buffer, offset, count);
            mWritedBytes += count;
            publishProgress(mWritedBytes);
        }
    }
}
