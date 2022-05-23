package com.me.backgrounddetectiontest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

// 방송을 하는 발신자 역할
public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver;   // BroadcastReceiver 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReceiver = new TestReceiver();   // TestReceiver 클래스를 Broadcast Receiver 로 정의
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * IntentFilter : 암시적 인텐트 -> 사용자에게 어느 앱을 사용할지 선택하도록 할 때 사용
         * Ex) 사용자가 다른 사람들과 함께 공유하고자 하는 콘텐츠의 경우
         *  -> ACTION_SEND 라는 Action 이 있는 인텐트 생성 후 -> 공유할 콘텐츠를 지정하는 Extra 추가
         */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        // Local 로 등록
        filter.addAction(TestReceiver.TestAction);
        registerReceiver(mReceiver, filter);
    }

    // 리시버 해제
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    // 브로드캐스트 리시버에 액션 전송 - 버튼 클릭
    public void sendTestBroadCast(View view) {
        // Action 을 담아 리시버로 인텐트 발송
        Intent intent = new Intent(TestReceiver.TestAction);
        sendBroadcast(intent);
    }
}