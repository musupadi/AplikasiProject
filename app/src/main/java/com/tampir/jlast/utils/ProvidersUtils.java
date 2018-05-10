package com.tampir.jlast.utils;

/**
 * Created by chongieball on 06/04/18.
 */

public class ProvidersUtils {

    private ProvidersUtils() {}

    public static String getKodePulsa(String brand, String poin) {
        String value = poin.substring(0, poin.length() - 2);
        switch (brand) {
            case "XL-INDO":
                return "HXP" + value;
            case "T-SEL INDONESIA":
                return "HSP" + value;
            case "AXIS INDONESIA":
                return "HXP" + value;
            case "INDOSAT":
                return "HIR" + value;
            case "THREE-IND":
                return "HTR" + value;
        }
        return "";
    }

    public static String getRequestID(String userid) {
        return userid + DateUtils.getMinuteAndHour();
    }
}
