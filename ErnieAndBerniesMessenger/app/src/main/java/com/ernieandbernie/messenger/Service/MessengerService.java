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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Models.Request;
import com.ernieandbernie.messenger.R;
import com.ernieandbernie.messenger.Util.Constants;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MessengerService extends LifecycleService {

    private static final String SERVICE_CHANNEL = "Ernie And Bernie's Messenger";
    private static final String TAG = "Messenger Service";
    private static final int NEW_REQUEST_NOTIFICATION_ID = 42;

    private ExecutorService executorService;
    private Repository repository;
    Future<?> requestNotificationFuture;

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

            requestNotificationFuture = executorService.submit(() -> {
                createNotification(requests.get(0));
            });
        });
    }

    private void createNotification(Request request) {
        repository.getProfileUrlByUid(request.requestFromUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    createNewRequestNotificationWithUserIcon(request, (String) snapshot.getValue());
                } else {
                    createNewRequestNotificationWithPlaceholderIcon(request);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNewRequestNotificationWithPlaceholderIcon(Request request) {
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_default_user);
        Bitmap bitmap1 = drawableToBitmap(drawable);
        createNewRequestNotificationWithBitmapIcon(request, bitmap1);
    }

    private void createNewRequestNotificationWithUserIcon(Request request, String iconUrl) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(iconUrl)
                .placeholder(R.drawable.ic_default_user)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        createNewRequestNotificationWithBitmapIcon(request, resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        createNewRequestNotificationWithPlaceholderIcon(request);
                    }
                });
    }

    private void createNewRequestNotificationWithBitmapIcon(Request request, Bitmap resource) {
        Intent okIntent = createNewRequestIntent(Constants.ACTION_OK, request);
        PendingIntent okPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 1, okIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent noIntent = createNewRequestIntent(Constants.ACTION_NO, request);
        PendingIntent noPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 2, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), SERVICE_CHANNEL)
                .setContentTitle(getString(R.string.new_friend_request))
                .setSmallIcon(R.drawable.ic_launcher_icon_eb_foreground)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.new_friend_request_content, request.requestFromDisplayName)))
                .addAction(R.drawable.ic_action_ok, getString(R.string.accept), okPendingIntent)
                .addAction(R.drawable.ic_action_no, getString(R.string.decline), noPendingIntent)
                .setLargeIcon(resource)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NEW_REQUEST_NOTIFICATION_ID, notification);
    }

    private Intent createNewRequestIntent(String action, Request request) {
        Intent intent = new Intent(getApplicationContext(), MessengerReceiver.class);
        intent.setAction(action);
        intent.putExtra(Constants.REQUEST_FROM_UID, request.requestFromUid);
        intent.putExtra(Constants.DISPLAY_NAME, request.requestFromDisplayName);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, NEW_REQUEST_NOTIFICATION_ID);
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
        notificationManagerCompat.cancel(NEW_REQUEST_NOTIFICATION_ID);
        if (requestNotificationFuture != null) {
            requestNotificationFuture.cancel(true);
        }
        super.onDestroy();
    }


    // https://stackoverflow.com/questions/3035692/how-to-convert-a-drawable-to-a-bitmap
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}