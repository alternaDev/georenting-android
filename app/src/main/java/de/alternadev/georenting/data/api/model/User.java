package de.alternadev.georenting.data.api.model;


import com.google.gson.annotations.SerializedName;

@org.parceler.Parcel
public class User {
    @SerializedName("google_token")
    public String googleToken;

    public String name;

    @SerializedName("avatar_url")
    public String avatarUrl;

    @SerializedName("cover_url")
    public String coverUrl;


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
