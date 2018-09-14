package com.bonade.xxp.xqc_android_im.imservice.manager;

import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMLogin;
import com.bonade.xxp.xqc_android_im.protobuf.IMMessage;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.google.protobuf.CodedInputStream;

import java.io.IOException;

/**
 * 消息分发中心，处理消息服务器返回的数据包
 * 1. decode  header与body的解析
 * 2. 分发
 */
public class IMPacketDispatcher {

    private static Logger logger = Logger.getLogger(IMPacketDispatcher.class);

    public static void packetDispatcher(short flag, CodedInputStream buffer) {
        try {
            switch (flag) {
                case SysConstant.PROTOCOL_FLAG_MESSAGE:
                    IMMessage.IMMsgData imMsgData = IMMessage.IMMsgData.parseFrom(buffer);
                    IMMessageManager.getInstance().onRecvMessage(imMsgData);
                    break;
            }
        } catch (IOException e) {
            logger.e("packetDispatcher# error,flag:%d", flag);
        }

    }
}
