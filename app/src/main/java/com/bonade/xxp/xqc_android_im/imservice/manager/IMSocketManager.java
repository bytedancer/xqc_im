package com.bonade.xxp.xqc_android_im.imservice.manager;

import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.callback.ListenerQueue;
import com.bonade.xxp.xqc_android_im.imservice.callback.Packetlistener;
import com.bonade.xxp.xqc_android_im.imservice.event.SocketEvent;
import com.bonade.xxp.xqc_android_im.imservice.network.MsgServerHandler;
import com.bonade.xxp.xqc_android_im.imservice.network.SocketThread;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.base.DataBuffer;
import com.bonade.xxp.xqc_android_im.protobuf.base.DefaultHeader;
import com.bonade.xxp.xqc_android_im.protobuf.base.Header;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;

import org.greenrobot.eventbus.EventBus;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;

/**
 * 业务层面:
 * 长连接建立成功之后，就要发送登陆信息，否则15s之内就会断开
 * 所以connMsg 与 login是强耦合的关系
 */
public class IMSocketManager extends IMManager {

    private Logger logger = Logger.getLogger(IMSocketManager.class);

    private static IMSocketManager instance = new IMSocketManager();
    public static IMSocketManager getInstance() {
        return instance;
    }
    private IMSocketManager() {
        logger.d("login#creating IMSocketManager");
    }

    private ListenerQueue listenerQueue = ListenerQueue.getInstance();

    // 请求消息服务器地址
//    private AsyncHttpClient client = new AsyncHttpClient();

    // 底层socket
    private SocketThread msgServerThread;

    // 快速重新连接的时候需要
    private MsgServerAddrsEntity currentMsgAddress = null;

    // 自身状态
    private SocketEvent socketStatus = SocketEvent.NONE;

    /**
     * 获取Msg地址，等待链接
     */
    @Override
    public void doOnStart() {
        socketStatus = SocketEvent.NONE;
    }

    //todo check
    @Override
    public void reset() {
        disconnectMsgServer();
        socketStatus = SocketEvent.NONE;
        currentMsgAddress = null;
    }

    /**
     * 实现自身事件驱动
     * @param socketEvent
     */
    public void triggerEvent(SocketEvent socketEvent) {
        setSocketStatus(socketEvent);
        EventBus.getDefault().postSticky(socketEvent);
    }

    public void sendRequest(GeneratedMessageLite requset, int sid, int cid) {
        sendRequest(requset,sid,cid,null);
    }

    public void sendRequest(GeneratedMessageLite requset, int sid, int cid, Packetlistener packetlistener) {
        int seqNo = 0;
        try {
            // 组装包头 header
            Header header = new DefaultHeader(sid, cid);
            int bodySize = requset.getSerializedSize();
            header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
            seqNo = header.getSeqnum();
            listenerQueue.push(seqNo, packetlistener);
            msgServerThread.sendRequest(requset, header);
        } catch (Exception e) {
            if (packetlistener != null) {
                packetlistener.onFaild();
            }
            listenerQueue.pop(seqNo);
            logger.e("#sendRequest#channel is close!");
        }
    }

    public void packetDispatch(ChannelBuffer channelBuffer) {
        DataBuffer buffer = new DataBuffer(channelBuffer);
        Header header = new Header();
        header.decode(buffer);
        // buffer 的指针位于body的地方
        int commandId = header.getCommandId();
        int serviceId = header.getServiceId();
        int seqNo = header.getSeqnum();
        logger.d("dispatch packet, serviceId:%d, commandId:%d", serviceId,
                commandId);
        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));

        Packetlistener listener = listenerQueue.pop(seqNo);
        if(listener != null){
            listener.onSuccess(codedInputStream);
            return;
        }

        // 抽象 父类执行
        switch (serviceId) {
            case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
                IMPacketDispatcher.loginPacketDispatcher(commandId,codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE:
                IMPacketDispatcher.buddyPacketDispatcher(commandId,codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_MSG_VALUE:
                IMPacketDispatcher.msgPacketDispatcher(commandId,codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_GROUP_VALUE:
                IMPacketDispatcher.groupPacketDispatcher(commandId,codedInputStream);
                break;
            default:
                break;
        }
    }

    public void reqMsgServerAddrs() {
        MsgServerAddrsEntity msgServer = new MsgServerAddrsEntity();
        msgServer.priorIP = "192.168.12.132";
        msgServer.port = 18080;
        if(msgServer == null){
            triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_FAILED);
            return;
        }
        connectMsgServer(msgServer);
        triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
    }

    /**
     * 与登陆login是强耦合的关系
     * @param currentMsgAddress
     */
    public void connectMsgServer(MsgServerAddrsEntity currentMsgAddress) {
        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);
        this.currentMsgAddress = currentMsgAddress;

        String priorIP = currentMsgAddress.priorIP;
        int port = currentMsgAddress.port;
        logger.i("login#connectMsgServer -> (%s:%d)",priorIP, port);

        // 在检查一遍，可能不重要
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
        }

        msgServerThread = new SocketThread(priorIP, port, new MsgServerHandler());
        msgServerThread.start();
    }

    /**
     * 重新与msg连接
     */
    public void reconnectMsg() {
        synchronized (IMSocketManager.class) {
            if (currentMsgAddress != null) {
                connectMsgServer(currentMsgAddress);
            } else {
                disconnectMsgServer();
                IMLoginManager.getInstance().relogin();
            }
        }
    }

    /**
     * 断开与msg连接
     */
    public void disconnectMsgServer() {
        listenerQueue.onDestory();
        logger.i("login#disconnectMsgServer");
        if (msgServerThread != null) {
            msgServerThread.close();
            msgServerThread = null;
            logger.i("login#do real disconnectMsgServer ok");
        }
    }

    /**
     * 判断链接是否处于连接状态
     * @return
     */
    public boolean isSocketConnect() {
        if (msgServerThread == null || msgServerThread.isClose()) {
            return false;
        }
        return true;
    }

    public void onMsgServerConnected() {
        logger.i("login#onMsgServerConnected");
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        IMLoginManager.getInstance().reqLoginMsgServer();
    }

    /**
     * 1. kickout 被踢出会触发这个状态    -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                    -- 重连
     * 之前的长连接状态 connected
     */
    public void onMsgServerDisconn() {
        logger.w("login#onMsgServerDisconn");
        disconnectMsgServer();
        triggerEvent(SocketEvent.MSG_SERVER_DISCONNECTED);
    }

    /**
     * 之前没有连接成功
     */
    public void onConnectMsgServerFail() {
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_FAILED);
    }

    private class MsgServerAddrsEntity {
        int code;
        String msg;
        String priorIP;
        String backupIP;
        int port;
        @Override
        public String toString() {
            return "LoginServerAddrsEntity{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", priorIP='" + priorIP + '\'' +
                    ", backupIP='" + backupIP + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }
}
