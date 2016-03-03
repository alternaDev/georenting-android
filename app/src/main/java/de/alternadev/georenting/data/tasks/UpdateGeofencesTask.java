package de.alternadev.georenting.data.tasks;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import de.alternadev.georenting.data.GeofenceTransitionsIntentService;
import de.alternadev.georenting.data.models.Fence;
import io.realm.Realm;

public class UpdateGeofencesTask extends GcmTaskService {

    @Override
    public void onCreate() {
        super.onCreate();

        //((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        GoogleApiClient mApiClient = new GoogleApiClient.Builder(this)
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

        Realm realm = Realm.getDefaultInstance();
        List<String> reqIDs = new ArrayList<>();
        for (Fence fence : realm.where(Fence.class).findAll()) {
            reqIDs.add(fence.getGeofenceID());
        }

        if (!LocationServices.GeofencingApi.removeGeofences(mApiClient, reqIDs).await().isSuccess()) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        realm.beginTransaction();
        realm.where(Fence.class).findAll().clear();
        realm.commitTransaction();

        // TODO: Get Geofences from Server


        List<Fence> fences = new ArrayList<>();
        realm.beginTransaction();
        Fence f = new Fence();
        f.setName("Ulf's Castle");
        f.setLatitude(53.128932);
        f.setLongitude(8.189734);
        f.setRadius(100);
        f.setOwner("Peter");
        f.setId("1");
        fences.add(f);
        f = new Fence();
        f.setName("Pedas Chillout Area");
        f.setLatitude(53.120569);
        f.setLongitude(8.192223);
        f.setRadius(100);
        f.setOwner("Ralf");
        f.setId("2");
        fences.add(f);
        realm.commitTransaction();


        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < fences.size(); i++) {
            f = fences.get(i);
            geofences.add(new Geofence.Builder()
                    .setCircularRegion(f.getLatitude(), f.getLongitude(), (int) f.getRadius())
                    .setRequestId(i + "")
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(30000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
            realm.beginTransaction();
            f.setGeofenceID(i + "");
            realm.commitTransaction();
        }

        GeofencingRequest r = new GeofencingRequest.Builder()
                .addGeofences(geofences)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return GcmNetworkManager.RESULT_FAILURE;
        }
        LocationServices.GeofencingApi.addGeofences(mApiClient,
                r, PendingIntent.getService(this, 0, new Intent(this, GeofenceTransitionsIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT));



        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
