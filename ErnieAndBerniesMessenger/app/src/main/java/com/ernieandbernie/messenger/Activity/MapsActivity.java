package com.ernieandbernie.messenger.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.widget.Toast;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;
import com.ernieandbernie.messenger.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        repository = Repository.getInstance(getApplicationContext());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        moveCamera();
        addPotentialFriendsToMap();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), (String) marker.getTag(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void addPotentialFriendsToMap() {
        repository.getUsersCloseTo().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                mMap.clear();
                for (User user : users) {
                    LatLng userLocation = new LatLng(user.latitude, user.longitude);
                    if (user.uid.equals(repository.getApplicationUser().getValue().uid)) {
                        mMap.addMarker(new MarkerOptions().position(userLocation).title(getString(R.string.you_are_here))).setTag(user.uid);
                    } else {
                        mMap.addMarker(new MarkerOptions().position(userLocation).title(user.displayName)).setTag(user.uid);
                    }
                }
            }
        });
    }

    private void moveCamera() {
        User user = repository.getApplicationUser().getValue();
        LatLng userLocation = new LatLng(user.latitude, user.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
    }
}