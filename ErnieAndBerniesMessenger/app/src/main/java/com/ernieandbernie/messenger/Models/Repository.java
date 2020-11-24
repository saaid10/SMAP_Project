package com.ernieandbernie.messenger.Models;

import android.content.Context;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ernieandbernie.messenger.Models.CallbackInterfaces.GetDisplayNameByUidCallback;
import com.ernieandbernie.messenger.Service.MessengerService;
import com.ernieandbernie.messenger.Util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    private static final String TAG = "Repository";

    private static volatile Repository INSTANCE;
    private final FirebaseDatabase database;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    private final FirebaseUser firebaseUser;
    private final Context context;

    private MutableLiveData<User> applicationUser = new MutableLiveData<>();
    private MutableLiveData<List<User>> usersCloseTo;
    private MutableLiveData<Request> friendRequests;

    private final Map<DatabaseReference, ValueEventListener> listeners = new HashMap<>();

    public static Repository getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (Repository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private Repository(Context application) {
        context = application.getApplicationContext();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        loadApplicationUser();
    }

    public void updateCurrentUserLocationInDB(LatLng latLng) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Constants.LATITUDE, latLng.latitude);
        childUpdates.put(Constants.LONGITUDE, latLng.longitude);
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).updateChildren(childUpdates);
    }

    public void setCurrentUserDisplayName() {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Constants.DISPLAY_NAME, firebaseUser.getDisplayName());
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).updateChildren(childUpdates);
    }

    private void loadApplicationUser() {
        DatabaseReference ref = databaseReference.child(Constants.USERS).child(firebaseUser.getUid());
        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationUser.postValue(createUserFromSnapshot(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(error.getMessage());
            }
        });
        listeners.put(ref, listener);
    }

    private User createUserFromSnapshot(DataSnapshot snapshot) {
        User user = snapshot.getValue(User.class);
        user.uid = snapshot.getKey();
        user.friends = user.friends != null ? user.friends : new HashMap<>();
        return user;
    }

    public LiveData<User> getApplicationUser() {
        if (applicationUser == null) {
            applicationUser = new MutableLiveData<>();
            loadApplicationUser();
        }
        return applicationUser;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    private void makeToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void uploadProfilePicture(Uri data) {
        UploadTask uploadTask = storageReference.child(firebaseUser.getUid()).child(Constants.PROFILE_PICTURE).putFile(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.child(firebaseUser.getUid()).child(Constants.PROFILE_PICTURE).getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    updateStorageUriOnUser(downloadUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    public void updateStorageUriOnUser(Uri downloadUri) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Constants.STORAGE_URI, downloadUri.toString());
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).updateChildren(childUpdates);
    }

    private void loadUsersCloseToUser() {
        Query query = databaseReference
                .child(Constants.USERS)
                .orderByChild(Constants.LATITUDE)
                .startAt(getApplicationUser().getValue().latitude - 1)
                .endAt(getApplicationUser().getValue().latitude + 1);

        ValueEventListener listener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> users = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    users.add(createUserFromSnapshot(dataSnapshot));
                }
                usersCloseTo.postValue(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(error.getMessage());
            }
        });

        listeners.put(query.getRef(), listener);
    }

    public LiveData<List<User>> getUsersCloseTo() {
        if (usersCloseTo == null) {
            usersCloseTo = new MutableLiveData<>();
            loadUsersCloseToUser();
        }
        return usersCloseTo;
    }

    public void sendFriendRequest(String requestUid) {
        databaseReference.child(Constants.REQUESTS).child(requestUid).child(firebaseUser.getUid()).setValue(firebaseUser.getDisplayName());
    }

    public void setupFriendRequests() {
        DatabaseReference ref = databaseReference
                .child(Constants.REQUESTS)
                .child(firebaseUser.getUid());

        ValueEventListener listener = ref.limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Request> requests = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Request request = new Request();
                    request.requestFromUid = child.getKey();
                    request.requestFromDisplayName = (String) child.getValue();
                    requests.add(request);
                }

                friendRequests.postValue(requests.get(0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listeners.put(ref, listener);
    }

    public LiveData<Request> getFriendRequests() {
        if (friendRequests == null) {
            friendRequests = new MutableLiveData<>();
            setupFriendRequests();
        }
        return friendRequests;
    }

    public DatabaseReference getProfileUrlByUid(String requestFromUid) {
        return databaseReference.child(Constants.USERS).child(requestFromUid).child(Constants.STORAGE_URI);
    }

    public void getDisplayNameByUid(String uid, GetDisplayNameByUidCallback callback) {
        databaseReference.child(Constants.USERS).child(uid).child(Constants.DISPLAY_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.callback(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addNewFriend(String newFriendUid, String newFriendDisplayName) {
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).child(Constants.FRIENDS).child(newFriendUid).setValue(newFriendDisplayName);

        databaseReference.child(Constants.USERS).child(newFriendUid).child(Constants.FRIENDS).child(firebaseUser.getUid()).setValue(firebaseUser.getDisplayName());

        deleteFriendRequest(newFriendUid);
    }

    public void deleteFriendRequest(String requestFromUid) {
        databaseReference.child(Constants.REQUESTS).child(firebaseUser.getUid()).child(requestFromUid).removeValue();
    }

    public void clearRepository() {
        for (Map.Entry<DatabaseReference, ValueEventListener> listener : listeners.entrySet()) {
            listener.getKey().removeEventListener(listener.getValue());
        }
        friendRequests = new MutableLiveData<>();
        usersCloseTo = new MutableLiveData<>();
        applicationUser = new MutableLiveData<>();
        INSTANCE = null;
    }
}

