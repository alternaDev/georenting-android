package de.alternadev.georenting.data.api;

import javax.inject.Singleton;

import okhttp3.HttpUrl;

import dagger.Module;
import dagger.Provides;


@Module(includes = ApiModule.class)
public class ProductionApiModule {
    @Provides
    @Singleton
    HttpUrl provideBaseURL() {
        return HttpUrl.parse(ApiModule.PRODUCTION_API_URL);
    }
}
