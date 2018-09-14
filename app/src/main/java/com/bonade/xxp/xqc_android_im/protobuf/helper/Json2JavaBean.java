package com.bonade.xxp.xqc_android_im.protobuf.helper;

import com.bonade.xxp.xqc_android_im.DB.entity.SessionEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.http.entity.UnreadMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.MsgAnalyzeEngine;
import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;

public class Json2JavaBean {

    public static SessionEntity getSessionEntity(UnreadMessage unreadMessage) {
        SessionEntity sessionEntity = new SessionEntity();
        int msgType = getMsgType(unreadMessage.getMsgType(), unreadMessage.getMsgContentType());
        sessionEntity.setLatestMsgType(msgType);
        int sessionType = getSessionType(unreadMessage.getMsgType());
        sessionEntity.setPeerType(sessionType);
        int peerId;
        if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
            peerId = unreadMessage.getToUserId();
        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
            peerId = unreadMessage.getGroupId();
        } else {
            throw new IllegalArgumentException("peerId is illegal");
        }
        sessionEntity.setPeerId(peerId);
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(unreadMessage.getFromUserId());
        sessionEntity.setLatestMsgId(unreadMessage.getMsgId());
        sessionEntity.setCreated((int) unreadMessage.getTimestamp() / 1000);
        sessionEntity.setUpdated((int) unreadMessage.getTimestamp() / 1000);
        String content  = unreadMessage.getMsgContent();
        // 判断具体的类型是什么
        if(msgType == DBConstant.MSG_TYPE_GROUP_TEXT ||
                msgType ==DBConstant.MSG_TYPE_SINGLE_TEXT){
            content =  MsgAnalyzeEngine.analyzeMessageDisplay(content);
        }
        sessionEntity.setLatestMsgData(content);
        return sessionEntity;
    }

    public static UnreadEntity getUnreadEntity(UnreadMessage unreadMessage, int unReadCount) {
        UnreadEntity unreadEntity = new UnreadEntity();
        unreadEntity.buildSessionKey();
        int sessionType = getSessionType(unreadMessage.getMsgType());
        int peerId;
        if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
            peerId = unreadMessage.getToUserId();
        } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
            peerId = unreadMessage.getGroupId();
        } else {
            throw new IllegalArgumentException("peerId is illegal");
        }
        unreadEntity.setSessionType(sessionType);
        unreadEntity.setPeerId(peerId);
        unreadEntity.setMainName(unreadMessage.getFromUserNickName());
        unreadEntity.setAvatar(unreadMessage.getFromUserLogo());
        unreadEntity.setUnReadCount(unReadCount);
        unreadEntity.setLatestMsgId(unreadMessage.getMsgId());
        unreadEntity.setLatestMsgData(unreadMessage.getMsgContent());
        return unreadEntity;
    }

    public static int getSessionType(int msgType) {
        switch (msgType) {
            case DBConstant.SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case DBConstant.SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getMsgType#" + msgType);
        }
    }

    public static int getMsgType(int msgType, int msgContentType) {
        switch (msgType) {
            case DBConstant.SESSION_TYPE_SINGLE:
                if (msgContentType == SysConstant.MSG_CONTENT_TYPE_TEXT) {
                    return DBConstant.MSG_TYPE_SINGLE_TEXT;
                } else if (msgContentType == SysConstant.MSG_CONTENT_TYPE_IMAGE) {
                    return DBConstant.MSG_TYPE_SINGLE_IMAGE;
                } else {
                    throw new IllegalArgumentException("cause by #msgContentType#" + msgContentType);
                }
            case DBConstant.SESSION_TYPE_GROUP:
                if (msgContentType == SysConstant.MSG_CONTENT_TYPE_TEXT) {
                    return DBConstant.MSG_TYPE_GROUP_TEXT;
                } else if (msgContentType == SysConstant.MSG_CONTENT_TYPE_IMAGE) {
                    return DBConstant.MSG_TYPE_GROUP_IMAGE;
                } else {
                    throw new IllegalArgumentException("cause by #msgContentType#" + msgContentType);
                }
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #msgType#" + msgType);
        }
    }

}
