package org.tint.utils;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Random;

/**
 * Created by Abhijit on 2016-05-28.
 */
public class NotificationUtils {
    public static void showDownloadCompleteNotification(Context context, String notificationTitle, String title, String message) {
        Intent notificationIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setTicker(notificationTitle)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(new Random().nextInt(), notification);
    }
}
