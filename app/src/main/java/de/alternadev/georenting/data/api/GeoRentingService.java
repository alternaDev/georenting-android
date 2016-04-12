package de.alternadev.georenting.data.api;



import java.util.List;

import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface GeoRentingService {
    @POST("users/auth")
    Observable<SessionToken> auth(@Body User user);

    @DELETE("users/auth")
    Observable<SessionToken> deAuth();

    @POST("users/me/gcm")
    Call<Void> registerGcmToken(@Body GcmToken token);

    @GET("fences")
    Call<List<GeoFence>> getFencesNear(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("radius") int radius);

    @GET("fences")
    Observable<List<GeoFence>> getFencesBy(@Query("user") String user);

    @POST("fences/{fenceId}/visit")
    Observable<Object> visitFence(@Path("fenceId") String fenceId);
}
