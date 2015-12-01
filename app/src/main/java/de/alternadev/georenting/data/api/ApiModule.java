package de.alternadev.georenting.data.api;

import android.app.Application;

import com.google.gson.Gson;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.GeoRentingApplication;
import retrofit.BaseUrl;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;


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

            Request request = original.newBuilder().header("Authorization", ((GeoRentingApplication) application).getSessionToken().token)
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        };
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client, BaseUrl baseUrl, Gson gson, @Named("sessionToken") Interceptor interceptor) {
        client.interceptors().add(interceptor);
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    GeoRentingService provideGeoRentingService(Retrofit restAdapter) {
        return restAdapter.create(GeoRentingService.class);
    }
}
