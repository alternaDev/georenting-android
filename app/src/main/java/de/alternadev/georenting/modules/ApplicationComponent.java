package de.alternadev.georenting.modules;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.GeoRentingApplication;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    void inject(GeoRentingApplication app);
}
