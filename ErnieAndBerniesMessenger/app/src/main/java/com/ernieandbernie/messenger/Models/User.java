package com.ernieandbernie.messenger.Models;

import com.google.firebase.database.Exclude;

import java.util.List;

public class User {
    public List<Friend> friends;
    public Double latitude;
    public Double longitude;
    public String displayName;
    public String storageUri;

    @Exclude
    public String uid;

    public User() {
    }

    public User(String displayName, List<Friend> friends, Double latitude, Double longitude, String storageUri) {
        this.friends = friends;
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.storageUri = storageUri;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getStorageUri() {
        return storageUri;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setStorageUri(String storageUri) {
        this.storageUri = storageUri;
    }

}

