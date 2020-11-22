package com.ernieandbernie.messenger.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.ernieandbernie.messenger.Util.Constants;


public class MessengerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("!2", "onReceive: " + intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, -1));
        Log.d("!2", "onReceive: " + intent.getStringExtra(Constants.REQUEST_FROM_UID));
        if (action.equals(Constants.ACTION_OK)) {
            Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
        } else if (action.equals(Constants.ACTION_NO)) {
            Toast.makeText(context, "no", Toast.LENGTH_SHORT).show();
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());
        notificationManagerCompat.cancel(intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, -1));
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}