package com.tampir.jlast.utils;

import android.content.res.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by rahmatul on 12/8/16.
 */

public class LibFunction {
    public static int dpToPx(int dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px){
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        // Get the size of the file
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("File is too large!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        return bytes;
    }

    public static int getGreyRandomColor(){
        /*
        * #D2D3DC
        * #AFAFAF
        * #F8F8FA
        * #E5E6EB
        * #C0C1CE
        *
        */
        int[] color = new int[]{0xFFD2D3DC,0xFFAFAFAF,0xFFF8F8FA,0xFFE5E6EB,0xFFC0C1CE,0xFFFF000};
        int i = new Random().nextInt(4);
        return color[i];
    }

    public static String scondToTimeString(int s){
        int hours = s / 3600;
        int minutes = (s % 3600) / 60;
        int seconds = s % 60;

        String timeString;
        if (hours>0) timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else timeString = String.format("%02d:%02d", minutes, seconds);

        return timeString;
    }
}
