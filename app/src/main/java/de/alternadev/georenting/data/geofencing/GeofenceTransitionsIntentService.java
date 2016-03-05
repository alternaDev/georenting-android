package de.alternadev.georenting.data.geofencing;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.models.Fence;
import io.realm.Realm;
import timber.log.Timber;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String PREF_CURRENT_GEOFENCE = "currentGeofence";

    public GeofenceTransitionsIntentService() {
        super("GeoRenting GeoFence Service");
    }

    @Inject
    GeoRentingService mService;

    @Inject
    SharedPreferences mPreferences;

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
            Log.e("Geofence", errorMessage);
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
            Log.e("Geofence", "Invalid Type");
        }
    }

    private void notifyServer(Geofence f) {
        Timber.d("Blocking Sign in: " + ((GeoRentingApplication) getApplication()).blockingSignIn());

        Realm realm = Realm.getDefaultInstance();
        Fence fence = realm.where(Fence.class).equalTo("geofenceID", f.getRequestId()).findFirst();

        mService.visitFence(fence.getId()).toBlocking().first(); // TODO: Use real ID.
    }
}
