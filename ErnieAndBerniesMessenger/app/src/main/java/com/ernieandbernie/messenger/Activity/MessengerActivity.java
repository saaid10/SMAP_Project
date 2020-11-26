package com.ernieandbernie.messenger.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ernieandbernie.messenger.Models.Message;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.View.FriendListViewModel;
import com.ernieandbernie.messenger.View.MessageListAdapter;
import com.ernieandbernie.messenger.View.MessengerViewModel;

import java.util.List;

public class MessengerActivity extends AppCompatActivity {

    private TextView txtMessage;
    private Button btnSend;

    private MessengerViewModel messengerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        messengerViewModel = new ViewModelProvider(this).get(MessengerViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.reyclerview_message_list);
        btnSend = findViewById(R.id.btnSend);
        txtMessage = findViewById(R.id.txtMessage);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messengerViewModel.sendNewMessage(txtMessage.getText().toString());
                txtMessage.setText("");
            }
        });
        MessageListAdapter messageListAdapter = new MessageListAdapter(this);

        recyclerView.setAdapter(messageListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        messengerViewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                messageListAdapter.setMessages(messages);
            }
        });
    }
}