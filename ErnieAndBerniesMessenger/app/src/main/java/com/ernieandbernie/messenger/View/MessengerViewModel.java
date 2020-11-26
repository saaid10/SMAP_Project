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

    public LiveData<User> getUser() {
        return user;
    }

    public void uploadProfilePicture(Uri uri) {
        repository.uploadProfilePicture(uri);
    }

    public void clearRepository() {
        repository.clearRepository();
    }

    public void updateCurrentUserLocationInDB(LatLng latLng) {
        repository.updateCurrentUserLocationInDB(latLng);
    }

    public void getMessagesFromChadId(String chadId, DataChangedListener<List<Message>> callback) {
        repository.getMessagesFromChadId(chadId, callback);
    }

    public void deleteFriend(String friendUid) {
        repository.deleteFriend(friendUid);
    }
}
