package de.alternadev.georenting.data.api;

import okhttp3.HttpUrl;

/**
 * Created by jhbruhn on 14.05.16.
 */
public class AvatarService {
    private final HttpUrl mBaseUrl;

    public AvatarService(HttpUrl baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    public String getAvatarUrl(String username) {
        return mBaseUrl.url().toString() + "users/" + username + "/avatar";
    }
}
