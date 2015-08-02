package de.alternadev.georenting;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoRentingModule {
    private Application mApp;

    public GeoRentingModule(Application app) {
        this.mApp = app;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(mApp);
    }

    @Provides
    @Singleton
    RefWatcher provideLeakCanary() {
        return LeakCanary.install(mApp);
    }
}
