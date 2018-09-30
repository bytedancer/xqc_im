package com.bonade.xxp.xqc_android_im.imservice.entity;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.SessionEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;

import java.util.ArrayList;
import java.util.List;

public class RecentInfo {

    /**
     * SessionEntity
     */
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int latestMsgType;
    private int latestMsgId;
    private String latestMsgData;
    private int updateTime;

    /**
     * UnreadEntity
     */
    private int unReadCount;

    /**
     * Group/UserEntity
     */
    private String name;
    private List<UserEntity> userEntities;

    /**
     * 是否置顶
     */
    private boolean isTop = false;
    /**
     * 是否屏蔽消息
     */
    private boolean isForbidden = false;

    public RecentInfo(){}

    public RecentInfo(SessionEntity sessionEntity, UserEntity userEntity, UnreadEntity unreadEntity){
        sessionKey = sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_SINGLE;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if(unreadEntity !=null)
            unReadCount = unreadEntity.getUnReadCount();

        if(userEntity != null){
            name = userEntity.getMainName();

            // 免打扰的设定
            int status = userEntity.getStatus();
            if (status == DBConstant.STATUS_SHIELD) {
                isForbidden = true;
            }

            ArrayList<UserEntity> list = new ArrayList<>(1);
            list.add(userEntity);
            userEntities = list;
        }
    }

    public RecentInfo(SessionEntity sessionEntity, GroupEntity groupEntity, UnreadEntity unreadEntity) {
        sessionKey = sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_GROUP;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if (unreadEntity != null) {
            unReadCount = unreadEntity.getUnReadCount();
        }

        if (groupEntity != null) {
            name = groupEntity.getMainName();

            // 免打扰的设定
            int status = groupEntity.getStatus();
            if (status == DBConstant.STATUS_SHIELD) {
                isForbidden = true;
            }

            ArrayList<UserEntity> userList = new ArrayList<>();
            ArrayList<Integer> list = new ArrayList<>();
            list.addAll(groupEntity.getGroupMemberIds());
            for (Integer userId : list) {
                UserEntity entity = IMContactManager.getInstance().findContact(userId);

                if (entity != null) {
                    userList.add(entity);
                }
                if (userList.size() >= 9) {
                    break;
                }
            }
            userEntities = userList;
        }
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgId() {
        return latestMsgId;
    }

    public void setLatestMsgId(int latestMsgId) {
        this.latestMsgId = latestMsgId;
    }

    public String getLatestMsgData() {
        return latestMsgData;
    }

    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserEntity> getUserEntities() {
        return userEntities;
    }

    public void setUserEntities(List<UserEntity> userEntities) {
        this.userEntities = userEntities;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public boolean isForbidden() {
        return isForbidden;
    }

    public void setForbidden(boolean forbidden) {
        isForbidden = forbidden;
    }
}
