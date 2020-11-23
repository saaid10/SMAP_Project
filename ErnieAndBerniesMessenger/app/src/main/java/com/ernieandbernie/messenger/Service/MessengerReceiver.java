package com.ernieandbernie.messenger.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.ernieandbernie.messenger.Models.Repository;
import com.ernieandbernie.messenger.Util.Constants;


public class MessengerReceiver extends BroadcastReceiver {

    private Repository repository;

    @Override
    public void onReceive(Context context, Intent intent) {
        repository = Repository.getInstance(context.getApplicationContext());
        String action = intent.getAction();
        if (action.equals(Constants.ACTION_OK)) {
            acceptRequest(intent.getStringExtra(Constants.REQUEST_FROM_UID), intent.getStringExtra(Constants.DISPLAY_NAME));
        } else if (action.equals(Constants.ACTION_NO)) {
            declineRequest(intent.getStringExtra(Constants.REQUEST_FROM_UID));
        }
        dismissNotification(context, intent);
    }

    private void dismissNotification(Context context, Intent intent) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
        notificationManagerCompat.cancel(intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, -1));
    }

    private void acceptRequest(String requestFromUid, String displayName) {
        repository.addNewFriend(requestFromUid, displayName);
    }

    private void declineRequest(String requestFromUid) {
        repository.deleteFriendRequest(requestFromUid);
    }
}