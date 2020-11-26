package com.ernieandbernie.messenger.Models;

public class Chat {

    private String chatId;

    public Chat() { }

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
