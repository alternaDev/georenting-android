package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {GeoRentingModule.class})
public interface GeoRentingComponent {
    void inject(GeoRentingApplication app);
}
