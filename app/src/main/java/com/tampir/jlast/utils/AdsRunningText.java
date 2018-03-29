package com.tampir.jlast.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class AdsRunningText implements Parcelable {
    public int intId;
    public String strBody;
    public int intTotalPlayed;

    public AdsRunningText() {}

    public AdsRunningText(int intId, String strBody, int intTotalPlayed) {
        this.intId = intId;
        this.strBody = strBody;
        this.intTotalPlayed = intTotalPlayed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(intId);
        dest.writeInt(intTotalPlayed);
    }

    public static final Parcelable.Creator<AdsRunningText> CREATOR = new Creator<AdsRunningText>() {
        @Override
        public AdsRunningText createFromParcel(Parcel source) {
            AdsRunningText adsRunningText = new AdsRunningText();
            adsRunningText.intId          = source.readInt();
            adsRunningText.intTotalPlayed = source.readInt();
            return adsRunningText;
        }

        @Override
        public AdsRunningText[] newArray(int size) {
            return new AdsRunningText[size];
        }
    };
}