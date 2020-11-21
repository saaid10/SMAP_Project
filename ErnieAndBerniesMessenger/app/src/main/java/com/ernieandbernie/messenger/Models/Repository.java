package com.ernieandbernie.messenger.Models;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Repository {
    private static volatile Repository INSTANCE;
    private final FirebaseDatabase database;
    private final DatabaseReference databaseReference;
    private final FirebaseUser user;

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
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void updateCurrentUserLocationInDB(LatLng latLng) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("latitude", latLng.latitude);
        childUpdates.put("longitude", latLng.longitude);
        databaseReference.child("users").child(user.getUid()).updateChildren(childUpdates);
    }
}
