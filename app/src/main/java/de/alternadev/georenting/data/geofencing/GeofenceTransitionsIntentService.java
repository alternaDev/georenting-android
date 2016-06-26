package de.alternadev.georenting.data.geofencing;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.data.tasks.VisitGeofenceTask;
import de.alternadev.georenting.util.TaskUtil;
import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String PREF_CURRENT_GEOFENCE = "currentGeofence";
    private static final String PREF_FENCE_COOLDOWN = "geofenceCooldown";

    private static final String REMOTE_CONFIG_GEOFENCE_COOLDOWN = "geofence_cooldown";

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

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaults(R.xml.remote_config_defaults);

        TaskUtil.waitForTask(remoteConfig.fetch());
        remoteConfig.activateFetched();

        long fenceCooldown = remoteConfig.getLong(REMOTE_CONFIG_GEOFENCE_COOLDOWN);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            for(Geofence f : triggeringGeofences) {
                // Only trigger if not currently in a Geofence.
                boolean currentFenceCooldown = new Date().getTime() - mPreferences.getLong(PREF_FENCE_COOLDOWN + "_" + f.getRequestId(), 0) < fenceCooldown;
                boolean currentFenceOk = !mPreferences.getBoolean(PREF_CURRENT_GEOFENCE + "_" + f.getRequestId(), false);
                if (currentFenceCooldown && currentFenceOk) {
                    notifyServer(f);
                    mPreferences.edit().putLong(PREF_FENCE_COOLDOWN + "_" + f.getRequestId(), new Date().getTime()).apply();
                    mPreferences.edit().putBoolean(PREF_CURRENT_GEOFENCE+ "_" + f.getRequestId(), true).apply();
                }
            }

        } else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for(Geofence f : triggeringGeofences) {
                if(f.getRequestId().equals(UpdateGeofencesTask.GEOFENCE_UPDATE)) {
                    UpdateGeofencesTask.scheduleUpdate(getApplicationContext());
                } else {
                    mPreferences.edit().putBoolean(PREF_CURRENT_GEOFENCE + "_" + f.getRequestId(), false).apply();
                }
            }
        } else {
            // Log the error.
            Timber.e("Invalid Type");
        }
    }

    private void notifyServer(Geofence f) {
        Bundle taskData = new Bundle();
        taskData.putString(VisitGeofenceTask.EXTRAS_FENCE_ID, f.getRequestId());

        OneoffTask task = new OneoffTask.Builder()
                .setService(VisitGeofenceTask.class)
                .setTag("VisitGeofence")
                .setExecutionWindow(0L, 60L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setExtras(taskData)
                .setPersisted(true)
                .build();
        GcmNetworkManager.getInstance(this).schedule(task);

        Timber.d("Starting visit Task for fence %s", f.getRequestId());
    }
}
