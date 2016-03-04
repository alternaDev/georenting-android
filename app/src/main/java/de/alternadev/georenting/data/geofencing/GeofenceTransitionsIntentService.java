package de.alternadev.georenting.data.geofencing;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import de.alternadev.georenting.R;

public class GeofenceTransitionsIntentService extends IntentService {
    public GeofenceTransitionsIntentService() {
        super("GeoRenting GeoFence Service");
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
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            for(Geofence f : triggeringGeofences)
                sendNotification(f);

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            //Log.i("Geofence", geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e("Geofence", "Invalid Type");
        }
    }

    private void sendNotification(Geofence f) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setContentTitle("Fence " + f.getRequestId())
                        .setContentText("Geofence!");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
         mNotificationManager.notify(Integer.valueOf(f.getRequestId()), mBuilder.build());
    }
}
