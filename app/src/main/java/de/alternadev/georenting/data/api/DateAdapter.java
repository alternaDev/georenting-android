package de.alternadev.georenting.data.api;


import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.ToJson;

import java.lang.annotation.Retention;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import hugo.weaving.DebugLog;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class DateAdapter {
    @ToJson
    @DebugLog
    long toJson(Date date) {
        if(date == null) return 0;
        return date.getTime() / 1000;
    }

    @FromJson
    @DebugLog
    Date fromJson(long time) throws ParseException {
        return new Date(time * 1000);
    }
}
