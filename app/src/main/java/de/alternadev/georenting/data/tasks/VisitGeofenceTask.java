package de.alternadev.georenting.data.tasks;

import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import java.io.IOException;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.auth.GoogleAuth;
import retrofit2.adapter.rxjava.HttpException;
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
        try {
            if (!mAuth.blockingSignIn()) {
                return GcmNetworkManager.RESULT_RESCHEDULE;
            }
        } catch(Exception e) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        String fenceId = taskParams.getExtras().getString(EXTRAS_FENCE_ID, "");

        if(TextUtils.isEmpty(fenceId)) return GcmNetworkManager.RESULT_FAILURE;

        try {
            Timber.d("Visiting fence: %s", fenceId);
            mService.visitFence(fenceId).execute().body();
            Timber.d("Visited Fence!");
        } catch (Exception e) {
            Timber.e(e, "Failed to Visit fence.");
            if(e instanceof HttpException)
                if(((HttpException) e).code() == 404) { // Not Found.
                    Timber.d("GeoFence not found. Returning Success.");
                    return GcmNetworkManager.RESULT_SUCCESS;
                }
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
