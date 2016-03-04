package de.alternadev.georenting.data.api;

import javax.inject.Singleton;

import com.squareup.okhttp.HttpUrl;

import dagger.Module;
import dagger.Provides;
import retrofit.BaseUrl;

@Module(includes = ApiModule.class)
public class ProductionApiModule {
    @Provides
    @Singleton
    BaseUrl provideBaseURL() {
        return () -> HttpUrl.parse(ApiModule.PRODUCTION_API_URL);
    }
}
