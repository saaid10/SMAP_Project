package com.ernieandbernie.messenger.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.Service.MessengerService;
import com.ernieandbernie.messenger.Util.Constants;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Method;

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
                        return;
                    }
                    repository.uploadProfilePicture(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setup();
        updateCurrentUserLocationInDB();

        if(savedInstanceState == null)
            startService(new Intent(this, MessengerService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    // https://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method m = menu.getClass().getDeclaredMethod(
                        "setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "onMenuOpened", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            FirebaseAuth.getInstance().signOut();
            AuthUI.getInstance().signOut(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    stopService(new Intent(FriendListActivity.this, MessengerService.class));
                    repository.clearRepository();
                    Intent intent = new Intent(FriendListActivity.this, FirebaseAuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // finish();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCurrentUserLocationInDB() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null)
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
                startActivity(new Intent(FriendListActivity.this, MapsActivity.class));
            }
        });


        RecyclerView recyclerView = findViewById(R.id.rcView);
        //  final MessengerListAdapter messengerListAdapter = new MessengerListAdapter(this, new MessengerListAdapter.OnMessengerClickListener())

        // repository.messageSetupTest();
        // repository.getChatTest();
        // repository.newMessageTest("BU5dfBrUhZWKZQts2eHUKPj9ERj1");
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(Constants.CONTENT_TYPE_IMAGE);
        pickImageLauncher.launch(intent);
    }
}