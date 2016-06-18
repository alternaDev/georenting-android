package de.alternadev.georenting.data.tasks;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.auth.GoogleAuth;
import hugo.weaving.DebugLog;
import timber.log.Timber;


public class RegisterFcmTask extends GcmTaskService {
    public static final String CURRENT_GCM_TOKEN = "current_gcm_token";

    @Inject
    GeoRentingService mGeoRentingService;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GoogleAuth mAuth;

    @Override
    public int onRunTask(TaskParams taskParams) {
        ((GeoRentingApplication) getApplication()).getComponent().inject(this);

        if(!mAuth.blockingSignIn()) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        try {
            String gcmTokenString = getToken();

            if (mPreferences.getString(CURRENT_GCM_TOKEN, "").equals(gcmTokenString)) {
                Timber.i("GCM already registered.");
                return GcmNetworkManager.RESULT_SUCCESS;
            }

            try {
                mGeoRentingService.registerGcmToken(new GcmToken(gcmTokenString)).execute();
                mPreferences.edit().putString(CURRENT_GCM_TOKEN, gcmTokenString).apply();
                Timber.i("GCM Token submitted.");
            } catch (IOException e ) {
                e.printStackTrace();
                Timber.e(e, "Sending GCM token failed. ");
                return GcmNetworkManager.RESULT_RESCHEDULE;

            }

        } catch (IOException e) {
            e.printStackTrace();
            Timber.e(e, "Generating GCM token failed. ");
            return GcmNetworkManager.RESULT_RESCHEDULE;

        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @DebugLog
    private String getToken() throws IOException {
        return FirebaseInstanceId.getInstance().getToken();
    }

    public static void scheduleRegisterFcm(Context ctx) {
        Task task = new OneoffTask.Builder()
                .setService(RegisterFcmTask.class)
                .setTag("RegisterFCM")
                .setExecutionWindow(0L, 5L)
                .setUpdateCurrent(true)
                .setPersisted(true)
                .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                .build();
        GcmNetworkManager.getInstance(ctx).schedule(task);
    }
}
