package de.alternadev.georenting.data.api;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;

@Module(includes = ApiModule.class)
public class InternalApiModule {
    @Provides
    @Singleton
    HttpUrl provideBaseURL() {
        return HttpUrl.parse(ApiModule.STAGING_API_URL);
    }
}
