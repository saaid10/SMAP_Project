package com.ernieandbernie.messenger.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.View.FriendListViewModel;
import com.ernieandbernie.messenger.View.MessengerViewModel;

public class MessengerActivity extends AppCompatActivity {

    private MessengerViewModel messengerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        messengerViewModel = new ViewModelProvider(this).get(MessengerViewModel.class);

        
    }
}