package de.alternadev.georenting.data.api;


import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.text.ParseException;
import java.util.Date;


import hugo.weaving.DebugLog;

public class DateAdapter {
    @ToJson
    long toJson(Date date) {
        if(date == null) return 0;
        return date.getTime() / 1000;
    }

    @FromJson
    Date fromJson(long time) {
        return new Date(time * 1000);
    }
}
