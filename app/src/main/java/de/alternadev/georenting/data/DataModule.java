package de.alternadev.georenting.data;

import android.app.Application;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;

/**
 * Created by jhbruhn on 30.04.16.
 */
@Module
public class DataModule {
    @Provides
    @Singleton
    BriteDatabase provideDatabase(Application app) {
        SqlBrite sqlBrite = SqlBrite.create();
        return sqlBrite.wrapDatabaseHelper(new DelightfulOpenHelper(app), Schedulers.io());
    }
}
