package com.ernieandbernie.messenger.Models;

import java.util.ArrayList;
import java.util.List;

public class Pair {

    public List<String> members = new ArrayList<>();

    public Pair() {}

    public Pair(String user1, String user2) {
        members.add(user1);
        members.add(user2);
    }
}
