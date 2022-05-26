package com.me.chatbottest.ui;

public class DataItem {

    // 내용
    private String content;
    // 이름
    private String name;
    // 뷰 타입(왼쪽 / 오른쪽)
    private int viewType;

    public DataItem(String content, String name, int viewType) {
        this.content = content;
        this.name = name;
        this.viewType = viewType;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public int getViewType() {
        return viewType;
    }

}
