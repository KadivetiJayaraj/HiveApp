package com.jayaraj.hime.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jayaraj.hime.R;
import com.jayaraj.hime.ui.SplashScreen;

public class FirebaseMessagingServices extends FirebaseMessagingService {

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);

    showNotification(remoteMessage);
  }

  private void showNotification(RemoteMessage remoteMessage) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      sendOreoNotification(remoteMessage);
    } else {
      sendNotification(remoteMessage);
    }
  }

  private void sendOreoNotification(RemoteMessage remoteMessage) {
    String title = remoteMessage.getData().get("title");
    String body = remoteMessage.getData().get("body");
    String profilepicture = remoteMessage.getData().get("profilePicture");
    String himeId = remoteMessage.getData().get("himeId");
    Intent intent;
    intent = new Intent(this, SplashScreen.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 1023, null, PendingIntent.FLAG_ONE_SHOT);
    Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    OreoNotification oreoNotification = new OreoNotification(this);
    Notification.Builder builder =
        oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound);

    int i = 0;
    oreoNotification.getManager().notify(i, builder.build());
  }

  private void sendNotification(RemoteMessage remoteMessage) {
    String title = remoteMessage.getData().get("title");
    String body = remoteMessage.getData().get("body");
    String profilepicture = remoteMessage.getData().get("profilePicture");
    String himeId = remoteMessage.getData().get("himeId");
    Intent intent;
    intent = new Intent(this, SplashScreen.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 1023, null, PendingIntent.FLAG_ONE_SHOT);
    NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this)
            .setContentTitle(title)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setVibrate(new long[] {100, 100, 100, 100})
            .setSound(defaultSound)
            .setContentIntent(pendingIntent);

    int i = 0;
    assert noti != null;
    noti.notify(i, builder.build());
  }
}
