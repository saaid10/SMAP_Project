package com.ernieandbernie.messenger.Models;

import com.google.firebase.database.Exclude;

public class Friend {
    public String displayName;
    public String uuid;

    @Exclude
    public String profileUrl;


    public Friend() { }

    public Friend(String displayName, String uuid) {
        this.displayName = displayName;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
