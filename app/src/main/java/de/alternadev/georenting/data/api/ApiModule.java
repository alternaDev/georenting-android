package de.alternadev.georenting.data.api;

import android.app.Application;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.GeoRentingApplication;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

@Module
public class ApiModule {

    public static final String PRODUCTION_API_URL = "https://georenting.herokuapp.com/";
    public static final String STAGING_API_URL = "https://georenting-staging.herokuapp.com/";



    @Provides
    @Singleton
    @Named("sessionToken")
    RequestInterceptor provideRequestInterceptor(Application application) {
        return request -> request.addHeader("Authorization", ((GeoRentingApplication)application).getSessionToken().token);
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(OkHttpClient client, Endpoint endpoint, Gson gson, @Named("sessionToken") RequestInterceptor interceptor) {
        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(endpoint)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(interceptor)
                .build();
    }

    @Provides
    @Singleton
    GeoRentingService provideGeoRentingService(RestAdapter restAdapter) {
        return restAdapter.create(GeoRentingService.class);
    }
}
