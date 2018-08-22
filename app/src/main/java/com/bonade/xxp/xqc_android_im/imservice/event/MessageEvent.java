package com.bonade.xxp.xqc_android_im.imservice.event;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;

import java.util.ArrayList;

public class MessageEvent {

    private ArrayList<MessageEntity> msgList;
    private Event event;

    public MessageEvent() {

    }

    public MessageEvent(Event event) {
        //默认值 初始化使用
        this.event = event;
    }

    public MessageEvent(MessageEntity entity, Event event) {
        //默认值 初始化使用
        this.event = event;
        msgList = new ArrayList<>();
        msgList.add(entity);
    }

    public enum Event {

        NONE,
        HISTORY_MSG_OBTAIN,

        SENDING_MESSAGE,

        ACK_SEND_MESSAGE_OK,
        ACK_SEND_MESSAGE_TIME_OUT,
        ACK_SEND_MESSAGE_FAILURE,

        HANDLER_IMAGE_UPLOAD_FAILD,
        IMAGE_UPLOAD_FAILD,
        HANDLER_IMAGE_UPLOAD_SUCCESS,
        IMAGE_UPLOAD_SUCCESS
    }

    public MessageEntity getMessageEntity() {
        if (msgList == null || msgList.isEmpty()) {
            return null;
        }

        return msgList.get(0);
    }

    public void setMessageEntity(MessageEntity messageEntity) {
        if (msgList == null) {
            msgList = new ArrayList<>();
        }
        msgList.clear();
        msgList.add(messageEntity);
    }

    public ArrayList<MessageEntity> getMsgList() {
        return msgList;
    }

    public void setMsgList(ArrayList<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
