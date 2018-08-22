package com.bonade.xxp.xqc_android_im.imservice.event;

public class PriorityEvent {

    private Object object;
    private Event event;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public enum  Event{
        MSG_RECEIVED_MESSAGE
    }
}
