package de.alternadev.georenting;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import de.alternadev.georenting.data.api.model.SessionToken;
import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private GeoRentingComponent mComponent;
    private SessionToken mSessionToken = new SessionToken();

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
                            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                            .build());
        }

        Timber.d("GeoRenting started.");
    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }

    public SessionToken getSessionToken() {return mSessionToken;}

    public void setSessionToken(SessionToken sessionToken) { this.mSessionToken = sessionToken;}
}
