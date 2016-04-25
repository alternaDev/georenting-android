package de.alternadev.georenting;

import de.alternadev.georenting.data.api.gcm.GcmListenerService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.data.geofencing.GeofenceTransitionsIntentService;
import de.alternadev.georenting.data.glide.OkHttpGlideModule;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.data.tasks.VisitGeofenceTask;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.HistoryFragment;
import de.alternadev.georenting.ui.main.MainActivity;
import de.alternadev.georenting.ui.main.MapFragment;
import de.alternadev.georenting.ui.main.MyGeofencesFragment;
import de.alternadev.georenting.ui.main.ProfileFragment;
import de.alternadev.georenting.ui.settings.SettingsFragment;

public interface GeoRentingGraph {
    void inject(GeoRentingApplication app);

    void inject(SignInActivity signInActivity);

    void inject(GcmRegistrationIntentService gcmRegistrationIntentService);

    void inject(MainActivity mainActivity);

    void inject(ProfileFragment profileFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(GeofenceTransitionsIntentService geofenceTransitionsIntentService);

    void inject(UpdateGeofencesTask updateGeofencesTask);

    void inject(OkHttpGlideModule okHttpGlideModule);

    void inject(MyGeofencesFragment myGeofencesFragment);

    void inject(VisitGeofenceTask visitGeofenceTask);

    void inject(MapFragment mapFragment);

    void inject(HistoryFragment historyFragment);

    void inject(GoogleAuth googleAuth);

    void inject(GcmListenerService gcmListenerService);
}
