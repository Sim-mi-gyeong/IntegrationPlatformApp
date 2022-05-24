package com.me.chatbottest.data;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class AudioData {

    @SerializedName("files")
    private File files;

    public AudioData(File files) {
        this.files = files;
    }
}
