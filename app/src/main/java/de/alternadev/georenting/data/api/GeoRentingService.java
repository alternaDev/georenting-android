package de.alternadev.georenting.data.api;



import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import retrofit.Call;
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
    Call<Void> registerGcmToken(@Body GcmToken token);
}
