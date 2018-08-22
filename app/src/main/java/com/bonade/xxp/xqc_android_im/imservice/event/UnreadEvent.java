package com.bonade.xxp.xqc_android_im.imservice.event;

import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;

public class UnreadEvent {

    private UnreadEntity unreadEntity;
    private Event event;

    public UnreadEvent(){

    }

    public UnreadEvent(Event event){
        this.event = event;
    }

    public UnreadEntity getUnreadEntity() {
        return unreadEntity;
    }

    public void setUnreadEntity(UnreadEntity unreadEntity) {
        this.unreadEntity = unreadEntity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public enum Event {
        UNREAD_MSG_LIST_OK,
        UNREAD_MSG_RECEIVED,

        SESSION_READED_UNREAD_MSG
    }
}
