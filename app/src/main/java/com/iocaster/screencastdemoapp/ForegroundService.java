package com.iocaster.screencastdemoapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import kim.yt.ffmpegmc264.MC264ScreenRecorder;

/*
 * Android Foreground Service Java example :
 * https://androidwave.com/foreground-service-android-example/
 */
public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    private static MC264ScreenRecorder mMC264Recorder;

    private Activity activity;
    private MainActivity3.MyMC264RecorderCallback callback;
    private int width, height;
    private String dstURL;
    private boolean landscapeMode;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity3.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ScreenCastDemo Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();

        //MC264ScreenRecorder mc264recorder = (MC264ScreenRecorder) intent.getSerializableExtra ("mc264Recoder");
        //activity = (Activity) intent.getSerializableExtra ("activity");
        //callback = (MainActivity3.MyMC264RecorderCallback) intent.getSerializableExtra ("callback");

        activity = MainActivity3.mActivity;
        callback = MainActivity3.mc264Callback;

        width = intent.getIntExtra("cap_width", 1440);
        height = intent.getIntExtra("cap_height", 1080);
        dstURL = intent.getStringExtra("cap_dst");
        landscapeMode = intent.getBooleanExtra("cap_landscape_mode", true);

        //mMC264Recorder = mc264recorder;
        mMC264Recorder = new MC264ScreenRecorder();
        mMC264Recorder.registerCallback( callback );

        mMC264Recorder.init( activity );
        mMC264Recorder.setCaptureSize(width, height);
        mMC264Recorder.setDst(dstURL);
        if (landscapeMode) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //redundant? No, required when started without clicking landscape mode
            mMC264Recorder.setLandscapeMode(true);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mMC264Recorder.setLandscapeMode(false);
        }
        mMC264Recorder.startFFMpegOnly(); //mMC264Recorder.start();

        //should wait for ffmpeg started
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int reqcode = intent.getIntExtra("requestCode", 1);
        int rltcode = intent.getIntExtra("resultCode", 1);
        //Intent itent = (Intent) intent.getSerializableExtra("intent");
        Intent itent = MainActivity3.mScreenCaptureIntent;

        //mMC264Recorder.onActivityResult(reqcode, rltcode, itent); //old style
        mMC264Recorder.startProjection(rltcode, itent);             //new style

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ForegroundService.java :: onDestroy() : Enter...");
        mMC264Recorder.stop();
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
                    "ScreenCastDemo Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    public void setCaptureSize(int width, int height) {
//        this.width = width;
//        this.height = height;
//    }
//
//    public void setDst( String dstURL ) {
//        this.dstURL = dstURL;
//    }
//
//    public void setLandscapeMode( boolean value ) {
//        this.landscapeMode = value;
//    }
//
//    public void start() {
//        mMC264Recorder.start();
//    }
}