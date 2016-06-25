package de.alternadev.georenting;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GeoRentingModule {
    private final GeoRentingApplication mApp;

    public GeoRentingModule(GeoRentingApplication app) {
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

    @Provides @Singleton Application provideApplication() {
        return mApp;
    }

    @Provides @Singleton GeoRentingApplication provideGeoRentingApplication() {
        return mApp;
    }
}
