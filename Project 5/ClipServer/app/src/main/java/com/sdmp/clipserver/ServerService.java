package com.sdmp.clipserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.util.HashMap;
import java.util.Map;
import androidx.core.app.NotificationCompat;

import sdmp.project5.services.musicplayer.*;

public class ServerService extends Service {

    private MediaPlayer mediaPlayer;
    int length;
    int previoustrack;
    int [] songlist={R.raw.track1,R.raw.track2,R.raw.track3,R.raw.track4,R.raw.track5};
    Map<Integer,Integer> songtrack=new HashMap<Integer,Integer>();
    private static final int NOTIFICATION_ID = 1;
    private Notification notification ;
    private static String CHANNEL_ID = "Music" ;
    private static final String TOAST_INTENT_FILTER =
            "edu.uic.cs478.s19.kaboom.showToast";
    @Override
    public void onCreate() {
        super.onCreate();

        this.createNotificationChannel();
        Intent launchIntent=new Intent();
        launchIntent.setComponent(new ComponentName("com.sdmp.audioclient", "com.sdmp.audioclient.MainActivity"));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT) ;

        notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setOngoing(true).setContentTitle("Music Playing")
                        .setFullScreenIntent(pendingIntent, false)
                        .build();



        startForeground(NOTIFICATION_ID, notification);

    }



    private void createNotificationChannel() {
            CharSequence name = "Music player notification";
            String description = "The channel is for music player notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

    }


    private final MusicPlayer.Stub mBinder = new MusicPlayer.Stub() {

        public void play(int position) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), songlist[position]);
            mediaPlayer.start();
            previoustrack=position;

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Intent intent=new Intent(TOAST_INTENT_FILTER);
                    intent.putExtra("STOPPED",1);
                    sendBroadcast(intent);


                }
            });
        }



        public void pause(int position) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                length = mediaPlayer.getCurrentPosition();
                if(!songtrack.containsKey(position)) {
                    songtrack.put(position, length);
                }
                else{
                    songtrack.replace(position,songtrack.get(position),length);
                }

            }
        }

        public void resume(int position) {
            if(!mediaPlayer.isPlaying()) {
                if(songtrack.containsKey(position)) {
                    mediaPlayer.seekTo(songtrack.get(position));
                    mediaPlayer.start();
                }
            }
            else {
                mediaPlayer.pause();
                if (songtrack.containsKey(position)) {
                    mediaPlayer.seekTo(songtrack.get(position));
                    mediaPlayer.start();
                }
            }

        }

        public void release(){
            stopForeground(true);
            stopSelf();
        }

        public void stop() {
            if(mediaPlayer.isPlaying()) {
                length = mediaPlayer.getCurrentPosition();
                if(!songtrack.containsKey(previoustrack)) {
                    songtrack.put(previoustrack, length);
                }
                else{
                    songtrack.replace(previoustrack,songtrack.get(previoustrack),length);
                }
                mediaPlayer.stop();
                mediaPlayer.release();

            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
