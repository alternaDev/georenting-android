package de.alternadev.georenting.data;

import android.app.Application;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.moshi.Moshi;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.alternadev.georenting.data.api.DateAdapter;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Module
public class NetworkModule {
    static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    @Provides
    OkHttpClient provideOkHttpClient(Application app, @Named("sessionToken") Interceptor tokenInterceptor) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return
            new OkHttpClient.Builder().connectTimeout(20, SECONDS)
                    .readTimeout(20, SECONDS)
                    .writeTimeout(20, SECONDS)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .addInterceptor(tokenInterceptor)
                    .cache(cache)
                    .build();
    }

    @Provides
    @Singleton
    Moshi provideMoshi() {
        return new Moshi.Builder().add(new DateAdapter()).build();
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app).downloader(new OkHttp3Downloader(client)).build();
    }
}
