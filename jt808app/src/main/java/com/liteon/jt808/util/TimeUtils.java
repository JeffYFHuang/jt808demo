package com.liteon.jt808.util;



import com.liteon.javacint.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DeW on 2017/4/1.
 */

public class TimeUtils {
    private static final String TAG = LogUtils.makeTag(TimeUtils.class);

    public static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");


    public static long getCurrentTimeJT808form(){
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        String dateStr = sSimpleDateFormat.format(date);
        Logger.log(TAG + "  date string=" + dateStr);
        long strToLong = 0;
        try{
             strToLong = Long.parseLong(dateStr);
        }catch (NumberFormatException ex){
            ex.printStackTrace();
        }finally {
            return strToLong;
        }
    }
}
