package de.alternadev.georenting.data.models;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.auto.value.AutoValue;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

@AutoValue public abstract class Fence implements FenceModel {
    public static final Mapper<Fence> MAPPER = new Mapper<>(AutoValue_Fence::new);

    public static final class Marshal extends FenceMarshal<Marshal> { }

    public static Builder builder() {
        return new AutoValue_Fence.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String value);
        public abstract Builder owner(String value);
        public abstract Builder geofenceId(String value);
        public abstract Builder _id(String value);
        public abstract Builder latitude(double value);
        public abstract Builder longitude(double value);
        public abstract Builder radius(double value);
        public abstract Fence build();
    }

    public static void insert(BriteDatabase db, Fence fence) {
        db.insert(Fence.TABLE_NAME, new FenceModel.FenceMarshal<>(fence).asContentValues());
    }

    public static List<Fence> getAll(BriteDatabase db) {
        List<Fence> result = new ArrayList<>();
        try (Cursor cursor = db.query(Fence.SELECT_ALL)) {
            while (cursor.moveToNext()) {
                result.add(Fence.MAPPER.map(cursor));
            }
        }
        return result;
    }

    public static void deleteAll(BriteDatabase db) {
        db.execute(Fence.DELETE_ALL);
    }
}
