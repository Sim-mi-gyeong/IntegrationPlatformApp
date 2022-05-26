package com.me.chatbottest.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

//    http://alswn9938.pythonanywhere.com/request
//    private final static String BASE_URL = "http://alswn9938.pythonanywhere.com";
    private final static String BASE_URL = "http://175.123.137.197:5000";

    public static RetrofitService getApiService() {
        return getClient().create(RetrofitService.class);
    }

    public static Retrofit getClient() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}
