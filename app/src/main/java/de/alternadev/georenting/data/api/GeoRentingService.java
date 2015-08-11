package de.alternadev.georenting.data.api;


import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface GeoRentingService {
    @POST("/users/auth")
    Observable<SessionToken> auth(@Body User user);

    @POST("/users/me/gcm")
    Object registerGcmToken(@Body GcmToken token);
}
