package com.tampir.jlast.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chongieball on 06/04/18.
 */

public class DateUtils {

    private DateUtils() {}

    private static String initDate(String formatDate) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatDate);

        return dateFormat.format(now);
    }

    public static String getMinuteAndHour() {
        return initDate("mmss");
    }
}
