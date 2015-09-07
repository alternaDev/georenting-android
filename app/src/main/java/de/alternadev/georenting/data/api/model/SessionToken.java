package de.alternadev.georenting.data.api.model;

public class SessionToken {
    public String token;
    public User user;

    @Override
    public String toString() {
        return "SessionToken{" +
                "token='" + token + '\'' +
                ", user=" + user +
                '}';
    }
}
