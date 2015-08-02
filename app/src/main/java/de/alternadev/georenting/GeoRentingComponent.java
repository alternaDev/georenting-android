package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.data.NetworkModule;
import de.alternadev.georenting.ui.SignInActivity;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class
    }
)
public interface GeoRentingComponent {
    void inject(GeoRentingApplication app);

    void inject(SignInActivity signInActivity);
}
