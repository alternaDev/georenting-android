package de.alternadev.georenting.data.api;


import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonQualifier;
import com.squareup.moshi.ToJson;

import java.lang.annotation.Retention;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class DateAdapter {
    @ToJson
    String toJson(@DateType Date date) {
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    @FromJson
    @DateType
    Date fromJson(String iso8601string) throws ParseException {
        String s = iso8601string.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
    }
    @Retention(RUNTIME)
    @JsonQualifier
    public @interface DateType {
    }
}
