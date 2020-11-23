package com.ernieandbernie.messenger.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.User;
import com.ernieandbernie.messenger.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                // Don't send friend request to yourself...
                if (marker.getTag().equals(repository.getFirebaseUser().getUid()))
                    return false;


                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(R.string.send_friend_request)
                        .setMessage(getString(R.string.friend_request_dialog, marker.getTitle()))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repository.sendFriendRequest((String) marker.getTag());
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
                return false;
            }
        });
    }

    private void addPotentialFriendsToMap() {
        repository.getUsersCloseTo().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                mMap.clear();
                if (users.isEmpty()) return;
                repository.getApplicationUser().observe(MapsActivity.this, new Observer<User>() {
                    @Override
                    public void onChanged(User applicationUser) {
                        removeObserver();
                        Set<String> friendIds = applicationUser.friends.keySet();
                        for (User user : users) {
                            if (friendIds.contains(user.uid)) {
                                continue;
                            }
                            LatLng userLocation = new LatLng(user.latitude, user.longitude);
                            if (user.uid.equals(applicationUser.uid)) {
                                addMarker(user, getString(R.string.you_are_here), userLocation);
                            } else {
                                addMarker(user, user.displayName, userLocation);
                            }
                        }
                    }
                });
            }
        });
    }

    private void addMarker(User user, String title, LatLng userLocation) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        marker.setTag(user.uid);
        loadMarkerIcon(user.storageUri, marker);
    }

    private void moveCamera() {
        repository.getApplicationUser().observe(this, (applicationUser) -> {
            removeObserver();
            LatLng userLocation = new LatLng(applicationUser.latitude, applicationUser.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
        });
    }

    private void loadMarkerIcon(String url, final Marker marker) {
        Glide.with(this).asBitmap().fitCenter().load(url).into(new CustomTarget<Bitmap>(90, 90) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                marker.setIcon(icon);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round));
            }
        });
    }

    private void removeObserver() {
        repository.getApplicationUser().removeObservers(this);
    }
}