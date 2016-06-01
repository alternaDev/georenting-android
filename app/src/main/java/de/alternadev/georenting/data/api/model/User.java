package de.alternadev.georenting.data.api.model;


import com.squareup.moshi.Json;

@org.parceler.Parcel
public class User {
    @Json(name = "google_token")
    public String googleToken;

    @Json(name = "name")
    public String name;

    @Json(name = "avatar_url")
    public String avatarUrl;

    @Json(name = "cover_url")
    public String coverUrl;

    @Json(name = "balance")
    public double balance;

    @Json(name = "ID")
    public int id;

    public User(){}
    public User(String googleToken) {
        this.googleToken = googleToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "googleToken='" + googleToken + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", id=" + id +
                '}';
    }

}
