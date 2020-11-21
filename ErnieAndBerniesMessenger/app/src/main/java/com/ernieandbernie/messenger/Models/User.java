package com.ernieandbernie.messenger.Models;

import java.util.List;

public class User {
    public List<Friend> friends;
    public Double latitude;
    public Double longitude;
    public String displayName;

    public User() {
    }

    public User(String displayName, List<Friend> friends, Double latitude, Double longitude) {
        this.friends = friends;
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
    }

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

