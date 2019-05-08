
package com.tampir.jlast.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.tampir.jlast.BuildConfig;

public class gpsTracker {
    LocationManager lm;
    Location gpslocation;

    boolean network_enabled=false;
    boolean network_run=false;

    Context context;
    public gpsTracker(Context context) {
        this.context = context;
    }

    public boolean getLocation(){
        if(lm==null) lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (!network_run) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            gpslocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            network_run = true;
            if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"NET START");
        }else{
            if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"NET ONSTART");
        }
        return true;
    }

    public void removeLocationListener(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"GPSNET STOP");
        if (network_run) try{lm.removeUpdates(locationListenerNetwork);network_run=false;}catch(Exception ex){if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"NET" + ex.getMessage());}
    }

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            gpslocation=location;
        }
        public void onProviderDisabled(String provider) {
            if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"AGPS OFF");
            network_enabled = false;
            if (mOnGpsChanged!=null) mOnGpsChanged.onStatus(network_enabled);
        }
        public void onProviderEnabled(String provider) {
            if(BuildConfig.BUILD_TYPE == "debug") Log.i(Const.TAG,"AGPS ON");
            network_enabled = true;
            if (mOnGpsChanged!=null) mOnGpsChanged.onStatus(network_enabled);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private OnGpsChanged mOnGpsChanged;
    public void setOnGpsChanged(OnGpsChanged mOnGpsChanged) {
        this.mOnGpsChanged = mOnGpsChanged;
        mOnGpsChanged.onStatus(network_enabled);
    }
    public interface OnGpsChanged {
        void onStatus(boolean network);
    }
}