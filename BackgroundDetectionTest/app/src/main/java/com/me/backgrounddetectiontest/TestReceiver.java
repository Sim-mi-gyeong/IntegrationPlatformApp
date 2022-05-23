package com.me.backgrounddetectiontest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// 방송을 받는 수신자 역할
// 해당하는 브로드캐스트 이벤트가 발생하면 onReceive 에 정의한 이벤트가 작동
public class TestReceiver extends BroadcastReceiver {

    // TODO TestAction = 액션 명 => '패키지명.action.액션명(자유롭게)' 형태 지정
    public final static String TestAction = "com.me.backgrounddetectiontest.ACTION_TEST_BROADCAST";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 각 방송 정보가 intent 로 전달
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "전원 연결 상태", Toast.LENGTH_SHORT).show();
        } else if(Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "전원 해제 상태", Toast.LENGTH_SHORT).show();
        } else if (TestAction.equals(intent.getAction())) {
            Toast.makeText(context, "방송", Toast.LENGTH_SHORT).show();

        }
    }
}
