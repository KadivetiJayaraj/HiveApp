package com.jayaraj.hime.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.jayaraj.hime.R;

public class OreoNotification extends ContextWrapper {

  private static final String CHANNEL_ID = "com.default.channelId";
  private static final String CHANNEL_NAME = "HIME";

  private NotificationManager notificationManager;

  public OreoNotification(Context base) {
    super(base);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createChannel();
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  private void createChannel() {

    NotificationChannel channel =
        new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
    channel.enableLights(false);
    channel.enableVibration(true);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

    getManager().createNotificationChannel(channel);
  }

  public NotificationManager getManager() {
    if (notificationManager == null) {
      notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    return notificationManager;
  }

  @TargetApi(Build.VERSION_CODES.O)
  public Notification.Builder getOreoNotification(
      String title, String body, PendingIntent pendingIntent, Uri soundUri) {
    return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
        .setContentIntent(pendingIntent)
        .setContentTitle(title)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
        .setContentText(body)
        .setVibrate(new long[] {100, 100, 100, 100})
        .setSmallIcon(R.drawable.app_icon)
        .setColor(getResources().getColor(R.color.colorPrimary))
        .setSound(soundUri)
        .setAutoCancel(true);
  }
}
