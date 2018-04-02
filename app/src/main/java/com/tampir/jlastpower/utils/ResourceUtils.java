package com.tampir.jlastpower.utils;

import android.content.Context;

/**
 * Created by chongieball on 30/03/18.
 */

public class ResourceUtils {

    private ResourceUtils() {}

    public static int getImageFromDrawable(String imageName, Context context) {
        int drawableId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());

        return drawableId;
    }
}
