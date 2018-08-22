package com.bonade.xxp.xqc_android_im.imservice.event;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;

import java.util.List;

/**
 * 异步刷新历史消息
 */
public class RefreshHistoryMsgEvent {

    private int pullTimes;
    private int lastMsgId;
    private int count;
    private List<MessageEntity> listMsg;
    private int peerId;
    private int peerType;
    private String sessionKey;

    public int getPullTimes() {
        return pullTimes;
    }

    public void setPullTimes(int pullTimes) {
        this.pullTimes = pullTimes;
    }

    public int getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(int lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<MessageEntity> getListMsg() {
        return listMsg;
    }

    public void setListMsg(List<MessageEntity> listMsg) {
        this.listMsg = listMsg;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerType() {
        return peerType;
    }

    public void setPeerType(int peerType) {
        this.peerType = peerType;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
