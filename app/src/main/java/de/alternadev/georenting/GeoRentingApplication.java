package de.alternadev.georenting;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import de.alternadev.georenting.modules.ApplicationComponent;
import de.alternadev.georenting.modules.ApplicationModule;
import de.alternadev.georenting.modules.DaggerApplicationComponent;
import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private ApplicationComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
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

    public ApplicationComponent getComponent() {
        return mComponent;
    }
}
