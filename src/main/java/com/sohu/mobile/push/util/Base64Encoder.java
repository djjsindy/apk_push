package com.sohu.mobile.push.util;

/**
 * Created by jianjundeng on 3/28/14.
 */
public class Base64Encoder {


    public static String encode(byte[] src) throws Exception {
        String result = Base64_EXT.encodeBytes(src, Base64_EXT.URL_SAFE);
        int index = result.indexOf("=");
        if (index > -1) {
            result = result.substring(0, index);
        }
        return result;
    }

    public static byte[] decode(String src) throws Exception {
        int i = src.length() % 4;
        if (i == 3) {
            src = src + "=";
        } else if (i == 2) {
            src = src + "==";
        }
        return Base64_EXT.decode(src, Base64_EXT.URL_SAFE);
    }


}
