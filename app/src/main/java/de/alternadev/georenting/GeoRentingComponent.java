package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.data.NetworkModule;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class
    }
)
public interface GeoRentingComponent {
    void inject(GeoRentingApplication app);
}
