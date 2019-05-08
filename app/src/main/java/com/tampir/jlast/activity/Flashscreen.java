package com.tampir.jlast.activity;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.Storage;
import com.tampir.jlast.utils.ApiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Flashscreen extends AppCompatActivity {

    int welcomeScreenDisplay = 2500;
    private Thread welcomeThread;
    private boolean nextPage = true;
    Handler mHandler = new Handler();

    @BindView(R.id.imgLogo)
    ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

//        ApiUtils.callApiVideo();
        ApiUtils.callApiRunningBanner();
        ButterKnife.bind(this);
        if (  //request permission >= Mashalow
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);

        }else{
            loadDataConfig();
        }
        ((TextView) findViewById(R.id.lbVersion)).setText(General.fromHtml("<b>Versi " + App.getAppVersion() + ", Build " + App.getAppBuild() + "</b><br/>" + "Copyright 2018"));
    }

    private void loadDataConfig()
    {
        YoYo.with(Techniques.FadeIn)
                .duration(1000)
                .playOn(imgLogo);

        new HttpConnection.Task(HttpConnection.METHOD_POST, "Configure", null, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {}

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                if (err == null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        App.storage.setDataReplace(jsonString, Storage.ST_CONFIG);
                    }
                }
                init();
            }
        }).execute();
    }

    private void init(){
        welcomeThread = new Thread() {
            int wait = 0;

            @Override
            public void run() {
                try {
                    super.run();
                    while (wait < welcomeScreenDisplay) {
                        sleep(100);
                        wait += 100;
                    }
                } catch (Exception e) {
                    System.out.println("EXc=" + e);
                } finally {
                    if (nextPage){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run () {
                                if (App.storage.getCurrentUser()!=null) {
                                    Intent intent = new Intent(getBaseContext(), Main.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(getBaseContext(), Login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }
                                finish();
                            }
                        });
                    }
                }
            }
        };
        welcomeThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (welcomeThread!=null) {
            if (welcomeThread.isAlive()) {
                nextPage = false;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                for (int grant : grantResults){
                    if (grant!= PackageManager.PERMISSION_GRANTED) {
                        finish();
                        return;
                    }
                }
                //init();
                loadDataConfig();
            }
        }
    }
}
