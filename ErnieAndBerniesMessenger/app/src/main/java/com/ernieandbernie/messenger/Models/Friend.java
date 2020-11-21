package com.ernieandbernie.messenger.Models;

public class Friend {
    public String displayName;
    public String uuid;


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
