package com.ernieandbernie.messenger.Models;

public class Message {
    // public String key;
    public String content;
    public String senderUid;
    public String senderDisplayName;
    public String timestamp;

    public Message() {}


    // https://stackoverflow.com/questions/8077530/android-get-current-timestamp
    public void setTimestamp() {
        long tsLong = System.currentTimeMillis()/1000;
        this.timestamp = Long.toString(tsLong);
    }
}
