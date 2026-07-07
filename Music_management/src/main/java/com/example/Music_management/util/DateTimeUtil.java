package com.example.Music_management.util;

import com.example.Music_management.exception.InvalidDateFormatException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    public static String parstDateToString(Date date,String pattern){
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            return formatter.format(date);
        }
    public static Date parse(String dateString,String pattern){
        try{
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            return formatter.parse(dateString);
        }catch(ParseException e){
            throw new InvalidDateFormatException("Failed to parse string to date");
        }
    }
}
