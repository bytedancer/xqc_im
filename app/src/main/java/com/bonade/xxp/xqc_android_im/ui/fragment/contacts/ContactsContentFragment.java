package com.bonade.xxp.xqc_android_im.ui.fragment.contacts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.callback.Packetlistener;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMSocketManager;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMOther;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;

public class ContactsContentFragment extends BaseFragment {

    public static ContactsContentFragment newInstance() {
        return new ContactsContentFragment();
    }

    private final String ACTION_SENDING_HEARTBEAT = "com.bonade.xxp.xqc_android_im.imservice.manager.ContactsContentFragment";
    private final int HEARTBEAT_INTERVAL = 2 * 1000;
    private PendingIntent pendingIntent;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        _mActivity.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        scheduleHeartbeat(HEARTBEAT_INTERVAL);
    }

    @Override
    public void onDestroy() {
        _mActivity.unregisterReceiver(imReceiver);
        super.onDestroy();
    }

    private void scheduleHeartbeat(int seconds) {
        if (pendingIntent == null) {
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(_mActivity, 0, intent, 0);
            if (pendingIntent == null) {
                return;
            }
        }

        AlarmManager am = (AlarmManager) _mActivity.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
    }

    private BroadcastReceiver imReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_SENDING_HEARTBEAT)) {
                Log.e("heartbeatReceiver", "收到广播");
            }
        }
    };
}
