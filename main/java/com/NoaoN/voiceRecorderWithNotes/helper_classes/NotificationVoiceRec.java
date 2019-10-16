package com.NoaoN.voiceRecorderWithNotes.helper_classes;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;

import com.NoaoN.voiceRecorderWithNotes.R;
import com.NoaoN.voiceRecorderWithNotes.activities.IActivityWithNotification;
import com.NoaoN.voiceRecorderWithNotes.activities.PlayRecWithNote;
import com.NoaoN.voiceRecorderWithNotes.activities.RecordingActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationVoiceRec extends Notification {


    //both notifications in both activities have the same ID, so the app only has 1 notification.
    private int ACTIVITY_NOTIFICATION_ID = 0;

    //notification fields
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private final String CHANNEL_ID = "0";

    private NotificationCompat.Builder builder;
    private String firstPendingIntentStr;
    private String secondPendingIntentStr;
    private String notificationMessage;
    private Context contex;
    private IntentFilter filter;
    private BroadcastReceiver receiver;

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public int getNotificationActivityId() {
        return ACTIVITY_NOTIFICATION_ID;
    }

    public int setNotificationActivityId(int activityNotificationID) {
        return ACTIVITY_NOTIFICATION_ID = activityNotificationID;
    }

    public void setFirstPendingIntentStr(String firstPendingIntentStr) {
        this.firstPendingIntentStr = firstPendingIntentStr;
    }

    public void setSecondPendingIntentStr(String secondPendingIntentStr) {
        this.secondPendingIntentStr = secondPendingIntentStr;
    }
    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public NotificationVoiceRec(Activity context){
        this.contex = context;
        if (context instanceof PlayRecWithNote) {
            setNotificationActivityId(1);
        } else {
            setNotificationActivityId(0);
        }
        Intent intent = new Intent(this.contex, PlayRecWithNote.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this.contex, 0, intent, 0);
        notificationManager = (NotificationManager) this.contex.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        createFilter();
        createReceiver();
    }

    /**
     * Create notification's receiver's filter.
     */
    private void createFilter(){
        filter = new IntentFilter();
        filter.addAction(this.contex.getString(R.string.pause));
        filter.addAction(this.contex.getString(R.string.stop));
        filter.addAction(this.contex.getString(R.string.play));
        filter.addAction(this.contex.getString(R.string.record));
    }

    /**
     * Create notification's receiver.
     */
    private void createReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(context instanceof IActivityWithNotification) {
                    if (context instanceof PlayRecWithNote) {
                        //action is of PlayRecWithNote activity.
                        ((PlayRecWithNote) context).BroadcastReceived(intent);
                    } else {
                        //action is of RecordingActivity.
                        ((RecordingActivity) context).BroadcastReceived(intent);
                    }
                }
            }
        };
        this.contex.registerReceiver(receiver, filter);
    }

    /**
     * Create notification's channel.
     */
    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(
                    Integer.toString(ACTIVITY_NOTIFICATION_ID), "playing recordings", importance);
            mChannel.setVibrationPattern(new long[]{0, 0});
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    /**
     * Creates and returns a notification to be shown to user.
     * @return a notification.
     */
    private void createBuilder() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.builder = new NotificationCompat.Builder(this.contex, CHANNEL_ID)
                    .setSmallIcon(R.drawable.recmic2)
                    .setContentTitle(this.contex.getString(R.string.app_name))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    //user can't remove notification, and won't clear via "clear all".
                    .setOngoing(true);
        }
        this.builder = new NotificationCompat.Builder(this.contex)
                .setSmallIcon(R.drawable.recmic2)
                .setContentTitle(this.contex.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID)
                //user can't remove notification, and won't clear via "clear all".
                .setOngoing(true);
    }

    /**
     * Creates and returns the "paused recording" notification, containing "stop" and "play"
     * actions (buttons).
     * @return "paused recording" notification.
     */
    public void createNotificationBuilder() {
        //firstPendingIntent - left button functionality.
        //secondPendingIntent - right button functionality.
        PendingIntent firstPendingIntent;
        PendingIntent secondPendingIntent;
        firstPendingIntent = PendingIntent.getBroadcast(this.contex, 0,
                new Intent(firstPendingIntentStr), 0);
        secondPendingIntent = PendingIntent.getBroadcast(this.contex, 0,
                new Intent(secondPendingIntentStr), 0);
         createBuilder();
         this.builder.addAction(R.drawable.stop_darker, firstPendingIntentStr,
                firstPendingIntent) // #0
                .addAction(R.drawable.recmic2, secondPendingIntentStr, secondPendingIntent)
                .setContentText(notificationMessage);  // #1;
        Notification notif = builder.build();
        notificationManager.notify(getNotificationActivityId(), notif);
    }

    /**
     * Unregister notification's receiver.
     */
    public void unregisterReceiver(){
        this.contex.unregisterReceiver(this.receiver);
    }
}
