package de.alternadev.georenting;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.elements.ShadowDocument;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import io.realm.Realm;
import io.realm.RealmConfiguration;
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

        initRealm();

        initializeSyncTask();

        Timber.d("GeoRenting started.");
    }

    private void initRealm() {
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());
    }

    private void initializeSyncTask() {
        Task task = new PeriodicTask.Builder()
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .setService(UpdateGeofencesTask.class)
                .setPeriod(3 * 60)
                .setFlex(30)
                .setUpdateCurrent(true)
                .setTag("GeofenceUpdater")
                .build();


        GcmNetworkManager.getInstance(this).schedule(task);

        task = new OneoffTask.Builder()
                .setService(UpdateGeofencesTask.class)
                .setTag("UpdateFences")
                .setExecutionWindow(0L, 5L)
                .build();
        GcmNetworkManager.getInstance(this).schedule(task);

    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }

    public SessionToken getSessionToken() {return mSessionToken;}

    public void setSessionToken(SessionToken sessionToken) { this.mSessionToken = sessionToken;}
}
