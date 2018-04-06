package com.tampir.jlast.utils;

/**
 * Created by chongieball on 06/04/18.
 */

public class ProvidersUtils {

    private ProvidersUtils() {}

    public static String getKodePulsa(String brand) {
        switch (brand) {
            case "XL-INDO":
                return "HXP10";
            case "T-SEL INDONESIA":
                return "HSP10";
            case "AXIS INDONESIA":
                return "HXP10";
            case "INDOSAT":
                return "HIR10";
            case "THREE-IND":
                return "THR10";
        }
        return "";
    }

    public static String getRequestID(String userid) {
        return userid + DateUtils.getMinuteAndHour();
    }
}
