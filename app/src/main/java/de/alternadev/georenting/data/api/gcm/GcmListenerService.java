package de.alternadev.georenting.data.api.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.AvatarService;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.HistoryFragment;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    @Inject
    SharedPreferences mPrefs;

    @Inject
    AvatarService mAvatarService;

    @Inject
    Picasso mPicasso;

    @Override
    public void onCreate() {
        super.onCreate();
        ((GeoRentingApplication) getApplicationContext()).getComponent().inject(this);
    }

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
                if(mPrefs.getBoolean(getString(R.string.pref_key_notify_visit), true))
                    notifyUserVisitedFence(data);
                break;
            case "onOwnFenceEntered":
                if(mPrefs.getBoolean(getString(R.string.pref_key_notify_visited), true))
                    notifyUserForeignerEnteredFence(data);
                break;
        }
    }

    private void notifyUserForeignerEnteredFence(Bundle data) {
        // Someone entered your fence!
        int fenceId = Integer.valueOf(data.getString("fenceId"));
        String fenceName = data.getString("fenceName", "Unnamed Fence");
        String visitorName = data.getString("visitorName", "Unknown User");

        String avatarUrl = mAvatarService.getAvatarUrl(visitorName);
        Timber.d("AvatarURL: %s", avatarUrl);
        Bitmap avatar;
        try {
            avatar = mPicasso.load(avatarUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // You entered a fence!
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(avatar)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setAutoCancel(true)
                        .setContentTitle("Fence Visited!")
                        .setContentText(visitorName + " visited your fence " + fenceName + "!");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(MainActivity.EXTRA_FRAGMENT, "history");

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, (int) (System.currentTimeMillis() & 0xfffffff), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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

        String avatarUrl = mAvatarService.getAvatarUrl(ownerName);
        Timber.d("AvatarURL: %s", avatarUrl);
        Bitmap avatar;
        try {
            avatar = mPicasso.load(avatarUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // You entered a fence!
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setLargeIcon(avatar)
                        .setSmallIcon(R.drawable.ic_person_black_24dp)
                        .setAutoCancel(true)
                        .setContentTitle("Fence Visited!")
                        .setContentText("You visited fence '" + fenceName + "' by " + ownerName + "!");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(MainActivity.EXTRA_FRAGMENT, "history");

        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(this, (int) (System.currentTimeMillis() & 0xfffffff), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
