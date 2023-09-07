package com.example.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Timer;
import java.util.TimerTask;

public class CounterService extends Service {

    Timer timer;
    ToneGenerator toneGenerator;

    private boolean running = false;
    private int count = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!running) {
            running = true;
            createNotificationChannel();
            startForeground(1, buildNotification(count));

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateNotification(++count);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
            }, 0, 1000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
        toneGenerator.release();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "General", "General", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(int count) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "General");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle("Counter is running!");
        builder.setContentText(Integer.toString(count));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    private void updateNotification(int count) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (notificationManager.areNotificationsEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(1, buildNotification(count));
        }
    }
}