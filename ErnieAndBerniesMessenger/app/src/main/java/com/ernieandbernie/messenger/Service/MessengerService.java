package com.ernieandbernie.messenger.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.Request;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.Util.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class MessengerService extends LifecycleService {

    public static final String SERVICE_CHANNEL = "Assignment 2 Service Channel";
    private static final String TAG = "CountryService";
    private static final int NOTIFICATION_ID = 42;
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

        repository.getFriendRequests().observe(this, new Observer<List<Request>>() {
            @Override
            public void onChanged(List<Request> requests) {
                if (requests.isEmpty()) return;

                repository.getProfileUrlByUid(requests.get(0).requestFromUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load((String)snapshot.getValue())
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        Intent okIntent = new Intent(getApplicationContext(), MessengerReceiver.class);
                                        okIntent.setAction(Constants.ACTION_OK);
                                        okIntent.putExtra(Constants.REQUEST_FROM_UID, requests.get(0).requestFromUid);
                                        okIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
                                        PendingIntent okPendingIntent =
                                                PendingIntent.getBroadcast(getApplicationContext(), 1, okIntent, 0);

                                        Intent noIntent = new Intent(getApplicationContext(), MessengerReceiver.class);
                                        noIntent.setAction(Constants.ACTION_NO);
                                        noIntent.putExtra(Constants.REQUEST_FROM_UID, requests.get(0).requestFromUid);
                                        noIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
                                        PendingIntent noPendingIntent =
                                                PendingIntent.getBroadcast(getApplicationContext(), 2, noIntent, 0);

                                        Notification notification = new NotificationCompat.Builder(getApplicationContext(), SERVICE_CHANNEL)
                                                .setContentTitle(getString(R.string.new_friend_request))
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.new_friend_request_content, requests.get(0).requestFromDisplayName)))
                                                .addAction(R.drawable.ic_launcher_foreground, "Accept", okPendingIntent)
                                                .addAction(R.drawable.ic_launcher_foreground, "Decline", noPendingIntent)
                                                .setLargeIcon(resource)
                                                .setAutoCancel(true)
                                                .build();

                                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                                        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        super.onBind(intent);
        throw new UnsupportedOperationException("Not yet implemented");
    }
}