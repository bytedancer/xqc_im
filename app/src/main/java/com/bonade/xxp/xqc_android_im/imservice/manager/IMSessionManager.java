package com.bonade.xxp.xqc_android_im.imservice.manager;

import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.SessionEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.ConfigurationSp;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.entity.UnreadMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.RecentInfo;
import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;
import com.bonade.xxp.xqc_android_im.imservice.event.SessionEvent;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMBuddy;
import com.bonade.xxp.xqc_android_im.protobuf.helper.EntityChangeEngine;
import com.bonade.xxp.xqc_android_im.protobuf.helper.Java2ProtoBuf;
import com.bonade.xxp.xqc_android_im.protobuf.helper.Json2JavaBean;
import com.bonade.xxp.xqc_android_im.protobuf.helper.ProtoBuf2JavaBean;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observer;
import rx.schedulers.Schedulers;

/**
 * app显示首页
 * 最近联系人列表
 */
public class IMSessionManager extends IMManager {

    private Logger logger = Logger.getLogger(IMSessionManager.class);

    private static IMSessionManager instance = new IMSessionManager();

    public static IMSessionManager getInstance() {
        return instance;
    }

    private IMSessionManager() {
    }

    private IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private IMLoginManager imLoginManager = IMLoginManager.getInstance();
    private DBInterface dbInterface = DBInterface.getInstance();
    private IMGroupManager imGroupManager = IMGroupManager.getInstance();

    // key = sessionKey --> sessionType_peerId
    private Map<String, SessionEntity> sessionMap = new ConcurrentHashMap<>();
    //SessionManager 状态字段
    private boolean sessionListReady = false;

    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        sessionListReady = false;
        sessionMap.clear();
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(SessionEvent event) {
        switch (event) {
            case RECENT_SESSION_LIST_SUCCESS:
                sessionListReady = true;
                break;
        }
        EventBus.getDefault().post(event);
    }

    public void onNormalLoginOk() {
        logger.d("recent#onLogin Successful");
        onLocalLoginOk();
        onLocalNetOk();
    }

    public void onLocalLoginOk() {
        logger.i("session#loadFromDb");
        List<SessionEntity> sessionInfoList = dbInterface.loadAllSession();
        for (SessionEntity sessionInfo : sessionInfoList) {
            sessionMap.put(sessionInfo.getSessionKey(), sessionInfo);
        }

        triggerEvent(SessionEvent.RECENT_SESSION_LIST_SUCCESS);
    }

    public void onLocalNetOk() {
        int latestUpdateTime = dbInterface.getSessionLastTime();
        logger.d("session#更新时间:%d", latestUpdateTime);
        reqGetRecentContacts(latestUpdateTime);
    }

    /**
     * 请求最近会话
     *
     * @param latestUpdateTime
     */
    private void reqGetRecentContacts(int latestUpdateTime) {
        ApiFactory.getMessageApi().getUnreadMsgList(imLoginManager.getLoginId())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseResponse<List<UnreadMessage>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ViewUtil.showMessage(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseResponse<List<UnreadMessage>> response) {
                        if (response == null
                                || response.getData() == null
                                || response.getData().isEmpty()) {
                            return;
                        }
                        onRepRecentContacts(response.getData());
                    }
                });
    }

    /**
     * 最近会话返回
     * 与本地的进行merge
     *
     * @param unreadMessages
     */
    public void onRepRecentContacts(List<UnreadMessage> unreadMessages) {
        Map<String, UnreadMessage> map = new HashMap<>();
        for (UnreadMessage unreadMessage : unreadMessages) {
            String sessionKey = unreadMessage.getSessionId();
            map.put(sessionKey, unreadMessage);
        }

        // 更新最近列表
        ArrayList<SessionEntity> needDB = new ArrayList<>();
        for (String sessionKey : map.keySet()) {
            SessionEntity sessionEntity = Json2JavaBean.getSessionEntity(map.get(sessionKey));
            sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
            needDB.add(sessionEntity);
        }
        logger.d("session#onRepRecentContacts is ready,now broadcast");
        // 将最新的session信息保存到DB中
        dbInterface.batchInsertOrUpdateSession(needDB);
        if (needDB.size() > 0) {
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }
    }

    /**
     * 请求删除会话
     *
     * @param recentInfo
     */
    public void reqRemoveSession(RecentInfo recentInfo) {
        logger.i("session#reqRemoveSession");
//
        int loginId = imLoginManager.getLoginId();
        String sessionKey = recentInfo.getSessionKey();
        // 直接本地先删除，清除未读消息
        if (sessionMap.containsKey(sessionKey)) {
            sessionMap.remove(sessionKey);
            IMUnreadMsgManager.getInstance().readUnreadSession(sessionKey);
            dbInterface.deleteSession(sessionKey);
            ConfigurationSp.getInstance(context, loginId).setSessionTop(sessionKey, false);
            triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
        }

//        IMBuddy.IMRemoveSessionReq removeSessionReq = IMBuddy.IMRemoveSessionReq
//                .newBuilder()
//                .setUserId(loginId)
//                .setSessionId(recentInfo.getPeerId())
//                .setSessionType(Java2ProtoBuf.getProtoSessionType(recentInfo.getSessionType()))
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE;
//        int cid = IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_REQ_VALUE;
//        imSocketManager.sendRequest(removeSessionReq, sid, cid);
    }

    /**
     * 删除会话返回
     */
    public void onRepRemoveSession(IMBuddy.IMRemoveSessionRsp removeSessionRsp) {
        logger.i("session#onRepRemoveSession");
        int resultCode = removeSessionRsp.getResultCode();
        if (0 != resultCode) {
            logger.e("session#removeSession failed");
            return;
        }
    }

    /**
     * 新建群组时候的更新
     *
     * @param groupEntity
     */
    public void updateSession(GroupEntity groupEntity) {
        logger.d("recent#updateSession GroupEntity:%s", groupEntity);
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setLatestMsgType(DBConstant.MSG_TYPE_GROUP_TEXT);
        sessionEntity.setUpdated(groupEntity.getUpdated());
        sessionEntity.setCreated(groupEntity.getCreated());
        sessionEntity.setLatestMsgData("[你创建的新群喔]");
        sessionEntity.setTalkId(groupEntity.getCreatorId());
        sessionEntity.setLatestMsgId(0);
        sessionEntity.setPeerId(groupEntity.getPeerId());
        sessionEntity.setPeerType(DBConstant.SESSION_TYPE_GROUP);
        sessionEntity.buildSessionKey();

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        ArrayList<SessionEntity> needDB = new ArrayList<>(1);
        needDB.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDB);
        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
    }

    /**
     * 1.自己发送消息
     * 2.收到消息
     *
     * @param msg
     */
    public void updateSession(MessageEntity msg) {
        logger.d("recent#updateSession msg:%s", msg);
        if (msg == null) {
            logger.d("recent#updateSession is end,cause by msg is null");
            return;
        }

        int loginId = imLoginManager.getLoginId();
        boolean isSend = msg.isSend(loginId);
        // 多端同步的问题
        int peerId = msg.getPeerId(isSend);

        SessionEntity sessionEntity = sessionMap.get(msg.getSessionKey());
        if (sessionEntity == null) {
            logger.d("session#updateSession#not found msgSessionEntity");
            sessionEntity = EntityChangeEngine.getSessionEntity(msg);
            sessionEntity.setPeerId(peerId);
            sessionEntity.buildSessionKey();
            // 判断群组的信息是否存在
            if (sessionEntity.getPeerType() == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = imGroupManager.findGroup(peerId);
                if (groupEntity == null) {
                    imGroupManager.reqGroupDetailInfo(peerId);
                }
            }
        } else {
            logger.d("session#updateSession#msgSessionEntity already in Map");
            sessionEntity.setUpdated(msg.getUpdated());
            sessionEntity.setLatestMsgData(msg.getMessageDisplay());
            sessionEntity.setTalkId(msg.getFromId());
            //todo check if msgid is null/0
            sessionEntity.setLatestMsgId(msg.getMsgId());
            sessionEntity.setLatestMsgType(msg.getMsgType());
        }

        // DB 先更新
        ArrayList<SessionEntity> needDB = new ArrayList<>(1);
        needDB.add(sessionEntity);
        dbInterface.batchInsertOrUpdateSession(needDB);

        sessionMap.put(sessionEntity.getSessionKey(), sessionEntity);
        triggerEvent(SessionEvent.RECENT_SESSION_LIST_UPDATE);
    }

    public List<SessionEntity> getRecentSessionList() {
        List<SessionEntity> recentInfoList = new ArrayList<>(sessionMap.values());
        return recentInfoList;
    }

    private static void sort(List<RecentInfo> data) {
        Collections.sort(data, new Comparator<RecentInfo>() {
            public int compare(RecentInfo o1, RecentInfo o2) {
                Integer a = o1.getUpdateTime();
                Integer b = o2.getUpdateTime();

                boolean isTopA = o1.isTop();
                boolean isTopB = o2.isTop();

                if (isTopA == isTopB) {
                    // 升序
                    //return a.compareTo(b);
                    // 降序
                    return b.compareTo(a);
                } else {
                    if (isTopA) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });
    }

    /**
     * 获取最近联系人列表，RecentInfo 是sessionEntity unreadEntity user/group 等等实体的封装
     *
     * @return
     */
    public List<RecentInfo> getRecentListInfo() {
        // 整理topList
        List<RecentInfo> recentSessionList = new ArrayList<>();
        int loginId = IMLoginManager.getInstance().getLoginId();

        List<SessionEntity> sessionList = getRecentSessionList();
        Map<Integer, UserEntity> userMap = IMContactManager.getInstance().getUserMap();
        Map<String, UnreadEntity> unreadMsgMap = IMUnreadMsgManager.getInstance().getUnreadMsgMap();
        Map<Integer, GroupEntity> groupEntityMap = IMGroupManager.getInstance().getGroupMap();
        HashSet<String> topList = ConfigurationSp.getInstance(context, loginId).getSessionTopList();

        for (SessionEntity recentSession : sessionList) {
            int sessionType = recentSession.getPeerType();
            int peerId = recentSession.getPeerId();
            String sessionKey = recentSession.getSessionKey();

            UnreadEntity unreadEntity = unreadMsgMap.get(sessionKey);
            if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
                GroupEntity groupEntity = groupEntityMap.get(peerId);
                groupEntity.setPeerId(peerId);
                RecentInfo recentInfo = new RecentInfo(recentSession, groupEntity, unreadEntity);
                if (topList != null && topList.contains(sessionKey)) {
                    recentInfo.setTop(true);
                }

                int lastFromId = recentSession.getTalkId();
                UserEntity talkUser = userMap.get(lastFromId);
                // 用户不存在
                if (talkUser != null) {
                    String oriContent = recentInfo.getLatestMsgData();
                    String finalContent = talkUser.getMainName() + ": " + oriContent;
                    recentInfo.setLatestMsgData(finalContent);
                }

                recentSessionList.add(recentInfo);
            } else if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
                UserEntity userEntity = userMap.get(peerId);
                RecentInfo recentInfo = new RecentInfo(recentSession, userEntity, unreadEntity);
                if (topList != null && topList.contains(sessionKey)) {
                    recentInfo.setTop(true);
                }
                recentSessionList.add(recentInfo);
            }
        }
        sort(recentSessionList);
        return recentSessionList;
    }

    public SessionEntity findSession(String sessionKey) {
        if (sessionMap.size() <= 0 || TextUtils.isEmpty(sessionKey)) {
            return null;
        }

        if (sessionMap.containsKey(sessionKey)) {
            return sessionMap.get(sessionKey);
        }

        return null;
    }

    public PeerEntity findPeerEntity(String sessionKey) {
        if (TextUtils.isEmpty(sessionKey)) {
            return null;
        }

        // 拆分
        PeerEntity peerEntity = null;
        String[] sessionInfo = EntityChangeEngine.spiltSessionKey(sessionKey);
        int peerType = Integer.parseInt(sessionInfo[0]);
        int peerId = Integer.parseInt(sessionInfo[1]);
        switch (peerType) {
            case DBConstant.SESSION_TYPE_SINGLE:
                peerEntity  = IMContactManager.getInstance().findContact(peerId);
                break;
            case DBConstant.SESSION_TYPE_GROUP:
                peerEntity = IMGroupManager.getInstance().findGroup(peerId);
                break;
            default:
                throw new IllegalArgumentException("findPeerEntity#peerType is illegal,cause by " + peerType);
        }
        return peerEntity;
    }

    public boolean isSessionListReady() {
        return sessionListReady;
    }

    public void setSessionListReady(boolean sessionListReady) {
        this.sessionListReady = sessionListReady;
    }
}
