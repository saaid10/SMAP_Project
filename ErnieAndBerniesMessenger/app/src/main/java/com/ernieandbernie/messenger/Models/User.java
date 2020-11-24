package com.ernieandbernie.messenger.Models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    /** Key = uid || value = displayName */
    public HashMap<String, String> friends;
    public Double latitude;
    public Double longitude;
    public String displayName;
    public String storageUri;

    @Exclude
    public String uid;

    public User() {
    }

    public User(String displayName, HashMap<String, String> friends, Double latitude, Double longitude, String storageUri) {
        this.friends = friends;
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.storageUri = storageUri;
    }

    public HashMap<String, String> getFriends() {
        return friends;
    }

    public List<Friend> getFriendsAsList() {
        List<Friend> friendsList = new ArrayList<>();

        for (Map.Entry<String, String> entry : friends.entrySet()) {
            friendsList.add(new Friend(entry.getValue(), entry.getKey()));
        }
        return friendsList;
    }

    public void setFriends(HashMap<String, String> friends) {
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

