package de.alternadev.georenting;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.data.NetworkModule;
import de.alternadev.georenting.data.api.ApiModule;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.MainActivity;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class,
        ApiModule.class
    }
)
public interface GeoRentingComponent {
    void inject(GeoRentingApplication app);

    void inject(SignInActivity signInActivity);

    void inject(GcmRegistrationIntentService gcmRegistrationIntentService);

    void inject(MainActivity mainActivity);
}
