package com.bonade.xxp.xqc_android_im.imservice.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.bonade.xxp.xqc_android_im.imservice.callback.Packetlistener;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMOther;
import com.bonade.xxp.xqc_android_im.util.Logger;

/**
 * 采用AlarmManager 进行心跳的检测
 *
 * 登陆之后就开始触发心跳检测 【仅仅是在线，重连就会取消的】
 * 退出reset 会释放alarmManager 资源
 */
public class IMHeartBeatManager extends IMManager {
    // 心跳检测4分钟检测一次，并且发送心跳包

    private Logger logger = Logger.getLogger(IMHeartBeatManager.class);

    private static IMHeartBeatManager instance = new IMHeartBeatManager();
    public static IMHeartBeatManager getInstance() {
        return instance;
    }
    private IMHeartBeatManager(){}

    private final int HEARTBEAT_INTERVAL = 4 * 60 * 1000;
    private final String ACTION_SENDING_HEARTBEAT = "com.bonade.xxp.xqc_android_im.imservice.manager.imheartbeatmanager";
    private PendingIntent pendingIntent;

    @Override
    public void doOnStart() {

    }

    /**
     * 登录成功之后
     */
    public void onloginNetSuccess() {
        logger.e("heartbeat#onLocalNetOk");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        logger.d("heartbeat#register actions");
        context.registerReceiver(imReceiver, intentFilter);
        //获取AlarmManager系统服务
        scheduleHeartbeat(HEARTBEAT_INTERVAL);
    }

    @Override
    public void reset() {
        logger.d("heartbeat#reset begin");
        try {
            context.unregisterReceiver(imReceiver);
            cancelHeartbeatTimer();
            logger.d("heartbeat#reset stop");
        } catch (Exception e) {
            logger.e("heartbeat#reset error:%s",e.getCause());
        }
    }

    /**
     * MsgServerHandler 直接调用
     */
    public void onMsgServerDisconn() {
        logger.w("heartbeat#onChannelDisconn");
        cancelHeartbeatTimer();
    }

    public void cancelHeartbeatTimer() {
        logger.w("heartbeat#cancelHeartbeatTimer");
        if (pendingIntent == null) {
            logger.w("heartbeat#pi is null");
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void scheduleHeartbeat(int seconds) {
        logger.d("heartbeat#scheduleHeartbeat every %d seconds", seconds);
        if (pendingIntent == null) {
            logger.w("heartbeat#fill in pendingintent");
            Intent intent = new Intent(ACTION_SENDING_HEARTBEAT);
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            if (pendingIntent == null) {
                logger.w("heartbeat#scheduleHeartbeat#pi is null");
                return;
            }
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + seconds, seconds, pendingIntent);
    }

    private BroadcastReceiver imReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.w("heartbeat#im#receive action:%s", action);
            if (action.equals(ACTION_SENDING_HEARTBEAT)) {
                sendHeartBeatPacket();
            }
        }

        private void sendHeartBeatPacket() {
            logger.d("heartbeat#reqSendHeartbeat");
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "im_heartBeat_wakelock");
            wakeLock.acquire();
            try {
                final long timeOut = 5 * 1000;
                IMOther.IMHeartBeat imHeartBeat = IMOther.IMHeartBeat.newBuilder()
                        .build();
                int sid = IMBaseDefine.ServiceID.SID_OTHER_VALUE;
                int cid = IMBaseDefine.OtherCmdID.CID_OTHER_HEARTBEAT_VALUE;
                IMSocketManager.getInstance().sendRequest(imHeartBeat, sid, cid, new Packetlistener(timeOut) {
                    @Override
                    public void onSuccess(Object response) {
                        logger.d("heartbeat#心跳成功，链接保活");
                    }

                    @Override
                    public void onFaild() {
                        logger.w("heartbeat#心跳包发送失败");
                        IMSocketManager.getInstance().onMsgServerDisconn();
                    }

                    @Override
                    public void onTimeout() {
                        logger.w("heartbeat#心跳包发送超时");
                        IMSocketManager.getInstance().onMsgServerDisconn();
                    }
                });
            } finally {
                wakeLock.release();
            }
        }
    };
}
