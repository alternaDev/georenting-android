package de.alternadev.georenting.data.models;


import android.database.Cursor;

import com.google.auto.value.AutoValue;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqldelight.RowMapper;

import java.util.ArrayList;
import java.util.List;

@AutoValue public abstract class Notification implements NotificationModel {
    public static final Factory<Notification> FACTORY = new Factory<>(( data) -> builder().data(data).build());
    public static final RowMapper<Notification> MAPPER = FACTORY.select_allMapper();

    public static Notification.Builder builder() {
        return new AutoValue_Notification.Builder();
    }

    public abstract Notification.Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Notification.Builder data(String value);
        public abstract Notification build();
    }

    public static void insert(BriteDatabase db, Notification data) {
        db.insert(Notification.TABLE_NAME, Notification.FACTORY.marshal(data).asContentValues());
    }

    public static List<Notification> getAll(BriteDatabase db) {
        List<Notification> result = new ArrayList<>();
        try (Cursor cursor = db.query(Notification.SELECT_ALL)) {
            while (cursor.moveToNext()) {
                result.add(Notification.MAPPER.map(cursor));
            }
        }
        return result;
    }

    public static void deleteAll(BriteDatabase db) {
        db.execute(Notification.DELETE_ALL);
    }
}
