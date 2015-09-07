package de.alternadev.georenting;

import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.MainActivity;
import de.alternadev.georenting.ui.main.ProfileFragment;

public interface GeoRentingGraph {
    void inject(GeoRentingApplication app);

    void inject(SignInActivity signInActivity);

    void inject(GcmRegistrationIntentService gcmRegistrationIntentService);

    void inject(MainActivity mainActivity);

    void inject(ProfileFragment profileFragment);
}
