package de.alternadev.georenting.data.ads;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AdModule {
    @Provides
    @Singleton
    AdmobAds provideAdmobAds() {
        return new AdmobAds();
    }
}
