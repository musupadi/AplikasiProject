package com.tampir.jlast.utils;

import com.tampir.jlast.App;

/**
 * Created by chongieball on 14/04/18.
 */

public class StreamingVideo {

    private StreamingVideo() {}

    public static void callApiVideo() {
        new HttpConnection.Task(HttpConnection.METHOD_POST, "videostreaming", "", new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                if (err == null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        if (App.storage.getData(Storage.ST_VIDEO) == null) App.storage.setData(
                                cj.getString("data").toString(), Storage.ST_VIDEO);
                        else App.storage.setDataReplace(cj.getString("data").toString(), Storage.ST_VIDEO);
                    }
                }
            }
        }).execute();
    }
}
