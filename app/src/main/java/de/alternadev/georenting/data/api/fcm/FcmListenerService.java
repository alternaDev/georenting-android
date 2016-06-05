package de.alternadev.georenting.data.api.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.moshi.Moshi;
import com.squareup.picasso.Picasso;
import com.squareup.sqlbrite.BriteDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.AvatarService;
import de.alternadev.georenting.data.models.Notification;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class FcmListenerService extends FirebaseMessagingService {

    private static final int NOTIFICATION_GEOFENCE_OPERATIONS_ID = 42;
    private static final String KEY_NOTIFICATION_TYPE = "type";
    private static final String NOTIFICATION_TYPE_FOREIGN_FENCE_ENTERED = "onForeignFenceEntered";
    private static final String NOTIFICATION_TYPE_OWN_FENCE_ENTERED = "onOwnFenceEntered";
    private static final String NOTIFICATION_TYPE_FENCE_EXPIRED = "onFenceExpired";
    private static final String NOTIFICATION_TYPE_SYNC = "sync";

    @Inject
    SharedPreferences mPrefs;

    @Inject
    AvatarService mAvatarService;

    @Inject
    Picasso mPicasso;

    @Inject
    Moshi mMoshi;

    @Inject
    BriteDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        ((GeoRentingApplication) getApplicationContext()).getComponent().inject(this);
    }

    @Override
    @DebugLog
    public void onMessageReceived(RemoteMessage message) {
        Map data = message.getData();

        String type = (String) data.get(KEY_NOTIFICATION_TYPE);
        if(type == null) return;

        switch (type) {
            case NOTIFICATION_TYPE_SYNC:
                startSync();
                break;
            case NOTIFICATION_TYPE_FOREIGN_FENCE_ENTERED:
                if(mPrefs.getBoolean(getString(R.string.pref_key_notify_visit), true)) {
                    storeNotification(data);
                    try {
                        postGeoFenceOperationsNotification();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case NOTIFICATION_TYPE_OWN_FENCE_ENTERED:
                if(mPrefs.getBoolean(getString(R.string.pref_key_notify_visited), true)) {
                    storeNotification(data);
                    try {
                        postGeoFenceOperationsNotification();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case NOTIFICATION_TYPE_FENCE_EXPIRED:
                if(mPrefs.getBoolean(getString(R.string.pref_key_notify_expired), true)) {
                    storeNotification(data);
                    try {
                        postGeoFenceOperationsNotification();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void storeNotification(Map data) {
        String json = mMoshi.adapter(Map.class).toJson(data);
        Notification.insert(mDatabase, Notification.builder().data(json).build());
    }

    private void postGeoFenceOperationsNotification() throws IOException {
        List<Notification> notifications = Notification.getAll(mDatabase);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_person_black_24dp)
                .setContentTitle(getString(R.string.app_name));

        if(notifications.size() > 0) {
            Map data = mMoshi.adapter(Map.class).fromJson(notifications.get(0).data());
            String name = (String) data.get("visitorName");
            if (name == null) {
                name = (String) data.get("ownerName");
            }
            String avatarUrl = mAvatarService.getAvatarUrl(name);
            Timber.d("AvatarURL: %s", avatarUrl);
            Bitmap avatar;
            try {
                avatar = mPicasso.load(avatarUrl).get();
                mBuilder.setLargeIcon(avatar);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(notifications.size() == 1) {
            mBuilder.setContentText(Html.fromHtml(notificationDataToString(mMoshi.adapter(Map.class).fromJson(notifications.get(0).data()))));
        } else {
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            for (Notification n : notifications) {
                inboxStyle.addLine(Html.fromHtml(notificationDataToString(mMoshi.adapter(Map.class).fromJson(n.data()))));
            }

            mBuilder.setStyle(inboxStyle);
            inboxStyle.setSummaryText(notifications.size() + "");
            mBuilder.setContentText(getSummary(notifications));
        }

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(MainActivity.EXTRA_FRAGMENT, MainActivity.FRAGMENT_HISTORY);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, (int) (System.currentTimeMillis() & 0xfffffff), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_GEOFENCE_OPERATIONS_ID, mBuilder.build());
    }

    private String getSummary(List<Notification> notifications) throws IOException {
        int visitCount = 0, foreignVisitCount = 0, expiryCount = 0;
        for(Notification n : notifications) {
            Map data = mMoshi.adapter(Map.class).fromJson(n.data());
            if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_FOREIGN_FENCE_ENTERED)) {
                foreignVisitCount++;
            } else if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_OWN_FENCE_ENTERED)) {
                visitCount++;
            } else if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_FENCE_EXPIRED)) {
                expiryCount++;
            }
        }

        StringBuilder b = new StringBuilder();
        if(visitCount > 0) {
            b.append(visitCount + " Fences visited, ");
        }
        if(foreignVisitCount > 0) {
            b.append(foreignVisitCount + " visitors, ");
        }
        if(expiryCount > 0) {
            b.append(expiryCount + " Fences expired.");
        }
        return b.toString();
    }

    private String notificationDataToString(Map data) {
        if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_FOREIGN_FENCE_ENTERED)) {
            String fenceName = (String) data.get("fenceName");
            String ownerName = (String) data.get("ownerName");
            return "<b>You entered</b>: " + fenceName + " by " + ownerName + "";
        } else if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_OWN_FENCE_ENTERED)) {
            String fenceName = (String) data.get("fenceName");
            String visitorName = (String) data.get("visitorName");
            return "<b>" + visitorName + " entered</b>: " + fenceName;
        } else if(data.get(KEY_NOTIFICATION_TYPE).equals(NOTIFICATION_TYPE_FENCE_EXPIRED)) {
            String fenceName = (String) data.get("fenceName");
            return "<b>Expired</b>: " + fenceName;
        }
        return data.toString();
    }

    private void startSync() {
        UpdateGeofencesTask.scheduleUpdate(this.getApplicationContext());
    }
}
