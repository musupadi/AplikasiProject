package com.tampir.jlastpower;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.HttpConnection;

import java.io.File;

public class UploadFoto extends IntentService {
    String LOG = "uploadfoto";

    public UploadFoto() {
        super("UploadFoto");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filename = intent.getStringExtra("filename");
        File file = new File(Const.IMAGE_PATH, filename);

        if(BuildConfig.BUILD_TYPE == "debug") Log.e(LOG,"start service");
        uploadFotoProfile(file);
    }

    private void uploadFotoProfile(final File fileupload){
        if (!fileupload.exists()){
            if(BuildConfig.BUILD_TYPE == "debug") Log.e(LOG,"File tidak ditemukan");
            return;
        }
        final ContentJson user = App.storage.getCurrentUser();
        // format file [action]_[id]_[type]_.jpg
        new HttpConnection.UploadFileToServer(fileupload,"UploadFotoFileGeneral", null, new HttpConnection.OnTaskFinishListener(){
            @Override
            public void onStart() {
                if(BuildConfig.BUILD_TYPE == "debug") Log.e(LOG,"mulai upload");
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                if (err==null) {
                    if(BuildConfig.BUILD_TYPE == "debug") Log.e(LOG,"selesai -->" + jsonString);
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        //remove file
                        fileupload.delete();
                    }else {
                        //ulangi
                        uploadFotoProfile(fileupload);
                    }
                }else{
                    //ulangi
                    uploadFotoProfile(fileupload);
                    if(BuildConfig.BUILD_TYPE == "debug") Log.e(LOG,"selesai gagal-->" + err.Message);
                }
            }
        }).execute();
    }
}