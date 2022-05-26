package com.me.chatbottest.network;

import com.google.gson.JsonObject;
import com.me.chatbottest.data.AudioData;
import com.me.chatbottest.data.VideoResponse;

import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitService {

    @Multipart
    @POST("/main")
    Call<ResponseBody> sendAudio(@Part MultipartBody.Part files);
//    Call<VideoResponse> sendAudio(@Body AudioData recorderData);
}
