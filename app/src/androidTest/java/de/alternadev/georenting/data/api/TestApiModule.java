package de.alternadev.georenting.data.api;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.SessionToken;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import rx.Observable;

public class TestApiModule extends ApiModule {

    private static final List<GeoFence> testFences = new ArrayList<>();

    static {
        testFences.add(new GeoFence(53.499160, 9.959948, 100, "Ralfs Area", "1"));
        testFences.add(new GeoFence(52.693967, 7.298851, 100, "Ulfs Hidden Palace", "2"));
    }

    @Override
    GeoRentingService provideGeoRentingService(Retrofit restAdapter) {
        GeoRentingService s = Mockito.mock(GeoRentingService.class);
        Mockito.when(s.refreshToken(Mockito.any())).thenReturn(Observable.just(new SessionToken()));
        Mockito.when(s.getFencesBy(Mockito.any())).thenReturn(Observable.just(testFences));
        return s;
    }

    @Override
    AvatarService provideAvatarService(HttpUrl baseUrl) {
        return new AvatarService(baseUrl);
    }

    @Override
    GoogleMapsStatic provideGoogleMapsStatic() {
        return new GoogleMapsStatic();
    }
}
