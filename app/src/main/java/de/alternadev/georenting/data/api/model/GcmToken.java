package de.alternadev.georenting.data.api.model;

import com.google.gson.annotations.SerializedName;

public class GcmToken {
    @SerializedName("gcm_id")
    public String gcmToken;

    public GcmToken(String token) {
    }
    public GcmToken() {}
}
