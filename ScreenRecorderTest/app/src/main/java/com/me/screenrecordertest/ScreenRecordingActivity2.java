package com.me.screenrecordertest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenRecordingActivity2 extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1001;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private MediaRecorder mediaRecorder;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallback mediaProjectionCallback;

    private int mScreenDensity;
    private static final int DISPLAY_WIDTH = 720;
    private static final int DISPLAY_HEIGHT = 1280;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private LinearLayout rootLayout;
    private ToggleButton toggleButton;
    private VideoView videoView;
    private String videoUri = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // View
        videoView = findViewById(R.id.videoView);
//        toggleButton = findViewById(R.id.toggleBtn);
        rootLayout = findViewById(R.id.rootLayout);

        // Event
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ScreenRecordingActivity2.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 + ContextCompat.checkSelfPermission(ScreenRecordingActivity2.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ScreenRecordingActivity2.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(ScreenRecordingActivity2.this, Manifest.permission.RECORD_AUDIO))  {

                        toggleButton.setChecked(false);
//                        Toast.makeText(MainActivity.this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                        Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
                                .setAction("ENABLE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(ScreenRecordingActivity2.this,
                                                new String[]{
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.RECORD_AUDIO
                                                }, REQUEST_PERMISSION);
                                    }
                                }).show();
                    }
                    else {
                        ActivityCompat.requestPermissions(ScreenRecordingActivity2.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO
                                }, REQUEST_PERMISSION);
                    }
                }
                else {
                    toggleScreenShare(v);
                }
            }
        });
    }

    private void toggleScreenShare(View v) {
        if (toggleButton.isChecked()) {
            initRecorder();
            recordScreen();
        } else {
            mediaRecorder.stop();
            mediaRecorder.reset();
            stopRecordScreen();

            // Play in Video View
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUri));
            videoView.start();
        }
    }

    private void initRecorder() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            videoUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + new StringBuilder("/EDMTRecord_").append(new SimpleDateFormat("dd-MM-yyyy-hh_mm_ss")
            .format(new Date())).append(".mp4").toString();

            mediaRecorder.setOutputFile(videoUri);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512*1000);
            mediaRecorder.setVideoFrameRate(30);
//            mediaRecorder.setVideoFrameRate(50);



            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordScreen() {
        if (mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE) {
            Toast.makeText(this, "Unk Error", Toast.LENGTH_SHORT).show();
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            toggleButton.setChecked(false);
            return;
        }

        mediaProjectionCallback = new MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();

    }

    private void stopRecordScreen() {
        if (virtualDisplay == null) {
            return;
        }
        virtualDisplay.release();
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    toggleScreenShare(toggleButton);
                }
                else {
                    toggleButton.setChecked(false);
                    Snackbar.make(rootLayout, "Permissions", Snackbar.LENGTH_INDEFINITE)
                            .setAction("ENABLE", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(ScreenRecordingActivity2.this,
                                            new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.RECORD_AUDIO
                                            }, REQUEST_PERMISSION);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {

        @Override
        public void onStop() {
            if (toggleButton.isChecked()) {
                toggleButton.setChecked(false);
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            mediaProjection = null;
            stopRecordScreen();
            super.onStop();
        }

    }
}