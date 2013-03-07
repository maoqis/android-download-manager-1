package com.yyxu.download.services;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadProgressData implements Parcelable {

    public long completedLength;
    public long averageSpeed;

    public DownloadProgressData(long completedLength, long averageSpeed) {
        this.completedLength = completedLength;
        this.averageSpeed = averageSpeed;
    }

    public DownloadProgressData(Parcel source) {
        completedLength = source.readLong();
        averageSpeed = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(completedLength);
        dest.writeLong(averageSpeed);
    }

    public static final Creator<DownloadProgressData> CREATOR = new Creator<DownloadProgressData>() {
        @Override
        public DownloadProgressData createFromParcel(Parcel source) {
            return new DownloadProgressData(source);
        }

        @Override
        public DownloadProgressData[] newArray(int size) {
            return new DownloadProgressData[size];
        }
    };

}
