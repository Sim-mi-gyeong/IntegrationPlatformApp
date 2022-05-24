package com.me.chatbottest.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

//    http://alswn9938.pythonanywhere.com/request
    private final static String BASE_URL = "http://alswn9938.pythonanywhere.com";

    public static RetrofitService getApiService() {
        return getClient().create(RetrofitService.class);
    }

    public static Retrofit getClient() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}
