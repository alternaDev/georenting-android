package de.alternadev.georenting.data.api.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.AvatarService;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class FcmListenerService extends FirebaseMessagingService {

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
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map data = message.getData();

        String type = (String) data.get("type");
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
            case "onFenceExpired":
                notifyFenceExpired(data);
                break;
        }
    }

    private void notifyUserForeignerEnteredFence(Map data) {
        // Someone entered your fence!
        int fenceId = Integer.valueOf((String) data.get("fenceId"));
        String fenceName = (String) data.get("fenceName");
        String visitorName = (String) data.get("visitorName");

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

        mNotificationManager.notify(-1 * (fenceId + " " + visitorName).hashCode(), mBuilder.build());
    }

    private void notifyUserVisitedFence(Map data) {
        // You entered a fence!
        int fenceId = Integer.valueOf((String) data.get("fenceId"));
        String fenceName = (String) data.get("fenceName");
        String ownerName = (String) data.get("ownerName");

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

        mNotificationManager.notify((fenceId + " " + ownerName).hashCode(), mBuilder.build());
    }

    private void notifyFenceExpired(Map data) {
        // Your fence expired.
        int fenceId = Integer.valueOf((String) data.get("fenceId"));
        String fenceName = (String) data.get("fenceName");

        String avatarUrl = mAvatarService.getAvatarUrl(fenceName);
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
                        .setContentTitle("Fence Expired!")
                        .setContentText("Your fence " + fenceName + "has died.");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(MainActivity.EXTRA_FRAGMENT, "history");

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, (int) (System.currentTimeMillis() & 0xfffffff), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify((fenceId + "expired").hashCode(), mBuilder.build());
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
