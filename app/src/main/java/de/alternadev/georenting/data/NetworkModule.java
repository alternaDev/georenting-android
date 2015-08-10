package de.alternadev.georenting.data;

import android.app.Application;
import android.net.Uri;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.Gson;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

import static com.jakewharton.byteunits.DecimalByteUnit.MEGABYTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@Module
public class NetworkModule {
    static final int DISK_CACHE_SIZE = (int) MEGABYTES.toBytes(50);

    @Provides
    OkHttpClient provideOkHttpClient(Application app) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, SECONDS);
        client.setReadTimeout(10, SECONDS);
        client.setWriteTimeout(10, SECONDS);
        client.networkInterceptors().add(new StethoInterceptor());

        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = null;
        try {
            cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.setCache(cache);

        return client;
    }

    @Provides
    Picasso providePicasso(Application app, OkHttpClient client) {
        return new Picasso.Builder(app)
                .downloader(new OkHttpDownloader(client))
                .listener((picasso, uri, exception) -> Timber.e(exception, "Failed to load image %s: ", uri))
                .build();
    }

    @Provides
    Gson provideGson() {
        return new Gson();
    }
}
