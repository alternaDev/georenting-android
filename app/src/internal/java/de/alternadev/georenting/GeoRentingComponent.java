package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.data.NetworkModule;
import de.alternadev.georenting.data.api.InternalApiModule;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class,
        InternalApiModule.class
    }
)
public interface GeoRentingComponent extends GeoRentingGraph {
}
