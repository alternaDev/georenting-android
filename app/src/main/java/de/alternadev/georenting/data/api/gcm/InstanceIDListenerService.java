package de.alternadev.georenting.data.api.gcm;

import android.content.Intent;

public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        startService(new Intent(this, GcmRegistrationIntentService.class));
    }
}
