
package com.yyxu.download.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.yyxu.download.model.DatabaseModel.Downloading;
import com.yyxu.download.model.DownloadingItem;
import com.yyxu.download.model.ModelUtil;
import com.yyxu.download.services.DownloadTask;

public class ConfigUtils {

    public static final String PREFERENCE_NAME = "com.yyxu.download";

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static final int URL_COUNT = 3;

    public static void storeURL(Context context, DownloadTask task) {
        ModelUtil.addOrUpdateDownloading(
                context,
                new DownloadingItem("default_name", task.getUrl(), "thumb", "path", (int) task
                        .getFileLength(), System.currentTimeMillis()));
    }

    public static void clearURL(Context context, String url) {
        ModelUtil.deleteDownloading(context, url);
    }

    public static List<String> getURLArray(Context context) {
        List<String> urlList = new ArrayList<String>();
        List<DownloadingItem> downloadings = ModelUtil.loadDownloadings(context,
                Downloading.START_TIME, true);
        for (int i = 0; i < URL_COUNT && i < downloadings.size(); i++) {
            urlList.add(downloadings.get(i).getUrl());
        }
        return urlList;
    }

}
