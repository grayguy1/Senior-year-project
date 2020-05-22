package com.example.easypark;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class MyService extends Service {

    FirebaseFirestore db;

    private CountDownTimer countDownTimer;

    private long startTimeInMillis = 60000;
    String currLot = "";
    String currSpace = "";
    private static final int notif_id=1;

    Notification notification;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = FirebaseFirestore.getInstance();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            /*if (extras.containsKey("restart")) {
                countDownTimer.cancel();
            }*/
            currLot = extras.getString("lotName");
            currSpace = extras.getString("lotID");
            startTimeInMillis = extras.getLong("timeBooked");
            createNotificationChannel();
            Intent notificationIntent = new Intent(MyService.this, HomeActivity.class)
                    .putExtra("lotID", currSpace)
                    .putExtra("currTime", startTimeInMillis);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            int hours = (int) (startTimeInMillis / 1000) / 3600;
            int minutes = (int) ((startTimeInMillis / 1000) % 3600) / 60;
            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);


            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Parking time remaining")
                    .setContentText(timeLeftFormatted + " Space: " + currSpace)
                    .setSmallIcon(R.drawable.ic_notification_timer)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(notif_id, notification);

            startTimer();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void startTimer(){
        //Update the countdown every second
        countDownTimer = new CountDownTimer(startTimeInMillis, 1000) {
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(MyService.this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,
                    0, notificationIntent, 0);
            @Override
            public void onTick(long l) {
                startTimeInMillis = l;
                int hours = (int) (startTimeInMillis / 1000) / 3600;
                int minutes = (int) ((startTimeInMillis/1000) % 3600) / 60;
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);


                createNotificationChannel();
                Intent notificationIntent = new Intent(MyService.this, HomeActivity.class)
                        .putExtra("lotID", currSpace)
                        .putExtra("currTime", startTimeInMillis)
                        .putExtra("lotName", currLot);
                PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,
                        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setContentTitle("Parking time remaining")
                        .setContentText(String.valueOf(timeLeftFormatted) + " Space: " + currSpace)
                        .setSmallIcon(R.drawable.ic_notification_timer)
                        .setContentIntent(pendingIntent)
                        .build();
                notificationManager.notify(1,notification);

            }

            @Override
            public void onFinish() {
                try {
                    final DocumentReference docRef = db.collection("Lots").document(currLot);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            //If the document exists
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    try {
                                        //Update the database to indicate that the parking space is now free
                                        docRef.update(currSpace, true);
                                        Toast.makeText(MyService.this, "Parking time has ended!", Toast.LENGTH_SHORT).show();
                                    } catch (NullPointerException e) {

                                    }
                                } else {
                                    //Log.d(TAG, "No such document");
                                }
                            } else {
                                //Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
                catch(Exception e){

                }
                stopForeground(true);
                stopSelf();
            }
        }.start();
    }
}