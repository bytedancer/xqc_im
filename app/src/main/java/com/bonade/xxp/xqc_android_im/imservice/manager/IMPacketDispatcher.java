package com.bonade.xxp.xqc_android_im.imservice.manager;

import com.google.protobuf.CodedInputStream;

/**
 * 消息分发中心，处理消息服务器返回的数据包
 * 1. decode  header与body的解析
 * 2. 分发
 */
public class IMPacketDispatcher {

    public static void loginPacketDispatcher(int commandId, CodedInputStream buffer) {

    }

    public static void buddyPacketDispatcher(int commandId, CodedInputStream buffer) {

    }

    public static void msgPacketDispatcher(int commandId, CodedInputStream buffer) {

    }

    public static void groupPacketDispatcher(int commandId,CodedInputStream buffer) {

    }
}
