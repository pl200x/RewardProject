package com.example.Multi_reward.util;

import com.example.Multi_reward.exception.InvalidDateFormatException;
import org.apache.ibatis.javassist.Loader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtil {

    public static String format(Date dateTime, String pattern){
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(dateTime);
    }
    public static Date parse(String dateString,String pattern){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            return formatter.parse(dateString);
        }catch(ParseException e){
            throw new InvalidDateFormatException("Failed to parse string to date");
        }
    }
    public static Date getCurrentDateStart(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }
    public static Date getCurrentDateEnd(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return calendar.getTime();
    }
}
