package com.ernieandbernie.messenger.Models.CallbackInterfaces;

import com.ernieandbernie.messenger.Models.Message;

import java.util.List;

public interface GetMessagesCallback {
    void callback(List<Message> messages);
}
