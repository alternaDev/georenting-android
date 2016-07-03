package de.alternadev.georenting.data.api;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;


@Module
public class ApiModule {

    public static final String PRODUCTION_API_URL = "https://georenting.herokuapp.com/";
    public static final String STAGING_API_URL = "https://georenting-staging.herokuapp.com/";


    @Provides
    @Singleton
    GeoRentingService provideGeoRentingService(Retrofit restAdapter) {
        return restAdapter.create(GeoRentingService.class);
    }

    @Provides
    @Singleton
    @Named("unAuthed")
    GeoRentingService proviedUnAuthedGeoRentingService(@Named("unAuthed") Retrofit restAdapter) {
        return restAdapter.create(GeoRentingService.class);
    }

    @Provides
    @Singleton
    AvatarService provideAvatarService(HttpUrl baseUrl) {
        return new AvatarService(baseUrl);
    }

    @Provides
    @Singleton
    GoogleMapsStatic provideGoogleMapsStatic() {
        return new GoogleMapsStatic();
    }
}
