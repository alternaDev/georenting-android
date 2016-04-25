package de.alternadev.georenting.data.tasks;

import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.auth.GoogleAuth;
import timber.log.Timber;

/**
 * Created by jhbruhn on 18.04.16.
 */
public class VisitGeofenceTask extends GcmTaskService {
    public static final String EXTRAS_FENCE_ID = "fenceId";

    @Inject
    GeoRentingService mService;

    @Inject
    GoogleAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        ((GeoRentingApplication) getApplication()).getComponent().inject(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        if(!mAuth.blockingSignIn()) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        String fenceId = taskParams.getExtras().getString(EXTRAS_FENCE_ID, "");

        if(TextUtils.isEmpty(fenceId)) return GcmNetworkManager.RESULT_FAILURE;

        try {
            Timber.d("Visiting fence: %s", fenceId);
            mService.visitFence(fenceId).toBlocking().first();
            Timber.d("Visited Fence!");
        } catch (Exception e) {
            Timber.e(e, "Failed to Visit fence.");
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        return GcmNetworkManager.RESULT_FAILURE;
    }
}
