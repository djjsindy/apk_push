package com.sohu.mobile.push.util;

import java.util.Calendar;

/**
 * Created by jianjundeng on 5/19/14.
 */
public class DateUtils {

    public static boolean currentIsLimit(){
        Calendar calendar=Calendar.getInstance();
        int hours=calendar.get(Calendar.HOUR_OF_DAY);
        if(hours<7||hours>=23){
            return true;
        }
        return false;
    }
}
