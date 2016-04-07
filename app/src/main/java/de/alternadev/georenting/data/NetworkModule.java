package de.alternadev.georenting.data;

import android.app.Application;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import timber.log.Timber;

import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Module
public class NetworkModule {
    static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    @Provides
    OkHttpClient provideOkHttpClient(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        return
            new OkHttpClient.Builder().connectTimeout(10, SECONDS)
                    .readTimeout(10, SECONDS)
                    .writeTimeout(10, SECONDS)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .cache(cache)
                    .build();
    }

    @Provides
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                .downloader(new OkHttp3Downloader(client))
                .listener((picasso, uri, exception) -> Timber.e(exception, "Failed to load image %s: ", uri))
                .build();
    }

    @Provides
    Gson provideGson() {
        return new Gson();
    }
}
