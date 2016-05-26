package de.alternadev.georenting.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.alternadev.georenting.data.models.FenceModel;
import de.alternadev.georenting.data.models.NotificationModel;

/**
 * Created by jhbruhn on 30.04.16.
 */
public class DelightfulOpenHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 3;
    public static final String DB_NAME = "database" + DB_VERSION + ".db";

    public DelightfulOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FenceModel.CREATE_TABLE);
        db.execSQL(NotificationModel.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}