package com.ernieandbernie.messenger.Models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.ernieandbernie.messenger.Models.CallbackInterfaces.DataChangedListener;
import com.ernieandbernie.messenger.Util.Constants;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    private static final String TAG = "Repository";

    private static volatile Repository INSTANCE;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    private final GeoFire geoFire;
    private final FirebaseUser firebaseUser;
    private final Context context;

    private MutableLiveData<User> applicationUser = new MutableLiveData<>();
    private MutableLiveData<List<User>> usersCloseTo = new MutableLiveData<>();
    private MutableLiveData<Request> friendRequests;
    private MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private MutableLiveData<List<Message>> messagesForNotification = new MutableLiveData<>();

    private final Map<DatabaseReference, ValueEventListener> listeners = new HashMap<>();

    private String currentChadId;
    private final Map<DatabaseReference, ValueEventListener> chadListener = new HashMap<>();
    private String currentFriendUid;
    private GeoQuery geoQuery;

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
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        geoFire = new GeoFire(databaseReference.child("geofire"));
        loadApplicationUser();
        loadUsersCloseToUser();
        setupFriendRequests();
    }

    public LiveData<Request> getFriendRequests() {
        if (friendRequests == null) {
            friendRequests = new MutableLiveData<>();
            setupFriendRequests();
        }
        return friendRequests;
    }

    public LiveData<User> getApplicationUser() {
        if (applicationUser == null) {
            applicationUser = new MutableLiveData<>();
            loadApplicationUser();
        }
        return applicationUser;
    }

    public LiveData<List<User>> getUsersCloseTo() {
        if (usersCloseTo == null) {
            usersCloseTo = new MutableLiveData<>();
            loadUsersCloseToUser();
        }
        return usersCloseTo;
    }

    public LiveData<List<Message>> getMessages() {
        if (messages == null) {
            messages = new MutableLiveData<>();
        }
        return messages;
    }


    public void updateCurrentUserLocationInDB(LatLng latLng) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(Constants.LATITUDE, latLng.latitude);
        childUpdates.put(Constants.LONGITUDE, latLng.longitude);
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).updateChildren(childUpdates);

        geoFire.setLocation(firebaseUser.getUid(), new GeoLocation(latLng.latitude, latLng.longitude));
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
        User user = new User();
        user.displayName = snapshot.child(Constants.DISPLAY_NAME).getValue(String.class);
        user.latitude = snapshot.child(Constants.LATITUDE).getValue(Double.class);
        user.longitude = snapshot.child(Constants.LONGITUDE).getValue(Double.class);
        user.storageUri = snapshot.child(Constants.STORAGE_URI).getValue(String.class);
        user.uid = snapshot.getKey();
        // user.friends = user.friends != null ? user.friends : new HashMap<>();
        for (DataSnapshot child : snapshot.child("friends").getChildren()) {
            user.friends.add(child.getValue(Friend.class));
        }
        return user;
    }

    public void getApplicationUserOnce(DataChangedListener<User> callback) {
        databaseReference
                .child(Constants.USERS)
                .child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onDataChanged(createUserFromSnapshot(snapshot));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        makeToast(error.getMessage());
                    }
                });
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
        getApplicationUserOnce(new DataChangedListener<User>() {
            @Override
            public void onDataChanged(User data) {
                geoQuery = geoFire.queryAtLocation(new GeoLocation(data.latitude, data.longitude), 100);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        databaseReference.child(Constants.USERS).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = createUserFromSnapshot(snapshot);

                                if (usersCloseTo.getValue() != null) {
                                    List<User> users = usersCloseTo.getValue();
                                    users.add(user);
                                    usersCloseTo.postValue(users);
                                } else {
                                    usersCloseTo.postValue(Collections.singletonList(user));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {

                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });

                /*Query query = databaseReference
                        .child(Constants.USERS)
                        .orderByChild(Constants.LATITUDE)
                        .startAt(data.latitude - 1)
                        .endAt(data.latitude + 1);

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

                listeners.put(query.getRef(), listener);*/
            }
        });

    }


    public void sendFriendRequest(String requestUid) {
        databaseReference.child(Constants.REQUESTS).child(requestUid).child(firebaseUser.getUid()).setValue(firebaseUser.getDisplayName());
    }

    private void setupFriendRequests() {
        DatabaseReference ref = databaseReference
                .child(Constants.REQUESTS)
                .child(firebaseUser.getUid());

        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) return;
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

    public DatabaseReference getProfileUrlByUid(String requestFromUid) {
        return databaseReference.child(Constants.USERS).child(requestFromUid).child(Constants.STORAGE_URI);
    }

    public void getDisplayNameByUid(String uid, DataChangedListener<String> callback) {
        databaseReference.child(Constants.USERS).child(uid).child(Constants.DISPLAY_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onDataChanged(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addNewFriend(String newFriendUid, String newFriendDisplayName) {
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).child(Constants.FRIENDS).push().setValue(new Friend(newFriendDisplayName, newFriendUid));

        databaseReference.child(Constants.USERS).child(newFriendUid).child(Constants.FRIENDS).push().setValue(new Friend(firebaseUser.getDisplayName(), firebaseUser.getUid()));

        createChatForNewFriends(newFriendUid);

        deleteFriendRequest(newFriendUid);
    }

    private void createChatForNewFriends(String newFriendUid) {
        DatabaseReference ref = databaseReference.child(Constants.CHADS);
        String key = ref.push().getKey();

        Chat chat = new Chat(key);
        ref.child(getFirebaseUser().getUid()).child(newFriendUid).setValue(chat);
        ref.child(newFriendUid).child(getFirebaseUser().getUid()).setValue(chat);

    }

    public void deleteFriendRequest(String requestFromUid) {
        databaseReference.child(Constants.REQUESTS).child(firebaseUser.getUid()).child(requestFromUid).removeValue();
    }

    public void clearRepository() {
        for (Map.Entry<DatabaseReference, ValueEventListener> listener : listeners.entrySet()) {
            listener.getKey().removeEventListener(listener.getValue());
        }
        geoQuery.removeAllListeners();
        friendRequests = new MutableLiveData<>();
        usersCloseTo = new MutableLiveData<>();
        applicationUser = new MutableLiveData<>();
        messages = new MutableLiveData<>();
        INSTANCE = null;
    }


    /**
     * Not sure this works as intended. Maybe use function below: setupMessages
     */
    public void getMessagesFromChadId(String chadId, DataChangedListener<List<Message>> callback) {
        DatabaseReference ref = databaseReference.child(Constants.MESSAGES).child(chadId);
        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messageList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    messageList.add(child.getValue(Message.class));
                }

                callback.onDataChanged(messageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        listeners.put(ref, listener);
    }

    private void setupMessages(String chadId) {
        currentChadId = chadId;
        DatabaseReference ref = databaseReference.child(Constants.MESSAGES).child(chadId);
        ValueEventListener listener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messageList = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    messageList.add(child.getValue(Message.class));
                }

                messages.postValue(messageList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chadListener.put(ref, listener);
        listeners.put(ref, listener);
    }

    public void deleteFriend(String friendId) {
        databaseReference.child(Constants.USERS)
                .child(firebaseUser.getUid())
                .child(Constants.FRIENDS)
                .orderByChild(Constants.UUID)
                .equalTo(friendId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey() != null) {
                                snapshot.getRef().child(child.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        databaseReference.child(Constants.USERS)
                .child(friendId)
                .child(Constants.FRIENDS)
                .orderByChild(Constants.UUID)
                .equalTo(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (child.getKey() != null) {
                                snapshot.getRef().child(child.getKey()).removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        databaseReference.child(Constants.CHADS).child(firebaseUser.getUid()).child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Chat chat = snapshot.getValue(Chat.class);

                databaseReference.child(Constants.MESSAGES).child(chat.getChatId()).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setActiveChat(String friendUid) {
        messages.postValue(Collections.emptyList());
        currentFriendUid = friendUid;
        getApplicationUserOnce((user) -> {
            databaseReference.child(Constants.CHADS).child(user.uid).child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (Map.Entry<DatabaseReference, ValueEventListener> listener : chadListener.entrySet()) {
                        listener.getKey().removeEventListener(listener.getValue());
                    }

                    Chat chad = snapshot.getValue(Chat.class);
                    if (chad == null) return;
                    setupMessages(chad.getChatId());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    public void sendNewMessage(String message) {
        getApplicationUserOnce((user) -> {
            DatabaseReference ref = databaseReference.child(Constants.MESSAGES).child(currentChadId);
            String key = ref.push().getKey();

            Message newMessage = new Message();
            newMessage.senderUid = user.uid;
            newMessage.setTimestamp();
            newMessage.senderDisplayName = user.displayName;
            newMessage.content = message;

            ref.child(key).setValue(newMessage);
        });
    }

    public void getCurrentFriend(DataChangedListener<User> callback) {
        databaseReference.child(Constants.USERS).child(currentFriendUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onDataChanged(createUserFromSnapshot(snapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void test() {
        databaseReference.child(Constants.CHADS).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Chat chad = child.getValue(Chat.class);
                    if (chad == null) continue;

                    DatabaseReference ref = databaseReference.child(Constants.MESSAGES).child(chad.getChatId());
                    ValueEventListener listener = ref.orderByChild("timestamp").startAt(Long.toString(System.currentTimeMillis()/1000)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d(TAG, "onDataChange: " + snapshot);
                            List<Message> messageList = new ArrayList<>();

                            for(DataSnapshot child : snapshot.getChildren()) {
                                messageList.add(child.getValue(Message.class));
                            }

                            if (messagesForNotification.getValue() != null) {
                                messageList.addAll(messagesForNotification.getValue());
                            }
                            messagesForNotification.postValue(messageList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    listeners.put(ref, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

