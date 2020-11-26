package com.ernieandbernie.messenger.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ernieandbernie.messenger.Models.Message;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.View.MessageListAdapter;
import com.ernieandbernie.messenger.View.MessengerViewModel;

import java.util.List;

public class MessengerActivity extends AppCompatActivity {

    private TextView displayName;
    private EditText txtMessage;
    private ImageView profilePicture;
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
        displayName = findViewById(R.id.displayName);
        profilePicture = findViewById(R.id.profilePicture);

        txtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideKeyBoard(MessengerActivity.this, txtMessage);
            }
        });
        messengerViewModel.getCurrentFriend((user) -> {
            displayName.setText(user.displayName);
            Glide.with(MessengerActivity.this)
                    .load(user.storageUri)
                    .placeholder(R.drawable.ic_default_user)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePicture);
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messengerViewModel.sendNewMessage(txtMessage.getText().toString());
                txtMessage.setText("");
            }
        });
        MessageListAdapter messageListAdapter = new MessageListAdapter(this);
        recyclerView.setAdapter(messageListAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        messengerViewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                manager.scrollToPosition(messages.size() - 1);
                messageListAdapter.setMessages(messages);
            }
        });
    }

    private void hideKeyBoard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}