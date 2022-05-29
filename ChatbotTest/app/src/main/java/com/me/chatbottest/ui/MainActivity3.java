package com.me.chatbottest.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.me.chatbottest.R;
import com.me.chatbottest.network.RetrofitClient;
import com.me.chatbottest.network.RetrofitService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity3 extends AppCompatActivity {

    private RetrofitService retrofitService;
    private String targetPath;
    private VideoView videoView;
    private MediaController mediaController;

    private Button startBtn;
    private Button stopBtn;
    private Button submitBtn;
    private EditText editText;

    private final String rootPath = Environment.getExternalStorageDirectory() + "/Download/";
//    private final String rootPath = Environment.getExternalStorageDirectory() + "/Movies/";
    private final String dirPath = "ChatBotRecord2";
    private final String rootDirPath = rootPath + dirPath;
//    private final String rootDirPath = rootPath;
    private String videoName;   // 서버에서 전송받은 영상 저장 이름
    private String saveVideoPath;   // 서버에서 전송받은 영상 저장 경로
    private String url;   // 서버에서 전송받은 영상 저장 경로

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

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
                folder.mkdir();
                Toast.makeText(getApplicationContext(), "ChatBotRecord 디렉토리 생성", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        // TODO Retrofit 객체 생성
        retrofitService = RetrofitClient.getClient().create(RetrofitService.class);

        WavClass wavObj = new WavClass(rootDirPath);
        startBtn = findViewById(R.id.startButton);
        stopBtn = findViewById(R.id.stopButton);
        submitBtn = findViewById(R.id.submitButton);
        editText = findViewById(R.id.editText);

        videoView = findViewById(R.id.videoView);
        mediaController = new MediaController(this);

        startBtn.setOnClickListener(v -> {
            if(checkWritePermission()) {

                wavObj.startRecording();
            }
            if(!checkWritePermission()){
                requestWritePermission();
            }
        });

        // TODO 녹음 종료 및 저장 -> 저장된 음성 파일 가져와서 -> 서버 전송
        stopBtn.setOnClickListener(v -> {
                    targetPath = wavObj.stopRecording();
                    Toast.makeText(getApplicationContext(), targetPath, Toast.LENGTH_SHORT).show();
                    // TODO 서버 전송
                    sendAudio();
        });

        // TODO EditText 로 파일명 입력받아 -> 서버 전송
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileText = String.valueOf(editText.getText());
                targetPath = rootDirPath + "/" + fileText + ".wav";
                sendAudio();
            }
        });

    }

    private boolean checkWritePermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED ;
    }

    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.MODIFY_AUDIO_SETTINGS,WRITE_EXTERNAL_STORAGE},1);
    }

    private void sendAudio() {

//        String target = rootDirPath + "/final_record.wav";
//        Log.d("target 경로", target);
//        File file = new File(target);
        Log.d("targetPath 경로", targetPath);
        File file = new File(targetPath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), file);
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files", file.getPath(), requestFile);

        // TODO enqueue()에 파라미터로 넘긴 콜백 - 통신이 성공/실패 했을 때 수행할 동작을 재정의
        retrofitService.sendAudio(uploadFile).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ResponseBody result = response.body();
//                Toast.makeText(getApplicationContext(), String.valueOf(result), Toast.LENGTH_SHORT).show();

                if (result != null && response.isSuccessful()) {
                    Log.d("요청", "전송 완료");

                    // TODO 서버에서 응답으로 받은 영상 저장(다운로드)
                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                    if (writtenToDisk) {
                        // TODO writeResponseBodyToDisk() 가 return True 면 -> 다운로드 완료 후 영상 재생
                        playVideo();
                    }

                    Log.d("응답", "file download was a success? " + writtenToDisk);


                    // TODO Response 받은 mp4 를 videoView 로 재생 -> 메인 스레드에서 생성한 핸들러로 처리해야함
//                    Uri uri = Uri.parse(String.valueOf(result));
//                    videoView.setMediaController(mediaController);
//                    videoView.setVideoURI(uri);
//                    videoView.requestFocus();
//                    videoView.start();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("요청", t.getMessage());
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            // TODO 저장될 경우의 영상 이름 AutoIncrement 로 설정
            videoName = System.currentTimeMillis() + ".mp4";
//            videoName = "movie.mp4";
//            saveVideoPath = rootDirPath + File.separator + videoName;
            saveVideoPath = Environment.getExternalStorageDirectory() + "/Movies/" + File.separator + videoName;
            Log.d("saveVideoPath", saveVideoPath);
            File futureStudioIconFile = new File(saveVideoPath);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                Log.d("inputStream", String.valueOf(inputStream));
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d("응답", "file download: " + fileSizeDownloaded + " of " + fileSize);

                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void playVideo() {

        url = saveVideoPath;

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "동영상 준비 완료", Toast.LENGTH_SHORT).show();

                // TODO 영상 준비 완료 후 저장
                videoView.seekTo(0);
                videoView.start();
            }
        });

    }
}
