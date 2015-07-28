package de.alternadev.georenting;

import android.app.Application;
import android.os.Build;

import timber.log.Timber;

public class GeoRentingApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.d("GeoRenting started.");
    }
}
