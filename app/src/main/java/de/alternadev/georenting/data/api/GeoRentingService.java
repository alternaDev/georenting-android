package de.alternadev.georenting.data.api;



import java.util.List;

import de.alternadev.georenting.data.api.model.ActivityItem;
import de.alternadev.georenting.data.api.model.CashStatus;
import de.alternadev.georenting.data.api.model.CostEstimate;
import de.alternadev.georenting.data.api.model.GcmToken;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.UpgradeSettings;
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
    @GET("application/upgrades")
    Observable<UpgradeSettings> getUpgradeSettings();

    @POST("users/auth")
    Observable<SessionToken> auth(@Body User user);

    @DELETE("users/auth")
    Observable<SessionToken> deAuth();

    @POST("users/refreshToken")
    Observable<SessionToken> refreshToken(@Body SessionToken token);

    @POST("users/me/gcm")
    Call<Void> registerGcmToken(@Body GcmToken token);

    @GET("users/me/history")
    Observable<List<ActivityItem>> getHistory(@Query("from") long from, @Query("to") long to);

    @GET("users/me/cash")
    Observable<CashStatus> getCash();

    @GET("fences")
    Call<List<GeoFence>> getFencesNear(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("radius") int radius, @Query("excludeOwn") boolean excludeOwn);

    @GET("fences")
    Observable<List<GeoFence>> getFencesNearObservable(@Query("latitude") double latitude, @Query("longitude") double longitude, @Query("radius") int radius, @Query("excludeOwn") boolean excludeOwn);

    @GET("fences")
    Observable<List<GeoFence>> getFencesBy(@Query("user") String user);

    @POST("fences/{fenceId}/visit")
    Call<Object> visitFence(@Path("fenceId") String fenceId);

    @POST("fences/estimateCost")
    Observable<CostEstimate> estimateCost(@Body GeoFence f);

    @POST("fences")
    Observable<GeoFence> createGeoFence(@Body GeoFence f);

    @DELETE("fences/{fenceId}")
    Observable<GeoFence> deleteGeoFence(@Path("fenceId") String fenceId);
}
