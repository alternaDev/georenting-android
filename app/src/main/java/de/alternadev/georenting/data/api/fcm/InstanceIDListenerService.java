package de.alternadev.georenting.data.api.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import de.alternadev.georenting.data.tasks.RegisterFcmTask;
import timber.log.Timber;

public class InstanceIDListenerService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.d("Refreshed token: %s", refreshedToken);
        RegisterFcmTask.scheduleRegisterFcm(this);
    }
}
