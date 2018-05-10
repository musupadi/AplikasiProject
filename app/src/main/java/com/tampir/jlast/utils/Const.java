package com.tampir.jlast.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by rahmatul on 6/19/16.
 */
public class Const {;
    public static final String HOST = "https://cms.j-lastworld.com/api/";
    public static final String TAG = "JLAST";
    public static final int LIMIT_LOAD_DATA = 30;

    public static final File IMAGE_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Jlast");
    public static final File FILE_PATH = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Jlast-Files");

    public static final int REQUEST_FOTO_PROFILE = 201;
    public static final int REQUEST_ALBUM_PROFILE = 203;
    public static final int INTENT_REQUEST_EDITPROFILE = 204;
    public static final int INTENT_REQUEST_REGISTER = 205;
    public static final int INTENT_REQUEST_PLAYER = 206;
    public static final int INTENT_REQUEST_PICTURE = 301;
}