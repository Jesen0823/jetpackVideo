package com.jesen.cod.libnetwork.cache;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DataConvert {
    @TypeConverter
    public static long date2Long(Date date){
        return date.getTime();
    }

    @TypeConverter
    public static Date long2Date(long lg){
        return new Date(lg);
    }
}
