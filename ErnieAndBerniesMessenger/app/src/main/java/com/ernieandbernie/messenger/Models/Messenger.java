package com.ernieandbernie.messenger.Models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class Messenger {
    public List<Friend> friends;
    public Double latitude;
    public Double longitude;

    public Messenger() { }

    public Messenger(List<Friend> friends, Double latitude, Double longitude) {
        this.friends = friends;
        this.latitude = latitude;
        this.longitude = longitude;
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

