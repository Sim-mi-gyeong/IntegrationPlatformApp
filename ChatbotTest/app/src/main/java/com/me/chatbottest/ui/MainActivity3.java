package com.me.chatbottest.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.me.chatbottest.R;
import com.me.chatbottest.data.AudioData;
import com.me.chatbottest.data.VideoResponse;
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
import java.util.HashMap;
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
    private Button playBtn;
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

        retrofitService = RetrofitClient.getClient().create(RetrofitService.class);

        WavClass wavObj = new WavClass(rootDirPath);
        startBtn = findViewById(R.id.startButton);
        stopBtn = findViewById(R.id.stopButton);
        submitBtn = findViewById(R.id.submitButton);
        playBtn = findViewById(R.id.playButton);
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
        stopBtn.setOnClickListener(v -> targetPath = wavObj.stopRecording());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 서버 전송
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

        File file = new File(targetPath);
        Log.v("전송할 파일", targetPath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), file);
        Log.v("전송할 파일 RequestBody ", String.valueOf(requestFile));
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("files", file.getPath(), requestFile);
        Log.v("전송할 파일 uploadFile ", String.valueOf(uploadFile));

        // enqueue()에 파라미터로 넘긴 콜백 - 통신이 성공/실패 했을 때 수행할 동작을 재정의
        retrofitService.sendAudio(uploadFile).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//            public void onResponse(Call<VideoResponse> call, Response<VideoResponse> response)
                ResponseBody result = response.body();
//                Log.d("RESULT : ", result.toString());
//                Toast.makeText(getApplicationContext(), String.valueOf(result), Toast.LENGTH_SHORT).show();

                if (result != null && response.isSuccessful()) {
                    Log.d("RESULT ", result.toString());

                    Log.d("요청", "전송 완료");

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                    Log.d("응답", "file download was a success? " + writtenToDisk);

                    long contentLength = result.contentLength();

                    Log.d("응답", "길이 : " + contentLength + " " + "타입 : " + String.valueOf(result.contentType()));
                    Log.d("응답", "source : " + result.source().toString());
                    Log.d("응답", "className : " + result.getClass().getName());   // okhttp3.ResponseBody$1


                    InputStream ins = result.byteStream();
                    int size;
                    byte[] b = new byte[1024];
                    OutputStream out = null;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
                    StringBuffer stringBuffer = new StringBuffer();
                    BufferedInputStream input = new BufferedInputStream(ins);

//                    OutputStream output = new FileOutputStream(ins)
                    // TODO Response 받은 mp4 를 videoView 로 재생 -> 메인 스레드에서 생성한 핸들러로 처리해야함
//                    Uri uri = Uri.parse(String.valueOf(result));
//                    videoView.setMediaController(mediaController);
//                    videoView.setVideoURI(uri);
//                    videoView.requestFocus();
//                    videoView.start();

                } else {
                    Log.d("요청","Post Status Code : " + response.code());
                    Log.d("요청", response.errorBody().toString());
                    Log.d("요청",call.request().body().toString());
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
//            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "movie.mp4");
            File futureStudioIconFile = new File(rootDirPath + File.separator + "movie.mp4");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
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

                // TODO 다운로드 완료 후 재생
               playVideo();

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
        String url = rootDirPath + "/movie.mp4";

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "동영상 준비 완료", Toast.LENGTH_SHORT).show();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.seekTo(0);
                videoView.start();
            }
        });

    }
}
