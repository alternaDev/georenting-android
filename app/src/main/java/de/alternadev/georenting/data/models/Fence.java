package de.alternadev.georenting.data.models;


import android.database.Cursor;

import com.google.auto.value.AutoValue;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqldelight.RowMapper;

import java.util.ArrayList;
import java.util.List;

@AutoValue public abstract class Fence implements FenceModel {
    public static final Factory<Fence> FACTORY = new Factory<>((_id, name, owner, geofenceId, latitude, longitude, radius) -> builder()._id(_id).name(name).owner(owner).geofenceId(geofenceId).latitude(latitude).longitude(longitude).radius(radius).build());
    public static final RowMapper<Fence> MAPPER = FACTORY.select_allMapper();

    public static Builder builder() {
        return new AutoValue_Fence.Builder();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String value);
        public abstract Builder owner(long value);
        public abstract Builder geofenceId(String value);
        public abstract Builder _id(Long value);
        public abstract Builder latitude(double value);
        public abstract Builder longitude(double value);
        public abstract Builder radius(double value);
        public abstract Fence build();
    }

    public static void insert(BriteDatabase db, Fence fence) {
        db.insert(Fence.TABLE_NAME, Fence.FACTORY.marshal(fence).asContentValues());
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
