package com.ernieandbernie.messenger.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.View.MessengerListAdapter;

public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
    }

    private void setup() {
        RecyclerView recyclerView = findViewById(R.id.rcView);
      //  final MessengerListAdapter messengerListAdapter = new MessengerListAdapter(this, new MessengerListAdapter.OnMessengerClickListener())
    }
}