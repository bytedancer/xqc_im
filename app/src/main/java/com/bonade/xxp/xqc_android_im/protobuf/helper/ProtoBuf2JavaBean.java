package com.bonade.xxp.xqc_android_im.protobuf.helper;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.SessionEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.http.entity.UnreadMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.MsgAnalyzeEngine;
import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMGroup;
import com.bonade.xxp.xqc_android_im.protobuf.IMMessage;
import com.bonade.xxp.xqc_android_im.util.pinyin.PinYin;

public class ProtoBuf2JavaBean {

    public static GroupEntity getGroupEntity(IMBaseDefine.GroupInfo groupInfo) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setMainName(groupInfo.getGroupName());
        groupEntity.setAvatar(groupInfo.getGroupAvatar());
        groupEntity.setCreatorId(groupInfo.getGroupCreatorId());
        groupEntity.setPeerId(groupInfo.getGroupId());
        groupEntity.setGroupType(getJavaGroupType(groupInfo.getGroupType()));
        groupEntity.setStatus(groupInfo.getShieldStatus());
        groupEntity.setUserCount(groupInfo.getGroupMemberListCount());
        groupEntity.setVersion(groupInfo.getVersion());
        groupEntity.setGroupMemberIds(groupInfo.getGroupMemberListList());

        // may be not good place
        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());

        return groupEntity;
    }

    /**
     * 创建群时候的转化
     *
     * @param groupCreateRsp
     * @return
     */
    public static GroupEntity getGroupEntity(IMGroup.IMGroupCreateRsp groupCreateRsp) {
        GroupEntity groupEntity = new GroupEntity();
        int timeNow = (int) (System.currentTimeMillis() / 1000);
        groupEntity.setMainName(groupCreateRsp.getGroupName());
        groupEntity.setGroupMemberIds(groupCreateRsp.getUserIdListList());
        groupEntity.setCreatorId(groupCreateRsp.getUserId());
        groupEntity.setPeerId(groupCreateRsp.getGroupId());

        groupEntity.setUpdated(timeNow);
        groupEntity.setCreated(timeNow);
        groupEntity.setAvatar("");
        groupEntity.setGroupType(DBConstant.GROUP_TYPE_TEMP);
        groupEntity.setStatus(DBConstant.STATUS_ONLINE);
        groupEntity.setUserCount(groupCreateRsp.getUserIdListCount());
        groupEntity.setVersion(1);

        PinYin.getPinYin(groupEntity.getMainName(), groupEntity.getPinyinElement());
        return groupEntity;
    }

    public static UserEntity getUserEntity(IMBaseDefine.UserInfo userInfo) {
        UserEntity userEntity = new UserEntity();
//        int timeNow = (int) (System.currentTimeMillis()/1000);
//
//        userEntity.setStatus(userInfo.getStatus());
//        userEntity.setAvatar(userInfo.getAvatarUrl());
//        userEntity.setCreated(timeNow);
//        userEntity.setDepartmentId(userInfo.getDepartmentId());
//        userEntity.setEmail(userInfo.getEmail());
//        userEntity.setGender(userInfo.getUserGender());
//        userEntity.setMainName(userInfo.getUserNickName());
//        userEntity.setPhone(userInfo.getUserTel());
//        userEntity.setPinyinName(userInfo.getUserDomain());
//        userEntity.setRealName(userInfo.getUserRealName());
//        userEntity.setUpdated(timeNow);
//        userEntity.setPeerId(userInfo.getUserId());
//
//        PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        return userEntity;
    }

    public static SessionEntity getSessionEntity(IMBaseDefine.ContactSessionInfo sessionInfo) {
        SessionEntity sessionEntity = new SessionEntity();

//        int msgType = getJavaMsgType(sessionInfo.getLatestMsgType());
//        sessionEntity.setLatestMsgType(msgType);
        sessionEntity.setPeerType(getJavaSessionType(sessionInfo.getSessionType()));
        sessionEntity.setPeerId(sessionInfo.getSessionId());
        sessionEntity.buildSessionKey();
        sessionEntity.setTalkId(sessionInfo.getLatestMsgFromUserId());
        sessionEntity.setLatestMsgId(sessionInfo.getLatestMsgId());
        sessionEntity.setCreated(sessionInfo.getUpdatedTime());

        String content = sessionInfo.getLatestMsgData().toStringUtf8();
//        String desMessage = new String(com.mogujie.tt.Security.getInstance().DecryptMsg(content));
//        // 判断具体的类型是什么
//        if(msgType == DBConstant.MSG_TYPE_GROUP_TEXT ||
//                msgType ==DBConstant.MSG_TYPE_SINGLE_TEXT){
//            desMessage =  MsgAnalyzeEngine.analyzeMessageDisplay(desMessage);
//        }

        sessionEntity.setLatestMsgData(sessionInfo.getLatestMsgData().toString());
        sessionEntity.setUpdated(sessionInfo.getUpdatedTime());
        return sessionEntity;
    }

    /**
     * 拆分消息在上层做掉 图文混排
     * 在这判断
     */
//    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
//        MessageEntity messageEntity = null;
//        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
//        switch (msgType) {
//            case MSG_TYPE_SINGLE_AUDIO:
//            case MSG_TYPE_GROUP_AUDIO:
//                try {
//                    /**语音的解析不能转自 string再返回来*/
//                    messageEntity = analyzeAudio(msgInfo);
//                } catch (JSONException e) {
//                    return null;
//                } catch (UnsupportedEncodingException e) {
//                    return null;
//                }
//                break;

//            case MSG_TYPE_GROUP_TEXT:
//            case MSG_TYPE_SINGLE_TEXT:
//                messageEntity = analyzeText(msgInfo);
//                break;
//            default:
//                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
//        }
//        return messageEntity;
//    }
    public static MessageEntity analyzeText(IMMessage.IMMsgData msgData) {
        return MsgAnalyzeEngine.analyzeMessage(msgData);
    }

//    public static AudioMessage analyzeAudio(IMBaseDefine.MsgInfo msgInfo) throws JSONException, UnsupportedEncodingException {
//        AudioMessage audioMessage = new AudioMessage();
//        audioMessage.setFromId(msgInfo.getFromSessionId());
//        audioMessage.setMsgId(msgInfo.getMsgId());
//        audioMessage.setMsgType(getJavaMsgType(msgInfo.getMsgType()));
//        audioMessage.setStatus(MessageConstant.MSG_SUCCESS);
//        audioMessage.setReadStatus(MessageConstant.AUDIO_UNREAD);
//        audioMessage.setDisplayType(DBConstant.SHOW_AUDIO_TYPE);
//        audioMessage.setCreated(msgInfo.getCreateTime());
//        audioMessage.setUpdated(msgInfo.getCreateTime());
//
//        ByteString bytes = msgInfo.getMsgData();
//
//        byte[] audioStream = bytes.toByteArray();
//        if(audioStream.length < 4){
//            audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
//            audioMessage.setAudioPath("");
//            audioMessage.setAudiolength(0);
//        }else {
//            int msgLen = audioStream.length;
//            byte[] playTimeByte = new byte[4];
//            byte[] audioContent = new byte[msgLen - 4];
//
//            System.arraycopy(audioStream, 0, playTimeByte, 0, 4);
//            System.arraycopy(audioStream, 4, audioContent, 0, msgLen - 4);
//            int playTime = CommonUtil.byteArray2int(playTimeByte);
//            String audioSavePath = FileUtil.saveAudioResourceToFile(audioContent, audioMessage.getFromId());
//            audioMessage.setAudiolength(playTime);
//            audioMessage.setAudioPath(audioSavePath);
//        }
//
//        /**抽离出来 或者用gson*/
//        JSONObject extraContent = new JSONObject();
//        extraContent.put("audioPath",audioMessage.getAudioPath());
//        extraContent.put("audiolength",audioMessage.getAudiolength());
//        extraContent.put("readStatus",audioMessage.getReadStatus());
//        String audioContent = extraContent.toString();
//        audioMessage.setContent(audioContent);
//
//        return audioMessage;
//    }

    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData) {

        MessageEntity messageEntity = null;
        int msgType = msgData.getMsgType();
        int msgContentType = msgData.getMsgContentType();

        switch (msgContentType) {
            case SysConstant.MSG_CONTENT_TYPE_TEXT:
                messageEntity = analyzeText(msgData);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if (messageEntity != null) {
            if (msgType == DBConstant.SESSION_TYPE_SINGLE) {
                messageEntity.setToId(Integer.parseInt(msgData.getToUserId()));
            } else {
                messageEntity.setToId(Integer.parseInt(msgData.getGroupId()));
            }
        }

        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }

    public static UnreadEntity getUnreadEntity(IMBaseDefine.UnreadInfo pbInfo) {
        UnreadEntity unreadEntity = new UnreadEntity();
        unreadEntity.setSessionType(getJavaSessionType(pbInfo.getSessionType()));
        unreadEntity.setLatestMsgData(pbInfo.getLatestMsgData().toString());
        unreadEntity.setPeerId(pbInfo.getSessionId());
        unreadEntity.setLatestMsgId(pbInfo.getLatestMsgId());
        unreadEntity.setUnReadCount(pbInfo.getUnreadCnt());
        unreadEntity.buildSessionKey();
        return unreadEntity;
    }

    public static int getJavaMsgType(int msgType, int msgContentType) {
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

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType) {
        switch (sessionType) {
            case SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" + sessionType);
        }
    }

    public static int getJavaGroupType(IMBaseDefine.GroupType groupType) {
        switch (groupType) {
            case GROUP_TYPE_NORMAL:
                return DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return DBConstant.GROUP_TYPE_TEMP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" + groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.GroupModifyType modifyType) {
        switch (modifyType) {
            case GROUP_MODIFY_TYPE_ADD:
                return DBConstant.GROUP_MODIFY_TYPE_ADD;
            case GROUP_MODIFY_TYPE_DEL:
                return DBConstant.GROUP_MODIFY_TYPE_DEL;
            default:
                throw new IllegalArgumentException("GroupModifyType is illegal,cause by " + modifyType);
        }
    }
}
