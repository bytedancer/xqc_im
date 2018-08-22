package com.bonade.xxp.xqc_android_im.imservice.event;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;

import java.util.List;

public class GroupEvent {

    private GroupEntity groupEntity;
    private Event event;

    /**
     * 很多场景只是关心改变的类型以及change的Ids
     */
    private int changeType;
    private List<Integer> changeList;

    public GroupEvent(Event event) {
        this.event = event;
    }

    public GroupEvent(GroupEntity groupEntity, Event event) {
        this.groupEntity = groupEntity;
        this.event = event;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public List<Integer> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<Integer> changeList) {
        this.changeList = changeList;
    }

    public enum Event {

        NONE,

        // 改变群信息
        GROUP_INFO_OK,
        GROUP_INFO_UPDATED,

        // 改变群成员
        CHANGE_GROUP_MEMBER_SUCCESS,
        CHANGE_GROUP_MEMBER_FAIL,
        CHANGE_GROUP_MEMBER_TIMEOUT,

        // 创建群
        CREATE_GROUP_SUCCESS,
        CREATE_GROUP_FAIL,
        CREATE_GROUP_TIMEOUT,

        // 屏蔽群
        SHIELD_GROUP_SUCCESS,
        SHIELD_GROUP_FAIL,
        SHIELD_GROUP_TIMEOUT
    }
}
