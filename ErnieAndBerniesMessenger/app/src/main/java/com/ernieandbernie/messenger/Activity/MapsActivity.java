package com.ernieandbernie.messenger.Activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ernieandbernie.messenger.Models.User;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.View.MapsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private final HashMap<String, Marker> markers = new HashMap<>();
    private MapsViewModel mapsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
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
        moveCamera();
        addPotentialFriendsToMap();
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // Don't send friend request to yourself...
                if (mapsViewModel.getFirebaseUser().getUid().equals(marker.getTag()))
                    return;


                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(R.string.send_friend_request)
                        .setMessage(getString(R.string.friend_request_dialog, marker.getTitle()))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mapsViewModel.sendFriendRequest((String) marker.getTag());
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });
    }

    private void addPotentialFriendsToMap() {
        mapsViewModel.getUsersCloseTo().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                clearMarkers();

                if (users.isEmpty()) {
                    return;
                }

                // Set<String> friendIds = applicationUser.getFriendUids();
                for (User user : users) {
                    if (user.getFriendUids().contains(mapsViewModel.getFirebaseUser().getUid())) {
                        continue;
                    }
                    LatLng userLocation = new LatLng(user.latitude, user.longitude);
                    if (user.uid.equals(mapsViewModel.getFirebaseUser().getUid())) {
                        addMarker(user, getString(R.string.you_are_here), userLocation);
                    } else {
                        addMarker(user, user.displayName, userLocation);
                    }
                }
            }
        });
    }

    private void clearMarkers() {
        if (!markers.isEmpty()) {
            for (Marker marker : markers.values()) {
                marker.remove();
            }
        }
    }

    private void addMarker(User user, String title, LatLng userLocation) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        marker.setTag(user.uid);
        loadMarkerIcon(user.storageUri, marker);
    }

    private void moveCamera() {
        mapsViewModel.getApplicationUserOnce((applicationUser) -> {
            LatLng userLocation = new LatLng(applicationUser.latitude, applicationUser.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));
        });
    }

    private void loadMarkerIcon(String url, final Marker marker) {
        Glide.with(this)
                .asBitmap()
                .fitCenter()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .into(new CustomTarget<Bitmap>(90, 90) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (marker.getTag() == null) return;
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                        marker.setIcon(icon);
                        markers.put((String) marker.getTag(), marker);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        if (marker.getTag() == null) return;
                        try {
                            BitmapDescriptor markerIcon = getBitmapDescriptor(R.drawable.ic_default_user);
                            marker.setIcon(markerIcon);
                        } catch (Exception e) {
                            Log.d(TAG, "onLoadFailed: " + e.getLocalizedMessage());
                        }
                        markers.put((String) marker.getTag(), marker);
                    }
                });
    }


    // https://gist.github.com/Ozius/1ef2151908c701854736
    private BitmapDescriptor getBitmapDescriptor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VectorDrawable vectorDrawable = (VectorDrawable) ContextCompat.getDrawable(getApplicationContext(), id);

            int h = vectorDrawable.getIntrinsicHeight();
            int w = vectorDrawable.getIntrinsicWidth();

            vectorDrawable.setBounds(0, 0, w, h);

            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bm);

        } else {
            return BitmapDescriptorFactory.fromResource(id);
        }
    }
}