
package com.yyxu.download.services;


public class DownloadService /*extends Service*/ {

//    private DownloadManager mDownloadManager;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mDownloadManager = new DownloadManager(this);
//    }
//
//    @Override
//    public void onStart(Intent intent, int startId) {
//
//        super.onStart(intent, startId);
//
//        if (intent.getAction().equals("com.yyxu.download.services.IDownloadService")) {
//            int type = intent.getIntExtra(MyIntents.TYPE, -1);
//            String url;
//
//            switch (type) {
//                case MyIntents.Types.START:
//                    if (!mDownloadManager.isRunning()) {
//                        mDownloadManager.startManage();
//                    }
//                    break;
//                case MyIntents.Types.ADD:
//                    url = intent.getStringExtra(MyIntents.URL);
//                    if (!TextUtils.isEmpty(url) && !mDownloadManager.hasTask(url)) {
//                        mDownloadManager.addTask(url);
//                    }
//                    break;
//                case MyIntents.Types.CONTINUE:
//                    url = intent.getStringExtra(MyIntents.URL);
//                    if (!TextUtils.isEmpty(url)) {
//                        mDownloadManager.resumeDownload(url);
//                    }
//                    break;
//                case MyIntents.Types.DELETE:
//                    url = intent.getStringExtra(MyIntents.URL);
//                    if (!TextUtils.isEmpty(url)) {
//                        mDownloadManager.deleteDownload(url);
//                    }
//                    break;
//                case MyIntents.Types.PAUSE:
//                    url = intent.getStringExtra(MyIntents.URL);
//                    if (!TextUtils.isEmpty(url)) {
//                        mDownloadManager.pauseTask(url);
//                    }
//                    break;
//                case MyIntents.Types.STOP:
//                    mDownloadManager.close();
//                    // mDownloadManager = null;
//                    break;
//
//                default:
//                    break;
//            }
//        }
//
//    }

}
