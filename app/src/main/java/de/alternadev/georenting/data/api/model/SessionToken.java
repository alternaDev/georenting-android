package de.alternadev.georenting.data.api.model;

import com.squareup.moshi.Json;

public class SessionToken {
    @Json(name = "token")
    public String token;

    @Json(name = "user")
    public User user;

    @Override
    public String toString() {
        return "SessionToken{" +
                "token='" + token + '\'' +
                ", user=" + user +
                '}';
    }
}
