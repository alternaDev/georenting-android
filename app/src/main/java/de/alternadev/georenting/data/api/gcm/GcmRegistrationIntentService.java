package de.alternadev.georenting.data.api.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GcmToken;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GcmRegistrationIntentService extends IntentService {

    private static final String CURRENT_GCM_TOKEN = "current_gcm_token";
    @Inject
    GeoRentingService mGeoRentingService;

    @Inject
    SharedPreferences mPreferences;

    public GcmRegistrationIntentService() {
        super("Register GCM");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String gcmTokenString = getToken();

            if (mPreferences.getString(CURRENT_GCM_TOKEN, "").equals(gcmTokenString)) {
                Timber.i("GCM already registered.");
                return;
            }

            try {
                mGeoRentingService.registerGcmToken(new GcmToken(gcmTokenString)).execute();
                mPreferences.edit().putString(CURRENT_GCM_TOKEN, gcmTokenString).apply();
                Timber.i("GCM Token submitted.");
            } catch (IOException  e ) {
                e.printStackTrace();
                Timber.e(e, "Sending GCM token failed. ");
            }

        } catch (IOException e) {
            e.printStackTrace();
            Timber.e(e, "Generating GCM token failed. ");
        }

    }

    @DebugLog
    private String getToken() throws IOException {
        InstanceID instanceID = InstanceID.getInstance(this);
        return instanceID.getToken(getString(R.string.gcm_default_sender_id),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }
}
