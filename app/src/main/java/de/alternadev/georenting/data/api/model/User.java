package de.alternadev.georenting.data.api.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("google_token")
    public String googleToken;

    public User(){}
    public User(String googleToken) {
        this.googleToken = googleToken;
    }
}
