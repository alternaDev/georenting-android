package de.alternadev.georenting.data.api;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;

@Module(includes = ApiModule.class)
public class InternalApiModule {
    @Provides
    @Singleton
    Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint(ApiModule.STAGING_API_URL);
    }
}
