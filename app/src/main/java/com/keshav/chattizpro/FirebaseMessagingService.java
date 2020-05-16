package com.keshav.chattizpro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/*
This class is created for recieving foreground notifications from the firebase server
and also for that we have included a service file into our Manifest
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        String notificationtitle = remoteMessage.getNotification().getTitle();
//        String notificationBody = remoteMessage.getNotification().getBody();
//
//        String click_action = remoteMessage.getNotification().getClickAction();
//        String from_sender_user_id = remoteMessage.getData().get("from_sender_user_id");
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.chaticon)
//                        .setContentTitle(notificationtitle)
//                        .setContentText(notificationBody);
//
//        Intent resultIntent = new Intent(click_action);
//       resultIntent.putExtra("user_id",from_sender_user_id);
//
//        PendingIntent resultpendingIntent =
//                PendingIntent.getActivity(
//                        this,
//                        0,
//                        resultIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//
//        mBuilder.setContentIntent(resultpendingIntent);
//
//        //Set id for the notification
//        int mNotificatonId = (int) System.currentTimeMillis();
//        //Gets an instance of the Notification Manger Service
//        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        //Builds the notification and issue it
//        mNotifyMgr.notify(mNotificatonId, mBuilder.build());
//    }
//}

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_sender_user_id = remoteMessage.getData().get("from_sender_user_id");
        Intent intent = new Intent(click_action);
        intent.putExtra("user_id",from_sender_user_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        String channelId = "Default";
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);



        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());

    }
}
