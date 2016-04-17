package de.alternadev.georenting.data.api.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.ui.SignInActivity;
import hugo.weaving.DebugLog;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    @Override
    @DebugLog
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        String type = data.getString("type");
        if(type == null) return;

        switch (type) {
            case "sync":
                startSync();
                break;
            case "onForeignFenceEntered":
                notifyUserVisitedFence(data);
                break;
            case "onOwnFenceEntered":
                notifyUserForeignerEnteredFence(data);
                break;
        }
    }

    private void notifyUserForeignerEnteredFence(Bundle data) {
        // Someone entered your fence!
        int fenceId = Integer.valueOf(data.getString("fenceId"));
        String fenceName = data.getString("fenceName", "Unnamed Fence");
        String visitorName = data.getString("visitorName", "Unknown User");

        // You entered a fence!
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setContentTitle("Fence Visited!")
                        .setContentText(visitorName + " visited your fence " + fenceName + "!");
        Intent resultIntent = new Intent(this, SignInActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SignInActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(-1 * fenceId, mBuilder.build());
    }

    private void notifyUserVisitedFence(Bundle data) {
        // You entered a fence!
        int fenceId = Integer.valueOf(data.getString("fenceId"));
        String fenceName = data.getString("fenceName", "Unnamed Fence");
        String ownerName = data.getString("ownerName", "Unknown User");

        // You entered a fence!
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setContentTitle("Fence Visited!")
                        .setContentText("You visited fence '" + fenceName + "' by " + ownerName + "!");
        Intent resultIntent = new Intent(this, SignInActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(SignInActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(fenceId, mBuilder.build());
    }

    private void startSync() {
        Task task = new OneoffTask.Builder()
                .setService(UpdateGeofencesTask.class)
                .setTag("UpdateFences")
                .setExecutionWindow(0L, 5L)
                .setUpdateCurrent(true)
                .setPersisted(true)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .build();
        GcmNetworkManager.getInstance(this).schedule(task);
    }
}
