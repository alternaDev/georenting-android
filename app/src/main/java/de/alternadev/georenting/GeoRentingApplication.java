package de.alternadev.georenting;

import android.app.Application;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private GeoRentingComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mComponent = DaggerGeoRentingComponent.builder()
                .geoRentingModule(new GeoRentingModule(this))
                .build();

        mComponent.inject(this);

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if(BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(
                                    Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(
                                    Stetho.defaultInspectorModulesProvider(this))
                            .build());
        }

        Timber.d("GeoRenting started.");
    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }
}
