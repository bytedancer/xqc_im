package com.bonade.xxp.xqc_android_im.DB.entity;

import com.bonade.xxp.xqc_android_im.protobuf.helper.EntityChangeEngine;
import com.google.gson.annotations.SerializedName;

/**
 * 聊天对象抽象类  may be user/group
 */
public abstract class PeerEntity {

    protected Long cid;
    @SerializedName(value = "id", alternate = {"groupId"})
    protected int peerId;
    /** Not-null value.
     * userEntity --> nickName
     * groupEntity --> groupName
     * */
    @SerializedName(value = "userName", alternate = {"groupName"})
    protected String mainName;
    /** Not-null value.*/
    @SerializedName(value = "userLogo", alternate = {"groupLogo"})
    protected String avatar;
    protected int created;
    protected int updated;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    // peer就能生成sessionKey
    public String getSessionKey(){
        return EntityChangeEngine.getSessionKey(peerId,getType());
    }

    public abstract int getType();
}
