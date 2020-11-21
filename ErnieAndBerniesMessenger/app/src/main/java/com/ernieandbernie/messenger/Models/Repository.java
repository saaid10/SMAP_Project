package com.ernieandbernie.messenger.Models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
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
        storageReference = FirebaseStorage.getInstance().getReference().child(firebaseUser.getUid());
        loadApplicationUser();

        // This is test stuff
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).child(Constants.FRIENDS).orderByChild(Constants.DISPLAY_NAME).equalTo("123").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                }
                Log.d(TAG, "onDataChange: " + snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(error.getMessage());
            }
        });
        databaseReference.child(Constants.USERS).orderByChild(Constants.LATITUDE).startAt(56.1731682 - 1).endAt(56.1731682 + 1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                }
                Log.d(TAG, "onDataChange: " + snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(error.getMessage());
            }
        });
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

    public void loadApplicationUser() {
        databaseReference.child(Constants.USERS).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                applicationUser.postValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                makeToast(error.getMessage());
            }
        });
    }

    public LiveData<User> getApplicationUser() {
        if (applicationUser == null) {
            applicationUser = new MutableLiveData<>();
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
        UploadTask uploadTask = storageReference.child(Constants.PROFILE_PICTURE).putFile(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d(TAG, "onComplete: " + downloadUri);
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
}
