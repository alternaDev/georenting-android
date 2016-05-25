package de.alternadev.georenting.data.api.model;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.Json;

public class GcmToken {
    @Json(name = "gcm_id")
    public String gcmToken;

    public GcmToken(String token) {
        this.gcmToken = token;
    }
    public GcmToken() {}
}
