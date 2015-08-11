package de.alternadev.georenting.data.api.gcm;

import android.os.Bundle;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
    }
}
