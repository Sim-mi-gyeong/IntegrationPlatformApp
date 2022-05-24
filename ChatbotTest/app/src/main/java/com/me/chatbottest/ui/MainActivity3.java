package com.me.chatbottest.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.me.chatbottest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity3 extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private final String rootPath = Environment.getExternalStorageDirectory().getPath() + "/Music/";
//    private final String dirPath = "ChatBotRecord/";
    private final String dirPath = "ChatBotRecord";
    private final String rootDirPath = rootPath + dirPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        // TODO "/Music/.." 내에 "/ChatBotRecord" 폴더 생성
        File directory = new File(rootPath);
        File[] files = directory.listFiles();
        List<String> filesDirList = new ArrayList<>();

        for (int i=0; i< files.length; i++) {
            filesDirList.add(files[i].getName());
        }
        if (filesDirList.contains(dirPath)) {
            Toast.makeText(getApplicationContext(), "이미 ChatBotRecord 디렉토리가 존재합니다.", Toast.LENGTH_SHORT).show();
        } else {
            // ChatBotRecord 디렉토리 생성
            File folder = new File(rootDirPath);
            try {
                folder.mkdir();   // 디렉토리 생성
                Toast.makeText(getApplicationContext(), "ChatBotRecord 디렉토리 생성", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        WavClass wavObj = new WavClass(rootDirPath);
        startBtn = findViewById(R.id.startButton);
        stopBtn = findViewById(R.id.stopButton);
        startBtn.setOnClickListener(v -> {
            if(checkWritePermission()) {
                wavObj.startRecording();
            }
            if(!checkWritePermission()){
                requestWritePermission();
            }
        });

        // TODO 녹음 종료 및 저장 -> 서버 전송
        stopBtn.setOnClickListener(v -> wavObj.stopRecording());

    }
    private boolean checkWritePermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED ;
    }
    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.MODIFY_AUDIO_SETTINGS,WRITE_EXTERNAL_STORAGE},1);
    }
}
