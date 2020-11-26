package com.ernieandbernie.messenger.View;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ernieandbernie.messenger.Models.CallbackInterfaces.DataChangedListener;
import com.ernieandbernie.messenger.Models.Message;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MessengerViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<User> user;


    public MessengerViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application.getApplicationContext());
        user = repository.getApplicationUser();
    }

    public LiveData<List<Message>> getMessages() {
        return repository.getMessages();
    }

    public void sendNewMessage(String message) {
        repository.sendNewMessage(message);
    }

    public void getCurrentFriend(DataChangedListener<User> callback) {
        repository.getCurrentFriend(callback);
    }
}
