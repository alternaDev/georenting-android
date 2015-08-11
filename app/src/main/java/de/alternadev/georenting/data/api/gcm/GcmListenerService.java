package de.alternadev.georenting.data.api.gcm;

import android.os.Bundle;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    @Override
    @DebugLog
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Timber.d("Message from " + from);
    }
}
