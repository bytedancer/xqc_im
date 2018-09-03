package com.bonade.xxp.xqc_android_im.config;

/**
 * 通讯常量类
 */
public class HandlerConstant {

    /**
     * 消息相关
     */
    public static final int HANDLER_RECORD_FINISHED = 0x01; // 录音结束
    public static final int HANDLER_STOP_PLAY = 0x02;// Speex 通知主界面停止播放
    public static final int RECEIVE_MAX_VOLUME = 0x03;
    public static final int RECORD_AUDIO_TOO_LONG = 0x04;
    public static final int MSG_RECEIVED_MESSAGE = 0x05;
}
