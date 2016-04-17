package de.alternadev.georenting.data.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import okhttp3.OkHttpClient;

/**
 * Created by jhbruhn on 17.04.16.
 */
public class OkHttpGlideModule implements GlideModule {

    @Inject
    OkHttpClient client;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        ((GeoRentingApplication) context.getApplicationContext()).getComponent().inject(this);
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
