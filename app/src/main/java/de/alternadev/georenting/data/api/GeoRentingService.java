package de.alternadev.georenting.data.api;


import com.squareup.okhttp.Call;

import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import rx.Observable;

public interface GeoRentingService {
    @POST("users/auth")
    Observable<SessionToken> auth(@Body User user);

    @DELETE("users/auth")
    Observable<SessionToken> deAuth();

    @POST("users/me/gcm")
    Call registerGcmToken(@Body GcmToken token);
}
