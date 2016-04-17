package de.alternadev.georenting.data.tasks;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.geofencing.GeofenceTransitionsIntentService;
import de.alternadev.georenting.data.models.Fence;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class UpdateGeofencesTask extends GcmTaskService {

    private Realm mRealm;
    private GoogleApiClient mApiClient;

    @Inject
    GeoRentingService mService;

    @Override
    public void onCreate() {
        super.onCreate();

        ((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Timber.i("Initializing Google");
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mApiClient.blockingConnect();

        /* PLAN:
         * 1. Remove all Google Geofences (using IDs from realm)
         * 2. Remove all Realm Geofences
         * 3. Get new Geofences from server
         * 4. Put new Geofences into Google, save IDs
         * 5. Put new Geofences into Realm, with IDs
         */
        Timber.i("removing old Fences");

        mRealm = Realm.getDefaultInstance();

        if (!removeOldGeofences(getOldRequestIDs())) {
            Timber.e("Could not remove old Geofences. Stopping.");
            mRealm.close();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        removeAllFences();

        Location lastLocation = getLocation();
        if(lastLocation == null) {
            Timber.e("Could not get Location. Stopping.");
            mRealm.close();
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        List<GeoFence> remoteFences;
        try {
            remoteFences = getRemoteGeoFences(lastLocation);
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("Could not get Remote Geofences. Stopping.");
            mRealm.close();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        if(remoteFences == null) {
            Timber.w("Not remote Fences were returned. Stopping.");
            mRealm.close();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        Timber.i("Adding new Fences (%d)", remoteFences.size());

        List<Fence> fences = new ArrayList<>();

        mRealm.beginTransaction();
        for(GeoFence remoteFence : remoteFences) {
            Timber.i("Adding fence: %s", remoteFence.id);
            Fence f = mRealm.createObject(Fence.class);
            f.setName(remoteFence.name);
            f.setLatitude(remoteFence.centerLat);
            f.setLongitude(remoteFence.centerLon);
            f.setRadius(remoteFence.radius);
            f.setId(remoteFence.id);
            fences.add(f);
        }

        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < fences.size(); i++) {
            Fence f = fences.get(i);
            geofences.add(createGeoFence(f, i));
            f.setGeofenceID(i + "");
        }
        mRealm.commitTransaction();

        if (geofences.size() > 0 && !addGeoFences(geofences)) {
            Timber.e("Could not add new Fences to google. Stopping.");
            mRealm.close();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        Timber.i("Done!");
        mRealm.close();
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @DebugLog
    private Location getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        final Location[] location = {null};

        LocationRequest request = LocationRequest.create()
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationCallback listener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                location[0] = result.getLastLocation();
            }
        };

        boolean success = LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request,
                listener, getMainLooper()).await().isSuccess();
        if(!success) return null;

        while(location[0] == null) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, listener);

        return location[0];
    }

    private List<GeoFence> getRemoteGeoFences(Location location) throws IOException {
        return mService.getFencesNear(location.getLatitude(), location.getLongitude(), 2000).execute().body();
    }

    private boolean addGeoFences(List<Geofence> fences) {
        GeofencingRequest r = new GeofencingRequest.Builder()
                .addGeofences(fences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Timber.i("Exec adding new Fences");

        return LocationServices.GeofencingApi.addGeofences(mApiClient,
                r, PendingIntent.getService(this, 0, new Intent(this, GeofenceTransitionsIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT)).await().isSuccess();
    }

    private void removeAllFences() {
        mRealm.beginTransaction();
        mRealm.where(Fence.class).findAll().clear();
        mRealm.commitTransaction();
    }

    private boolean removeOldGeofences(List<String> ids) {
        return ids.size() <= 0 || LocationServices.GeofencingApi.removeGeofences(mApiClient, ids).await().isSuccess();
    }

    private List<String> getOldRequestIDs() {
        List<String> reqIDs = new ArrayList<>();
        for (Fence fence : mRealm.where(Fence.class).findAll()) {
            reqIDs.add(fence.getGeofenceID());
        }
        return reqIDs;
    }

    private Geofence createGeoFence(Fence f, int id) {
        return new Geofence.Builder()
                .setCircularRegion(f.getLatitude(), f.getLongitude(), (int) f.getRadius())
                .setRequestId(id + "")
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(60 * 1000) // Notify every minute.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();

        initializeTasks(this);
    }

    public static void initializeTasks(Context ctx) {
        Task task = new PeriodicTask.Builder()
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .setService(UpdateGeofencesTask.class)
                .setPeriod(15 * 60) // Every 15 Minutes.
                .setFlex(30)
                .setUpdateCurrent(true)
                .setTag("GeofenceUpdater")
                .setPersisted(true)
                .build();

        GcmNetworkManager.getInstance(ctx).schedule(task);

        task = new OneoffTask.Builder()
                .setService(UpdateGeofencesTask.class)
                .setTag("UpdateFences")
                .setExecutionWindow(0L, 5L)
                .setUpdateCurrent(true)
                .setPersisted(true)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .build();
        GcmNetworkManager.getInstance(ctx).schedule(task);
    }

    public static void removeTasks(Context ctx) {
        GcmNetworkManager.getInstance(ctx).cancelAllTasks(UpdateGeofencesTask.class);
    }
}
