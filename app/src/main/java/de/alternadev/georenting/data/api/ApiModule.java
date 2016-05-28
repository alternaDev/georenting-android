package de.alternadev.georenting.data.api;

import android.app.Application;

import com.squareup.moshi.Moshi;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.GeoRentingApplication;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;


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
    AvatarService provideAvatarService(HttpUrl baseUrl) {
        return new AvatarService(baseUrl);
    }

    @Provides
    @Singleton
    GoogleMapsStatic provideGoogleMapsStatic() {
        return new GoogleMapsStatic();
    }
}
