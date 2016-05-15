package de.alternadev.georenting.data.api;

import android.app.Application;

import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.GeoRentingApplication;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;


@Module
public class ApiModule {

    public static final String PRODUCTION_API_URL = "https://georenting.herokuapp.com/";
    public static final String STAGING_API_URL = "https://georenting-staging.herokuapp.com/";


    @Provides
    @Singleton
    @Named("sessionToken")
    Interceptor provideInterceptor(Application application) {
        return chain -> {
            Request original = chain.request();

            if (((GeoRentingApplication) application).getSessionToken() != null && ((GeoRentingApplication) application).getSessionToken().token != null) {
                Request request = original.newBuilder().header("Authorization", ((GeoRentingApplication) application).getSessionToken().token)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
            return chain.proceed(original);
        };
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client, HttpUrl baseUrl, Gson gson) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

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
