package com.tampir.jlast;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tampir.jlast.utils.Connectivity;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.ParameterHttpPost;

public class appService extends Service {
    boolean onUploadFile = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(Const.TAG,"Service Starting.");
        updateLocation();
    }

    private void updateLocation(){
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        updateProses();
                    }
                },
                10000);
    }

    private void updateProses(){
        ContentJson user = App.storage.getCurrentUser();
        if (user!=null && Connectivity.isConnected(getBaseContext())) {
            HttpConnection.Task uploadTask;
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .build();
            uploadTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "UpdateLocation", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    updateLocation();
                }
            });
            uploadTask.execute();
        }else{
            updateLocation();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(Const.TAG,"Service Stoping.");

    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}




