package de.alternadev.georenting.data.tasks;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.alternadev.georenting.data.geofencing.GeofenceTransitionsIntentService;
import de.alternadev.georenting.data.models.Fence;
import io.realm.Realm;

public class UpdateGeofencesTask extends GcmTaskService {

    private Realm mRealm;
    private GoogleApiClient mApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        //((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.i("Task", "Initializing Google");
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
        Log.i("Task", "removing old Fences");

        mRealm = Realm.getDefaultInstance();

        if(!removeOldGeofences(getOldRequestIDs())) {
            mRealm.close();
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        removeAllFences();

        // TODO: Get Geofences from Server

        Log.i("Task", "Adding new Fences");

        List<Fence> fences = new ArrayList<>();
        mRealm.beginTransaction();
        Fence f = mRealm.createObject(Fence.class);
        f.setName("Ulf's Castle");
        f.setLatitude(53.128932);
        f.setLongitude(8.189734);
        f.setRadius(100);
        f.setOwner("Peter");
        f.setId("1");
        fences.add(f);
        f = mRealm.createObject(Fence.class);
        f.setName("Pedas Chillout Area");
        f.setLatitude(53.120569);
        f.setLongitude(8.192223);
        f.setRadius(100);
        f.setOwner("Ralf");
        f.setId("2");
        fences.add(f);

        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < fences.size(); i++) {
            f = fences.get(i);
            geofences.add(createGeoFence(f, i));
            f.setGeofenceID(i + "");
        }
        mRealm.commitTransaction();

        if(!addGeoFences(geofences)) {
            mRealm.close();
            return GcmNetworkManager.RESULT_FAILURE;
        }

        Log.i("Task", "Done!");
        mRealm.close();
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private boolean addGeoFences(List<Geofence> fences) {
        GeofencingRequest r = new GeofencingRequest.Builder()
                .addGeofences(fences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Log.i("Task", "Exec adding new Fences");

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
                .setNotificationResponsiveness(30000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }
}
