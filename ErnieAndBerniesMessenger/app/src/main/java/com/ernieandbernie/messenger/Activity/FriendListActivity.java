package com.ernieandbernie.messenger.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.Util.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

public class FriendListActivity extends AppCompatActivity {

    private static final String TAG = "FriendListActivity";
    private Repository repository;
    private FusedLocationProviderClient fusedLocationProviderClient;


    // View bindings
    private Button btnProfilePic, btnAddFriends;

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    updateCurrentUserLocationInDB();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() == null) {
                        //Display an error
                        return;
                    }

                    repository.uploadProfilePicture(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
        setup();
        updateCurrentUserLocationInDB();
    }

    private void updateCurrentUserLocationInDB() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    repository.updateCurrentUserLocationInDB(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            // requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }


    private void setup() {
        repository = Repository.getInstance(getApplicationContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // View Bindings
        btnProfilePic = findViewById(R.id.btnProfilePic);
        btnAddFriends = findViewById(R.id.btnAddFriends);
        btnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        btnAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO:
            }
        });


        RecyclerView recyclerView = findViewById(R.id.rcView);
        //  final MessengerListAdapter messengerListAdapter = new MessengerListAdapter(this, new MessengerListAdapter.OnMessengerClickListener())
        repository.getApplicationUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                Log.d(TAG, "onChanged: " + user);
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(Constants.CONTENT_TYPE_IMAGE);
        pickImageLauncher.launch(intent);
    }
}