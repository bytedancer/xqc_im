package com.bonade.xxp.xqc_android_im.imservice.manager;

import android.content.Intent;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.SessionEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.callback.Packetlistener;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.TextMessage;
import com.bonade.xxp.xqc_android_im.imservice.event.MessageEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.PriorityEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.RefreshHistoryMsgEvent;
import com.bonade.xxp.xqc_android_im.imservice.service.LoadImageService;
import com.bonade.xxp.xqc_android_im.imservice.support.SequenceNumberMaker;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMMessage;
import com.bonade.xxp.xqc_android_im.protobuf.helper.EntityChangeEngine;
import com.bonade.xxp.xqc_android_im.protobuf.helper.Java2ProtoBuf;
import com.bonade.xxp.xqc_android_im.protobuf.helper.ProtoBuf2JavaBean;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息的处理
 */
public class IMMessageManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageManager.class);

    private static IMMessageManager instance = new IMMessageManager();

    public static IMMessageManager getInstance() {
        return instance;
    }

    private IMMessageManager() {
    }

    private IMSocketManager imSocketManager = IMSocketManager.getInstance();
    private IMSessionManager imSessionManager = IMSessionManager.getInstance();
    private DBInterface dbInterface = DBInterface.getInstance();

    // 消息发送超时时间（自定义）
    private final long TIMEOUT_MILLISECONDS = 6 * 1000;
    private final long IMAGE_TIMEOUT_MILLISECONDS = 4 * 60 * 1000;

    private long getTimeoutTolerance(MessageEntity msg) {
        switch (msg.getDisplayType()) {
            case DBConstant.SHOW_IMAGE_TYPE:
                return IMAGE_TIMEOUT_MILLISECONDS;
            default:
                break;
        }
        return TIMEOUT_MILLISECONDS;
    }

    /**
     * 接受到消息，并且向服务端发送确认
     *
     * @param msg
     */
    public void ackReceiveMsg(MessageEntity msg) {
//        logger.d("chat#ackReceiveMsg -> msg:%s", msg);
//        IMBaseDefine.SessionType sessionType = Java2ProtoBuf.getProtoSessionType(msg.getSessionType());
//        IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.newBuilder()
//                .setMsgId(msg.getMsgId())
//                .setSessionId(msg.getToId())
//                .setUserId(msg.getFromId())
//                .setSessionType(sessionType)
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_ACK_VALUE;
//        imSocketManager.sendRequest(imMsgDataAck, sid, cid);
    }

    @Override
    public void doOnStart() {

    }

    public void onLoginSuccess() {
        if (!EventBus.getDefault().isRegistered(instance)) {
            EventBus.getDefault().register(instance);
        }
    }

    @Override
    public void reset() {
        EventBus.getDefault().unregister(instance);
    }

    /**
     * 自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**
     * 图片的处理放在这里，因为在发送图片的过程中，很可能messageActivity已经关闭掉
     *
     * @param event
     */
    @Subscribe
    public void onEvent(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        switch (type) {
            case IMAGE_UPLOAD_FAILD:
                logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                imageMessage.setStatus(MessageConstant.MSG_FAILURE);
                dbInterface.insertOrUpdateMessage(imageMessage);

                /**通知Activity层 失败*/
                event.setEvent(MessageEvent.Event.HANDLER_IMAGE_UPLOAD_FAILD);
                event.setMessageEntity(imageMessage);
                triggerEvent(event);
                break;
            case IMAGE_UPLOAD_SUCCESS:
                onImageLoadSuccess(event);
                break;
        }
    }

    /**
     * 事件的处理会在一个后台线程中执行，对应的函数名是onEventBackgroundThread，
     * 虽然名字是BackgroundThread，事件处理是在后台线程，
     * 但事件处理时间还是不应该太长
     * 因为如果发送事件的线程是后台线程，会直接执行事件，
     * 如果当前线程是UI线程，事件会被加到一个队列中，由一个线程依次处理这些事件，
     * 如果某个事件处理时间太长，会阻塞后面的事件的派发或处理
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackgroundThread(RefreshHistoryMsgEvent historyMsgEvent) {
        doRefreshLocalMsg(historyMsgEvent);
    }

    /**
     * 发送消息，最终的状态情况
     * MessageManager下面的拆分
     * 应该是自己发的信息，所以msgId为0
     * 这个地方用DB id作为主键
     *
     * @param msg
     */
    public void sendMessage(MessageEntity msg) {
        logger.d("chat#sendMessage, msg:%s", msg);
        // 发送情况下 msg_id 都是0
        // 服务端是从1开始计数的
//        if (!SequenceNumberMaker.getInstance().isFailure(msg.getMsgId())) {
//            throw new RuntimeException("#sendMessage# msgId is wrong,cause by 0!");
//        }
//
//        IMBaseDefine.MsgType msgType = Java2ProtoBuf.getProtoMsgType(msg.getMsgType());
//        byte[] sendContent = msg.getSendContent();
//
//        IMMessage.IMMsgData msgData = IMMessage.IMMsgData.newBuilder()
//                .setFromUserId(msg.getFromId())
//                .setToSessionId(msg.getToId())
//                .setMsgId(0)
//                .setCreateTime(msg.getCreated())
//                .setMsgType(msgType)
//                .setMsgData(ByteString.copyFrom(sendContent))  // 这个点要特别注意 todo ByteString.copyFrom
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE;
//
//        final MessageEntity messageEntity = msg;
//        imSocketManager.sendRequest(msgData, sid, cid, new Packetlistener(getTimeoutTolerance(messageEntity)) {
//            @Override
//            public void onSuccess(Object response) {
//                try {
//                    IMMessage.IMMsgDataAck imMsgDataAck = IMMessage.IMMsgDataAck.parseFrom((CodedInputStream) response);
//                    logger.i("chat#onAckSendedMsg");
//                    if (imMsgDataAck.getMsgId() <= 0) {
//                        throw new RuntimeException("Msg ack error,cause by msgId <=0");
//                    }
//                    messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
//                    messageEntity.setMsgId(imMsgDataAck.getMsgId());
//                    // 主键ID已经存在，直接替换
//                    dbInterface.insertOrUpdateMessage(messageEntity);
//                    // 更新sessionEntity lastMsgId问题
//                    imSessionManager.updateSession(messageEntity);
//                    triggerEvent(new MessageEvent(messageEntity, MessageEvent.Event.ACK_SEND_MESSAGE_OK));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFaild() {
//                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
//                dbInterface.insertOrUpdateMessage(messageEntity);
//                triggerEvent(new MessageEvent(messageEntity, MessageEvent.Event.ACK_SEND_MESSAGE_FAILURE));
//            }
//
//            @Override
//            public void onTimeout() {
//                messageEntity.setStatus(MessageConstant.MSG_FAILURE);
//                dbInterface.insertOrUpdateMessage(messageEntity);
//                triggerEvent(new MessageEvent(messageEntity, MessageEvent.Event.ACK_SEND_MESSAGE_TIME_OUT));
//            }
//        });
    }

    /**
     * 收到服务端原始信息
     * 1. 解析消息的类型
     * 2. 根据不同的类型,转化成不同的消息
     * 3. 先保存在DB[insertOrreplace]中，session的更新，Unread的更新
     * 4. 上层通知
     *
     * @param imMsgData
     */
    public void onRecvMessage(IMMessage.IMMsgData imMsgData) {
        logger.i("chat#onRecvMessage");
        if (imMsgData == null) {
            logger.e("chat#decodeMessageInfo failed,cause by is null");
            return;
        }

        MessageEntity recvMessage = ProtoBuf2JavaBean.getMessageEntity(imMsgData);
        int loginId = IMLoginManager.getInstance().getLoginId();
        boolean isSend = recvMessage.isSend(loginId);
        recvMessage.buildSessionKey(isSend);
        recvMessage.setStatus(MessageConstant.MSG_SUCCESS);

        // 对于混合消息，未读消息计数还是1,session已经更新
        dbInterface.insertOrUpdateMessage(recvMessage);
        imSessionManager.updateSession(recvMessage);

        // 发送已读确认由上层的activity处理 特殊处理
        // 1. 未读计数、 通知、session页面 2. 当前会话
        PriorityEvent notifyEvent = new PriorityEvent();
        notifyEvent.setEvent(PriorityEvent.Event.MSG_RECEIVED_MESSAGE);
        notifyEvent.setObject(recvMessage);
        triggerEvent(notifyEvent);
    }

    /**
     * 1. 先保存DB
     * 2. push到adapter中
     * 3. 等待ack,更新页面
     *
     * @param textMessage
     */
    public void sendText(TextMessage textMessage) {
        logger.i("chat#text#textMessage");
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        long pkId =  DBInterface.getInstance().insertOrUpdateMessage(textMessage);
        imSessionManager.updateSession(textMessage);
        sendMessage(textMessage);
    }

//    public void sendVoice(AudioMessage audioMessage) {
//        logger.i("chat#audio#sendVoice");
//        audioMessage.setStatus(MessageConstant.MSG_SENDING);
//        long pkId =  DBInterface.instance().insertOrUpdateMessage(audioMessage);
//        sessionManager.updateSession(audioMessage);
//        sendMessage(audioMessage);
//    }

    public void sendSingleImage(ImageMessage msg) {
        logger.d("ImMessageManager#sendImage ");
        ArrayList<ImageMessage> msgList = new ArrayList<>();
        msgList.add(msg);
        sendImages(msgList);
    }

    /**
     * 发送图片消息
     *
     * @param msgList
     */
    public void sendImages(List<ImageMessage> msgList) {
        logger.i("chat#image#sendImages size:%d", msgList.size());
        if (null == msgList || msgList.isEmpty()) {
            return;
        }

        int len = msgList.size();
        ArrayList<MessageEntity> needDBList = new ArrayList<>();
        for (ImageMessage msg : msgList) {
            needDBList.add(msg);
        }
        dbInterface.batchInsertOrUpdateMessage(needDBList);

        for (ImageMessage msg : msgList) {
            logger.d("chat#pic#sendImage  msg:%s", msg);
            // image message would wrapped as a text message after uploading
            int loadStatus = msg.getLoadStatus();

            switch (loadStatus) {
                case MessageConstant.IMAGE_LOADED_FAILURE:
                case MessageConstant.IMAGE_UNLOAD:
                case MessageConstant.IMAGE_LOADING:
                    msg.setLoadStatus(MessageConstant.IMAGE_LOADING);
                    Intent loadImageIntent = new Intent(context, LoadImageService.class);
                    loadImageIntent.putExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS, msg);
                    context.startService(loadImageIntent);
                    break;
                case MessageConstant.IMAGE_LOADED_SUCCESS:
                    sendMessage(msg);
                default:
                    throw new RuntimeException("sendImages#status不可能出现的状态");
            }
        }

        // 将最后一条更新到Session上面
        imSessionManager.updateSession(msgList.get(len - 1));
    }

    /**
     * 重新发送 message数据包
     * 1.检测DB状态
     * 2.删除DB状态 [不用删除]
     * 3.调用对应的发送
     * 判断消息的类型、判断是否是重发的状态
     *
     * @param msg
     */
    public void resendMessage(MessageEntity msg) {
        if (msg == null) {
            logger.d("chat#resendMessage msgInfo is null or already send success!");
            return;
        }

        // check 历史原因处理
        if (!SequenceNumberMaker.getInstance().isFailure(msg.getMsgId())) {
            // 之前的状态处理有问题
            msg.setStatus(MessageConstant.MSG_SUCCESS);
            dbInterface.insertOrUpdateMessage(msg);
            triggerEvent(new MessageEvent(msg, MessageEvent.Event.ACK_SEND_MESSAGE_OK));
            return;
        }

        logger.d("chat#resendMessage msgInfo %s", msg);
        // 重新设定message的时间,已经从DB中删除
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        msg.setUpdated(nowTime);
        msg.setCreated(nowTime);

        // 判断信息的类型
        int msgType = msg.getDisplayType();
        switch (msgType) {
            case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                sendText((TextMessage) msg);
                break;
            case DBConstant.SHOW_IMAGE_TYPE:
                sendSingleImage((ImageMessage) msg);
                break;
//            case DBConstant.SHOW_AUDIO_TYPE:
//                sendVoice((AudioMessage)msgInfo);
//                break;
            default:
                throw new IllegalArgumentException("#resendMessage#enum type is wrong!!,cause by displayType" + msgType);
        }
    }

    /**
     * 拉取历史消息 {from MessageActivity}
     *
     * @param pullTimes
     * @param sessionKey
     * @param peerEntity
     * @return
     */
    public List<MessageEntity> loadHistoryMsg(int pullTimes, String sessionKey, PeerEntity peerEntity) {
        int lastMsgId = 99999999;
        int lastCreateTime = 1455379200;
        int count = SysConstant.MSG_CNT_PER_PAGE;
        SessionEntity sessionEntity = IMSessionManager.getInstance().findSession(sessionKey);
        if (sessionEntity != null) {
            // 以前已经聊过天，删除之后，sessionEntity不存在
            logger.i("#loadHistoryMsg# sessionEntity is null");
            lastMsgId = sessionEntity.getLatestMsgId();
            // 这个地方设定有问题，先使用最大的时间,session的update设定存在问题
            //lastCreateTime = sessionEntity.getUpdated();
        }

        if (lastMsgId < 1 || TextUtils.isEmpty(sessionKey)) {
            return Collections.emptyList();
        }

        if (count > lastMsgId) {
            count = lastMsgId;
        }
        List<MessageEntity> msgList = doLoadHistoryMsg(
                pullTimes, peerEntity.getPeerId(),
                peerEntity.getType(),
                sessionKey, lastMsgId, lastCreateTime, count);
        return msgList;
    }

    // 根据次数有点粗暴
    public List<MessageEntity> loadHistoryMsg(MessageEntity msg, int pullTimes) {
        logger.d("IMMessageActivity#LoadHistoryMsg");
        // 在滑动的过程中请求，msgId请求下一条的
        int reqLastMsgId = msg.getMsgId() - 1;
        int loginId = IMLoginManager.getInstance().getLoginId();
        int reqLastCreateTime = msg.getCreated();
        String chatKey = msg.getSessionKey();
        int cnt = SysConstant.MSG_CNT_PER_PAGE;
        List<MessageEntity> msgList = doLoadHistoryMsg(pullTimes,
                msg.getPeerId(msg.isSend(loginId)),
                msg.getSessionType(),
                chatKey, reqLastMsgId, reqLastCreateTime, cnt);
        return msgList;
    }

    /**
     * 从DB中请求信息
     * 1. 从最近会话点击进入，拉取消息
     * 2. 在消息页面下拉刷新
     *
     * @param pullTimes
     * @param peerId
     * @param peerType
     * @param sessionKey
     * @param lastMsgId
     * @param lastCreateTime
     * @param count
     * @return
     */
    private List<MessageEntity> doLoadHistoryMsg(int pullTimes,
                                                 final int peerId,
                                                 final int peerType,
                                                 final String sessionKey,
                                                 int lastMsgId,
                                                 int lastCreateTime,
                                                 int count) {
        if (lastMsgId < 1 || TextUtils.isEmpty(sessionKey)) {
            return Collections.emptyList();
        }

        if (count > lastMsgId) {
            count = lastMsgId;
        }

        // 降序结果输出desc
        List<MessageEntity> listMsg = dbInterface.getHistoryMsg(sessionKey, lastMsgId, lastCreateTime, count);
        // asyn task refresh
        int resSize = listMsg.size();
        logger.d("LoadHistoryMsg return size is %d", resSize);
        if (resSize == 0 || pullTimes == 1 || pullTimes % 3 == 0) {
            RefreshHistoryMsgEvent historyMsgEvent = new RefreshHistoryMsgEvent();
            historyMsgEvent.setPullTimes(pullTimes);
            historyMsgEvent.setCount(count);
            historyMsgEvent.setLastMsgId(lastMsgId);
            historyMsgEvent.setListMsg(listMsg);
            historyMsgEvent.setPeerId(peerId);
            historyMsgEvent.setPeerType(peerType);
            historyMsgEvent.setSessionKey(sessionKey);
            triggerEvent(historyMsgEvent);
        }
        return listMsg;
    }

    /**
     * asyn task
     * 因为是多端同步，本地信息并不一定完整，拉取时提前异步检测
     *
     * @param hisEvent
     */
    private void doRefreshLocalMsg(RefreshHistoryMsgEvent hisEvent) {
        // check DB数据的一致性
        int lastSuccessMsgId = hisEvent.getLastMsgId();
        List<MessageEntity> listMsg = hisEvent.getListMsg();

        int resSize = listMsg.size();
        if (hisEvent.getPullTimes() > 1) {
            for (int i = resSize - 1; i >= 0; i--) {
                MessageEntity msg = listMsg.get(i);
                if (!SequenceNumberMaker.getInstance().isFailure(msg.getMsgId())) {
                    lastSuccessMsgId = msg.getMsgId();
                    break;
                }
            }
        } else {
            // 第一次拉取
            if (SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId)) {
                // 正序第一个
                for (MessageEntity entity : listMsg) {
                    if (!SequenceNumberMaker.getInstance().isFailure(entity.getMsgId())) {
                        lastSuccessMsgId = entity.getMsgId();
                        break;
                    }
                }
            }
        }

        final int refreshCnt = hisEvent.getCount() * 3;
        int peerId = hisEvent.getPeerId();
        int peerType = hisEvent.getPeerType();
        String sessionKey = hisEvent.getSessionKey();
        boolean localFailure = SequenceNumberMaker.getInstance().isFailure(lastSuccessMsgId);
        if (localFailure) {
            logger.e("LoadHistoryMsg# all msg is failure!");
            if (hisEvent.getPullTimes() == 1) {
                reqHistoryMsgNet(peerId, peerType, lastSuccessMsgId, refreshCnt);
            } else {
                // 正常
                refreshDBMsg(peerId, peerType, sessionKey, lastSuccessMsgId, refreshCnt);
            }
        }
    }

    /**
     * 历史消息直接从DB中获取。
     * 所以要保证DB数据没有问题
     *
     * @param peerId
     * @param peedType
     * @param chatKey
     * @param lastMsgId
     * @param refreshCnt
     */
    public void refreshDBMsg(int peerId, int peedType, String chatKey, int lastMsgId, int refreshCnt) {
        if (lastMsgId < 1) {
            return;
        }

        int beginMsgId = lastMsgId - refreshCnt;
        if (beginMsgId < 1) {
            beginMsgId = 1;
        }

        // 返回的结果是升序
        List<Integer> msgIdList =  dbInterface.refreshHistoryMsgId(chatKey, beginMsgId, lastMsgId);
        if (msgIdList.size() == (lastMsgId - beginMsgId + 1)) {
            logger.d("refreshDBMsg#do need refresh Message!,cause sizeOfList is right");
            return;
        }

        // 查找缺失的msgId
        List<Integer> needReqList = new ArrayList<>();
        for (int startIndex = beginMsgId, endIndex = lastMsgId; startIndex <= endIndex; startIndex++) {
            if (!msgIdList.contains(startIndex)) {
                needReqList.add(startIndex);
            }
        }

        // 请求缺失的消息
        if (needReqList.size() > 0) {
            reqMsgById(peerId, peedType, needReqList);
        }
    }

    private void reqMsgById(int peerId, int sessionType, List<Integer> msgIds) {
//        int userId = IMLoginManager.getInstance().getLoginId();
//        IMBaseDefine.SessionType sType = Java2ProtoBuf.getProtoSessionType(sessionType);
//        IMMessage.IMGetMsgByIdReq imGetMsgByIdReq = IMMessage.IMGetMsgByIdReq.newBuilder()
//                .setSessionId(peerId)
//                .setUserId(userId)
//                .setSessionType(sType)
//                .addAllMsgIdList(msgIds)
//                .build();
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_REQ_VALUE;
//        imSocketManager.sendRequest(imGetMsgByIdReq, sid, cid);
    }

    public void onReqMsgById(IMMessage.IMGetMsgByIdRsp rsp) {
        int userId = rsp.getUserId();
        int peerId = rsp.getSessionId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(rsp.getSessionType());
        String sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);

        List<IMBaseDefine.MsgInfo> msgList = rsp.getMsgListList();
        if (msgList.size() <= 0) {
            logger.i("onReqMsgById# have no msgList");
            return;
        }
        List<MessageEntity> dbEntity = new ArrayList<>();
        for (IMBaseDefine.MsgInfo msg : msgList) {
            MessageEntity entity = ProtoBuf2JavaBean.getMessageEntity(msg);
            if (entity == null) {
                logger.d("#IMMessageManager# onReqHistoryMsg#analyzeMsg is null,%s", entity);
                continue;
            }

            entity.setSessionKey(sessionKey);
            switch (sessionType) {
                case DBConstant.SESSION_TYPE_GROUP:
                    entity.setToId(peerId);
                    break;
                case DBConstant.SESSION_TYPE_SINGLE:
                    if (entity.getFromId() == userId) {
                        entity.setToId(peerId);
                    } else {
                        entity.setToId(userId);
                    }
                    break;
            }

            dbEntity.add(entity);
        }

        dbInterface.batchInsertOrUpdateMessage(dbEntity);
        // 事件驱动通知
        MessageEvent event = new MessageEvent();
        event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAIN);
        triggerEvent(event);
    }

    /**
     * 请求历史消息
     *
     * @param peerId
     * @param peerType
     * @param lastMsgId
     * @param cnt
     */
    public void reqHistoryMsgNet(int peerId, int peerType, int lastMsgId, int cnt) {
//        int loginId = IMLoginManager.getInstance().getLoginId();
//
//        IMMessage.IMGetMsgListReq req = IMMessage.IMGetMsgListReq.newBuilder()
//                .setUserId(loginId)
//                .setSessionType(Java2ProtoBuf.getProtoSessionType(peerType))
//                .setSessionId(peerId)
//                .setMsgIdBegin(lastMsgId)
//                .setMsgCnt(cnt)
//                .build();
//
//        int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
//        int cid = IMBaseDefine.MessageCmdID.CID_MSG_LIST_REQUEST_VALUE;
//        imSocketManager.sendRequest(req, sid, cid);
    }

    /**
     * 收到消息的具体信息
     * 保存在DB中
     * 通知上层，请求消息成功
     * <p>
     * 对于群而言，如果消息数目返回的数值小于请求的cnt,则表示群的消息能拉取的到头了，更早的消息没有权限拉取。
     * 如果msg_cnt 和 msg_id_begin计算得到的最早消息id与实际返回的最早消息id不一致，说明服务器消息有缺失，需要
     * 客户端做一个缺失标记，避免下次再次拉取。
     *
     * @param rsp
     */
    public void onReqHistoryMsg(IMMessage.IMGetMsgListRsp rsp) {
        // 判断loginId 判断sessionId
        int userId = rsp.getUserId();
        int sessionType = ProtoBuf2JavaBean.getJavaSessionType(rsp.getSessionType());
        int peerId = rsp.getSessionId();
        String sessionKey = EntityChangeEngine.getSessionKey(peerId, sessionType);
        int msgBegin = rsp.getMsgIdBegin();

        List<IMBaseDefine.MsgInfo> msgList = rsp.getMsgListList();

        ArrayList<MessageEntity> result = new ArrayList<>();
        for (IMBaseDefine.MsgInfo msgInfo : msgList) {
            MessageEntity messageEntity = ProtoBuf2JavaBean.getMessageEntity(msgInfo);
            if (messageEntity == null) {
                logger.d("#IMMessageManager# onReqHistoryMsg#analyzeMsg is null,%s", messageEntity);
                continue;
            }
            messageEntity.setSessionKey(sessionKey);
            switch (sessionType) {
                case DBConstant.SESSION_TYPE_GROUP: {
                    messageEntity.setToId(peerId);
                }
                break;
                case DBConstant.SESSION_TYPE_SINGLE: {
                    if (messageEntity.getFromId() == userId) {
                        messageEntity.setToId(peerId);
                    } else {
                        messageEntity.setToId(userId);
                    }
                }
                break;
            }
            result.add(messageEntity);
        }

        // 事件的通知 check
        if (result.size() > 0) {
            dbInterface.batchInsertOrUpdateMessage(result);
            MessageEvent event = new MessageEvent();
            event.setEvent(MessageEvent.Event.HISTORY_MSG_OBTAIN);
            triggerEvent(event);
        }
    }

    /**
     * 下载图片的整体迁移出来
     * @param imageEvent
     */
    private void onImageLoadSuccess(MessageEvent imageEvent) {

        ImageMessage imageMessage = (ImageMessage)imageEvent.getMessageEntity();
        logger.d("pic#onImageUploadFinish");
        String imageUrl = imageMessage.getUrl();
        logger.d("pic#imageUrl:%s", imageUrl);
        String realImageURL = "";
        try {
            realImageURL = URLDecoder.decode(imageUrl, "utf-8");
            logger.d("pic#realImageUrl:%s", realImageURL);
        } catch (UnsupportedEncodingException e) {
            logger.e(e.toString());
        }

        imageMessage.setUrl(realImageURL);
        imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
        imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
        dbInterface.insertOrUpdateMessage(imageMessage);

        // 通知Activity层成功,事件通知
        imageEvent.setEvent(MessageEvent.Event.HANDLER_IMAGE_UPLOAD_SUCCESS);
        imageEvent.setMessageEntity(imageMessage);
        triggerEvent(imageEvent);

        imageMessage.setContent(MessageConstant.IMAGE_MSG_START
                + realImageURL + MessageConstant.IMAGE_MSG_END);
        sendMessage(imageMessage);
    }


}
