package com.ernieandbernie.messenger.Models;

import com.google.firebase.database.Exclude;

public class Request {

    @Exclude
    public String requestFromUid;
    @Exclude
    public String requestFromDisplayName;

    public Request() {}
}
