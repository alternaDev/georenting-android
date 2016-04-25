package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.data.NetworkModule;
import de.alternadev.georenting.data.api.InternalApiModule;
import de.alternadev.georenting.data.auth.AuthModule;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class,
        InternalApiModule.class,
        AuthModule.class
    }
)
public interface GeoRentingComponent extends GeoRentingGraph {
}
