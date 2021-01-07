/*
 * This source code is based on these pages :
 *  https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/media/projection/MediaProjectionDemo.java
 *  https://android.googlesource.com/platform/development/+/master/samples/ApiDemos
 */
package com.iocaster.screencastdemoapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
//import android.preference.PreferenceManager;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.iocaster.screencastdemoapp.camerapreview.CameraActivity;

import java.util.ArrayList;
import java.util.List;

import kim.yt.ffmpegmc264.MC264ScreenRecorder;

//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {
    private static final String TAG = "ScreenCastDemoApp";
    private static final int PERMISSION_REQ_CODE = 100;

    private static final List<Resolution> RESOLUTIONS = new ArrayList<Resolution>() {{
        //4:3
        //add(new Resolution(1920,1440));       //too big size to encoder : instead use 1440 x 1080
        add(new Resolution(1440,1080));
        add(new Resolution(1366,1024));
        add(new Resolution(1600,1200));
        add(new Resolution(960,720));
        add(new Resolution(720,540));
        add(new Resolution(320,240));
        //16:9
        add(new Resolution(1920,1080));
        add(new Resolution(1366,768));
        add(new Resolution(1600,900));
        add(new Resolution(960,540));
        add(new Resolution(720,405));
    }};

    private int mDisplayWidth;
    private int mDisplayHeight;

    private Button btnStart, btnStop, btnHide;
    private CheckBox chkCaptureFromCamera;
    private CheckBox chkVideoMode;  //landscape mode
    private CheckBox chkMIC;
    private EditText capDst;

    //private static MC264ScreenRecorder mMC264Recorder;

    public static MediaProjectionManager pm;
    public static Intent mScreenCaptureIntent;
    public static Activity mActivity;
    public static MyMC264RecorderCallback mc264Callback;

    private ContextWrapper mCtx;

    public class MyMC264RecorderCallback extends MC264ScreenRecorder.Callback {
        @Override
        public void onStart() {
            Log.d(TAG, "MC264ScreenRecorder.Callback::onStart() ... " );

            //hideMe(); //move to bottom

            Toast.makeText(mCtx, "stream started ...", Toast.LENGTH_LONG).show();

            /*
             * launch camera preview app if user selects the camera streaming
             */
            if( chkCaptureFromCamera.isChecked() ) {
                Intent intent = new Intent(mCtx, CameraActivity.class);
                if (chkVideoMode.isChecked()) {
                    intent.putExtra("req_landscape", 1);
                } else {
                    intent.putExtra("req_landscape", 0);
                }
                startActivity(intent);
            } else {
                hideMe();
            }
        }

        @Override
        public void onStop( int retcode ) {
            Log.d(TAG, "MC264ScreenRecorder.Callback::onStop() ... retcode = " + retcode );

            if( retcode == 0 ) {
                Toast.makeText(mCtx,
                        "Normal finished : retcode = " + retcode, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mCtx,
                        "Error finished : retcode = " + retcode, Toast.LENGTH_LONG).show();
            }
            btnStart.setEnabled(true);
            chkCaptureFromCamera.setEnabled(true);
            chkVideoMode.setEnabled(true);
//            //chkMIC.setEnabled(true);
//            ScreenRecorderNotification.cancel(mCtx);
//            //showMe();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "---> onCreate() ..." );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_projection);

        mActivity = this;
        mCtx = this;
        mc264Callback = new MyMC264RecorderCallback();

        /*
         * request an android permissions : WRITE_EXTERNAL_STORAGE / RECORD_AUDIO
         * It is required when to save the ffmpeg output into a file and to record MIC input.
         */
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                1337);

        //getDeviceScreenSize();
        ArrayAdapter<Resolution> arrayAdapter = new ArrayAdapter<Resolution>(
                this, android.R.layout.simple_list_item_1, RESOLUTIONS);
        Spinner s = (Spinner) findViewById(R.id.spinner);
        s.setAdapter(arrayAdapter);
        s.setOnItemSelectedListener(new ResolutionSelector());
        s.setSelection(0);

        btnStart = findViewById(R.id.button_start);
        btnStop = findViewById(R.id.button_stop);
        btnHide = findViewById(R.id.button_hide);
        chkCaptureFromCamera = findViewById(R.id.checkBox_CaptureCamera);
        chkVideoMode = findViewById(R.id.checkBox_VideoMode);
//        chkMIC = findViewById(R.id.checkBox_mic);
        capDst = findViewById(R.id.editText_capDst);

        String defaultDst = getResources().getString(R.string.cap_dst_ussage2);
        String savedDst = PreferenceManager.getDefaultSharedPreferences(this).getString("dst", defaultDst);
        capDst.setText( savedDst );

//moved into ForegroundService.java :: onStartCommand()
//        mMC264Recorder = new MC264ScreenRecorder();
//        mMC264Recorder.registerCallback( new MyMC264RecorderCallback() );
//        mMC264Recorder.init( this );
    }

//    @Override
//    protected void onStop() {
//        Log.d(TAG, "---> onStop() ..." );
//        super.onStop();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "---> onDestroy() ..." );
//        mMC264Recorder.release();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finish or Not");
        builder.setMessage("Do you want to finish? ");

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //MainActivity2.super.onBackPressed();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainActivity3.super.onBackPressed();
            }
        });

        builder.show();
    }

    private void getDeviceScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        RESOLUTIONS.add(new Resolution(size.x,size.y));
        RESOLUTIONS.add(new Resolution(size.y,size.x));

        Log.i( TAG, "---> getDeviceScreenSize() : width x height = " + size.x + " x " + size.y );
    }

    private void saveDst( String dstStr ) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("dst", dstStr ).apply();
    }

    private boolean first_run = true;

    /*
     * java.lang.SecurityException: Media projections require a foreground service of type ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
     * refer to : https://medium.com/@juliozynger/media-projection-and-audio-capture-1ca72e271e9c
     *              --> https://github.com/julioz/AudioCaptureSample/blob/master/app/src/main/java/com/zynger/audiocapturesample/MainActivity.kt
     * refer to : https://thdev.tech/androiddev/2020/05/01/Android-MediaProjection-New/
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "---> onActivityResult() ...");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PERMISSION_REQ_CODE) {
            Log.e(TAG, "onActivityResult() : Unknown request code = " + requestCode);
            Toast.makeText(mCtx,
                    "onActivityResult() : Unknown request code =" + requestCode, Toast.LENGTH_SHORT).show();

            btnStart.setEnabled(true);
            chkCaptureFromCamera.setEnabled(true);
            chkVideoMode.setEnabled(true);
            return;
        }
        else if (resultCode != RESULT_OK) {
            Log.e(TAG, "onActivityResult() : User denied screen sharing permission");
            Toast.makeText(mCtx,
                    "onActivityResult() : User denied screen sharing permission", Toast.LENGTH_SHORT).show();

            btnStart.setEnabled(true);
            chkCaptureFromCamera.setEnabled(true);
            chkVideoMode.setEnabled(true);
            return;
        }

        mScreenCaptureIntent = data;    //save intent for ForegroundService

        if(false) {
            //mMC264Recorder.onActivityResult(requestCode, resultCode, data);   //old style : moved into ForegroundService.java :: onStartCommand()
        } else {
            /*
             * run MediaProjection within a ForegroundService to avoid a SecurityException :
             * java.lang.SecurityException: Media projections require a foreground service of type ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
             *
             */
            startMyService(requestCode, resultCode, data);                        //new style
        }
    }

    private class ResolutionSelector implements Spinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
            Resolution r = (Resolution) parent.getItemAtPosition(pos);
            mDisplayHeight = r.y;
            mDisplayWidth = r.x;
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) { /* Ignore */ }
    }

    private static class Resolution {
        int x;
        int y;
        public Resolution(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            return x + "x" + y;
        }
    }

    public void onBtnStart(View view) {
        String capDstStr = new String("") + capDst.getText();
        saveDst(capDstStr);

        if( false ) {
//moved into ForegroundService.java :: onStartCommand()
//            mMC264Recorder.setCaptureSize(mDisplayWidth, mDisplayHeight);
//            mMC264Recorder.setDst(capDstStr);
//            if (chkVideoMode.isChecked()) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //redundant? No, required when started without clicking landscape mode
//                mMC264Recorder.setLandscapeMode(true);
//            } else {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                mMC264Recorder.setLandscapeMode(false);
//            }
//            mMC264Recorder.start();
        } else {
            pm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

            startActivityForResult(pm.createScreenCaptureIntent(), PERMISSION_REQ_CODE);

            Toast.makeText(mCtx, "wait 15 seconds ...", Toast.LENGTH_LONG).show();
        }

        String capModeStr;
        if( chkVideoMode.isChecked() )
            capModeStr = new String("Capture as Landscape : " + mDisplayWidth + "x" + mDisplayHeight);
        else
            capModeStr = new String("Capture as Portrait : " + mDisplayHeight + "x" + mDisplayWidth);

        btnStart.setEnabled(false);
        chkCaptureFromCamera.setEnabled(false);
        chkVideoMode.setEnabled(false);
//        //chkMIC.setEnabled(false);
//        ScreenRecorderNotification.notify(this, capModeStr, 4747);
//        //hideMe();     //moved into MC264ScreenRecorder.Callback::onStart();
    }

    public void onBtnStop(View view) {
//        mMC264Recorder.stop();
        stopMyService();
        first_run = true;
    }

    public void onCheckLandscape(View view) {
        if (chkVideoMode.isChecked()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            Toast.makeText(this,
                    "Turn your phone to LANDSCAPE before click START !!!", Toast.LENGTH_LONG).show();
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            Toast.makeText(this,
                    "Turn your phone to PORTRAIT before click START !!!", Toast.LENGTH_LONG).show();
        }
    }

    public void onCheckMIC(View view) {
//        if (chkMIC.isChecked())
//            mMC264Recorder.includeMICCapture( true );
//        else
//            mMC264Recorder.includeMICCapture( false );

    }

    private void hideMe() {
        //send this app to background
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(i);
    }
    private void showMe() {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }
    public void onBtnHide(View view) {
        hideMe();
    }


    //--- Foreground Service Start / Stop ---
    public void startMyService(int requestCode, int resultCode, Intent data) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "ScreenCastDemoApp Service Started");

        serviceIntent.putExtra("requestCode", requestCode);
        serviceIntent.putExtra("resultCode", resultCode);
        serviceIntent.putExtra("intent", data);

        //serviceIntent.putExtra("mc264Recoder", (Serializable) mMC264Recorder);
//        serviceIntent.putExtra("activity", (Serializable) this);
//        serviceIntent.putExtra("callback", (Serializable) new MyMC264RecorderCallback());
        String defaultDst = getResources().getString(R.string.cap_dst_ussage2);
        String savedDst = PreferenceManager.getDefaultSharedPreferences(this).getString("dst", defaultDst);
        serviceIntent.putExtra("cap_width", mDisplayWidth);
        serviceIntent.putExtra("cap_height", mDisplayHeight);
        serviceIntent.putExtra("cap_dst", savedDst);
        if (chkVideoMode.isChecked()) {
            serviceIntent.putExtra("cap_landscape_mode", true);
        } else {
            serviceIntent.putExtra("cap_landscape_mode", false);
        }

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopMyService() {
        Log.d(TAG, "MainActivity3.java :: stopMyService() : Enter...");
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }
}
