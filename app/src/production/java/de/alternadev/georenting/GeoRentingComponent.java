package de.alternadev.georenting;

import javax.inject.Singleton;

import dagger.Component;
import de.alternadev.georenting.GeoRentingGraph;
import de.alternadev.georenting.data.NetworkModule;
import de.alternadev.georenting.data.api.ApiModule;
import de.alternadev.georenting.data.api.ProductionApiModule;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.main.MainActivity;
import de.alternadev.georenting.ui.main.ProfileFragment;

@Singleton
@Component(
    modules = {
        GeoRentingModule.class,
        NetworkModule.class,
        ProductionApiModule.class
    }
)
public interface GeoRentingComponent extends GeoRentingGraph {

}