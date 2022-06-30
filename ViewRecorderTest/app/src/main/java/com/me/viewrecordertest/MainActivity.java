package com.me.viewrecordertest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Context mAppContext;

    private View mRootView;

    private Button mButtonRecord;

    private Button mButtonSwitch;

    private TextView mTextView;

    private Handler mMainHandler;

    private Handler mWorkerHandler;

    private ViewRecorder mViewRecorder;

    private static int mNumber = 0;

    private boolean mRecording = false;

    private boolean mFullscreen = false;

    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            mTextView.setText(String.valueOf(mNumber++));
            mMainHandler.postDelayed(this, 500);
        }
    };

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final View.OnClickListener mRecordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mButtonRecord.setEnabled(false);
            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRecording) {
                        stopRecord();
                    } else {
                        startRecord();
                    }
                    updateRecordButtonText();
                }
            });
        }
    };

    private final View.OnClickListener mSwitchOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mButtonSwitch.setEnabled(false);
            if (mRecording) {
                mViewRecorder.setRecordedView(mFullscreen ? mTextView : mRootView);
                mFullscreen = !mFullscreen;
                mButtonSwitch.setText(mFullscreen ? R.string.center_view : R.string.full_screen);
                mButtonSwitch.setEnabled(true);
            }
        }
    };

    private final MediaRecorder.OnErrorListener mOnErrorListener = new MediaRecorder.OnErrorListener() {

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.e(TAG, "MediaRecorder error: type = " + what + ", code = " + extra);
            mViewRecorder.reset();
            mViewRecorder.release();
        }
    };

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "외부 저장소 사용을 위해 읽기/쓰기 필요", Toast.LENGTH_SHORT).show();

                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

            } else {

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();

        mRootView = findViewById(R.id.root);
        mTextView = (TextView) findViewById(R.id.text);
        mButtonRecord = (Button) findViewById(R.id.record);
        mButtonRecord.setOnClickListener(mRecordOnClickListener);
        mButtonSwitch = (Button) findViewById(R.id.switcher);
        mButtonSwitch.setOnClickListener(mSwitchOnClickListener);

        checkPermission();

        mMainHandler = new Handler();
        HandlerThread ht = new HandlerThread("bg_view_recorder");
        ht.start();
        mWorkerHandler = new Handler(ht.getLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMainHandler.removeCallbacks(mUpdateTextRunnable);
        if (mRecording) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                    updateRecordButtonText();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainHandler.post(mUpdateTextRunnable);
        updateRecordButtonText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkerHandler.getLooper().quit();
    }

    private void updateRecordButtonText() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mButtonRecord.setText(mRecording ? R.string.stop_record : R.string.start_record);
                mButtonRecord.setEnabled(true);

                mButtonSwitch.setEnabled(mRecording);
                if (mRecording) {
                    mFullscreen = false;
                    mButtonSwitch.setText(R.string.full_screen);
                }
            }
        });
    }

    private void startRecord() {
        File directory = mAppContext.getExternalCacheDir();
        if (directory != null) {
            directory.mkdirs();
            if (!directory.exists()) {
                Log.w(TAG, "startRecord failed: " + directory + " does not exist!");
                return;
            }
        }

        mViewRecorder = new ViewRecorder();
//        mViewRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // uncomment this line if audio required
        mViewRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mViewRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mViewRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mViewRecorder.setVideoFrameRate(60); // 5fps
        mViewRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mViewRecorder.setVideoSize(720, 1280);
        mViewRecorder.setVideoEncodingBitRate(2000 * 1000);
        Log.d(TAG, getCacheDir() + "/" + System.currentTimeMillis() + ".mp4");
//        mViewRecorder.setOutputFile(getCacheDir() + "/" + System.currentTimeMillis() + ".mp4");
        mViewRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/Movies" + "/" + System.currentTimeMillis() + ".mp4");
        mViewRecorder.setOnErrorListener(mOnErrorListener);

        mViewRecorder.setRecordedView(mTextView);
        try {
            mViewRecorder.prepare();
            mViewRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "startRecord failed", e);
            return;
        }

        Log.d(TAG, "startRecord successfully!");
        mRecording = true;
    }

    private void stopRecord() {
        try {
            mViewRecorder.stop();
            mViewRecorder.reset();
            mViewRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecording = false;
        Log.d(TAG, "stopRecord successfully!");
    }
}