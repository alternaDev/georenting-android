package de.alternadev.georenting.data.api;

import com.squareup.okhttp.HttpUrl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.BaseUrl;

@Module(includes = ApiModule.class)
public class InternalApiModule {
    @Provides
    @Singleton
    BaseUrl provideBaseURL() {
        return () -> HttpUrl.parse(ApiModule.STAGING_API_URL);
    }
}
