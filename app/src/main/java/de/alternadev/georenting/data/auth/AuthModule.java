package de.alternadev.georenting.data.auth;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by jhbruhn on 25.04.16.
 */
@Module
public class AuthModule {
    @Provides
    @Singleton
    GoogleAuth provideGoogleAuth(Application application) {
        return new GoogleAuth(application);
    }
}
