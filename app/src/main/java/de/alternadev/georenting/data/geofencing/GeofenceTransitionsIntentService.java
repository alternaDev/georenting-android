package de.alternadev.georenting.data.geofencing;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.data.tasks.VisitGeofenceTask;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String PREF_CURRENT_GEOFENCE = "currentGeofence";

    public GeofenceTransitionsIntentService() {
        super("GeoRenting GeoFence Service");
    }

    @Inject
    SharedPreferences mPreferences;

    @Inject
    BriteDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        ((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(
                    geofencingEvent.getErrorCode());
            Timber.e(errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            for(Geofence f : triggeringGeofences) {
                // Only trigger if not currently in a Geofence.
                if (!mPreferences.getBoolean(PREF_CURRENT_GEOFENCE + "_" + f.getRequestId(), false)) {
                    notifyServer(f);
                    mPreferences.edit().putBoolean(PREF_CURRENT_GEOFENCE + "_" + f.getRequestId(), true).apply();
                }
            }

        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for(Geofence f : triggeringGeofences) {
                mPreferences.edit().putBoolean(PREF_CURRENT_GEOFENCE + "_" + f.getRequestId(), false).apply();
            }
        } else {
            // Log the error.
            Timber.e("Invalid Type");
        }
    }

    private void notifyServer(Geofence f) {
        List<Fence> result = new ArrayList<>();
        mDatabase.setLoggingEnabled(true);
        try (Cursor cursor = mDatabase.query(Fence.SELECT_BY_GEOFENCE_ID, f.getRequestId())) {
            Timber.d("Count: %d", cursor.getCount());
            while (cursor.moveToNext()) {
                result.add(Fence.MAPPER.map(cursor));
            }
        }

        if(result.size() == 0) {
            Timber.e("Can not visit fence. %s", result);
            return;
        }
        Fence fence = result.get(0);

        if(fence == null) {
            Timber.e("Can not visit fence. %s", fence);
            return;
        }

        Bundle taskData = new Bundle();
        taskData.putString(VisitGeofenceTask.EXTRAS_FENCE_ID, fence._id() + "");

        OneoffTask task = new OneoffTask.Builder()
                .setService(VisitGeofenceTask.class)
                .setTag("VisitGeofence")
                .setExecutionWindow(0L, 60L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setExtras(taskData)
                .setPersisted(true)
                .build();
        GcmNetworkManager.getInstance(this).schedule(task);

        Timber.d("Starting visit Task for fence %s", fence.geofenceId());
    }
}
