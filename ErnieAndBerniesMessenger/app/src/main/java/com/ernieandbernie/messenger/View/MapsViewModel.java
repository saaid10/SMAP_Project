package com.ernieandbernie.messenger.View;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ernieandbernie.messenger.Models.CallbackInterfaces.DataChangedListener;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MapsViewModel extends AndroidViewModel {

    private final Repository repository;
    private final LiveData<User> user;

    public MapsViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application.getApplicationContext());
        user = repository.getApplicationUser();
    }

    public FirebaseUser getFirebaseUser() {
        return repository.getFirebaseUser();
    }

    public void sendFriendRequest(String requestUid) {
        repository.sendFriendRequest(requestUid);
    }

    public LiveData<List<User>> getUsersCloseTo() {
        return repository.getUsersCloseTo();
    }

    public void getApplicationUserOnce(DataChangedListener<User> callback) {
        repository.getApplicationUserOnce(callback);
    }

    public LiveData<User> getApplicationUser() {
        return user;
    }
}
