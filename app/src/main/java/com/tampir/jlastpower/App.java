package com.tampir.jlastpower;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;
import com.tampir.jlastpower.main.screen.player.ContentPlayer;
import com.tampir.jlastpower.utils.Connectivity;
import com.tampir.jlastpower.utils.SqlConnection;
import com.tampir.jlastpower.utils.Storage;
import com.tampir.jlastpower.utils.gpsTracker;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    public final static int DbVersi = 4;
    public static Context context;
    public static Storage storage = null;
    public static SQLiteDatabase dbshared;
    public static gpsTracker gps;

    public static ContentPlayer contentPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        this.gps = new gpsTracker(this);
        this.context = this;
        this.storage = new Storage(new SqlConnection(this));
    }

    public static String getOS(){ return android.os.Build.VERSION.SDK_INT + "";}
    public static String getAppVersion(){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getAppBuild(){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Boolean isConnected(){
        return Connectivity.isConnected(context);
    }
}
