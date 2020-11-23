package com.ernieandbernie.messenger.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.Request;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.Util.Constants;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class MessengerService extends LifecycleService {

    public static final String SERVICE_CHANNEL = "Assignment 2 Service Channel";
    private static final String TAG = "CountryService";
    private static final int NOTIFICATION_ID = 42;

    private ExecutorService executorService;

    private Repository repository;

    public MessengerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(SERVICE_CHANNEL, TAG, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        repository = Repository.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        loadWork();
        return START_STICKY;
    }

    private void loadWork() {
        repository.getFriendRequests().observe(this, requests -> {
            if (requests.isEmpty()) return;

            createNotification(requests.get(0));
        });
    }

    private void createNotification(Request request) {
        repository.getProfileUrlByUid(request.requestFromUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    createNotificationWithUserIcon(request, (String) snapshot.getValue());
                } else {
                    createNotificationWithPlaceholderIcon(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNotificationWithPlaceholderIcon(Request request) {
        Resources resources = getApplicationContext().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round);
        createNotificationWithBitmapIcon(request, bitmap);
    }

    private void createNotificationWithUserIcon(Request request, String iconUrl) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(iconUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        createNotificationWithBitmapIcon(request, resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        createNotificationWithPlaceholderIcon(request);
                    }
                });
    }

    private void createNotificationWithBitmapIcon(Request request, Bitmap resource) {
        Intent okIntent = createIntent(Constants.ACTION_OK, request);
        PendingIntent okPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 1, okIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent noIntent = createIntent(Constants.ACTION_NO, request);
        PendingIntent noPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), SERVICE_CHANNEL)
                .setContentTitle(getString(R.string.new_friend_request))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.new_friend_request_content, request.requestFromDisplayName)))
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.accept), okPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.decline), noPendingIntent)
                .setLargeIcon(resource)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    private Intent createIntent(String action, Request request) {
        Intent intent = new Intent(getApplicationContext(), MessengerReceiver.class);
        intent.setAction(action);
        intent.putExtra(Constants.REQUEST_FROM_UID, request.requestFromUid);
        intent.putExtra(Constants.DISPLAY_NAME, request.requestFromDisplayName);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
        return intent;
    }

    @Override
    public IBinder onBind(@NotNull Intent intent) {
        // TODO: Return the communication channel to the service.
        super.onBind(intent);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }
}