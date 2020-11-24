package com.ernieandbernie.messenger.View;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;

public class FriendListViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<User> user;


    public FriendListViewModel(@NonNull Application application) {
        super(application);
        repository = Repository.getInstance(application.getApplicationContext());
        user = repository.getApplicationUser();
    }

    public LiveData<User> getUser() {
        return user;
    }

}
