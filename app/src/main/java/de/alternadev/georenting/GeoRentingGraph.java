package de.alternadev.georenting;

import de.alternadev.georenting.data.api.fcm.FcmListenerService;
import de.alternadev.georenting.data.api.fcm.FcmRegistrationIntentService;
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.data.geofencing.GeofenceTransitionsIntentService;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.data.tasks.VisitGeofenceTask;
import de.alternadev.georenting.ui.CreateGeofenceActivity;
import de.alternadev.georenting.ui.GeofenceDetailActivity;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.HistoryFragment;
import de.alternadev.georenting.ui.main.MainActivity;
import de.alternadev.georenting.ui.main.MapFragment;
import de.alternadev.georenting.ui.main.MyGeofencesFragment;
import de.alternadev.georenting.ui.main.ProfileFragment;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceAdapter;
import de.alternadev.georenting.ui.settings.SettingsFragment;

public interface GeoRentingGraph {
    void inject(GeoRentingApplication app);

    void inject(SignInActivity signInActivity);

    void inject(FcmRegistrationIntentService gcmRegistrationIntentService);

    void inject(MainActivity mainActivity);

    void inject(ProfileFragment profileFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(GeofenceTransitionsIntentService geofenceTransitionsIntentService);

    void inject(UpdateGeofencesTask updateGeofencesTask);

    void inject(MyGeofencesFragment myGeofencesFragment);

    void inject(VisitGeofenceTask visitGeofenceTask);

    void inject(MapFragment mapFragment);

    void inject(HistoryFragment historyFragment);

    void inject(GoogleAuth googleAuth);

    void inject(FcmListenerService gcmListenerService);

    void inject(GeofenceAdapter geofenceAdapter);

    void inject(GeofenceDetailActivity geofenceDetailActivity);

    void inject(CreateGeofenceActivity createGeofenceActivity);
}
