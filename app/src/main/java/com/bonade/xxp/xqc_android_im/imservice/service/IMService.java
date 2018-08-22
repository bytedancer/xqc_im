package com.bonade.xxp.xqc_android_im.imservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.ConfigurationSp;
import com.bonade.xxp.xqc_android_im.DB.sp.LoginSp;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.PriorityEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMGroupManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMHeartBeatManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMMessageManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMNotificationManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMReconnectManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMSessionManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMSocketManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMUnreadMsgManager;
import com.bonade.xxp.xqc_android_im.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * IMService 负责所有IMManager的初始化与reset
 * 并且Manager的状态的改变 也会影响到IMService的操作
 * 备注: 有些服务应该在LOGIN_OK 之后进行
 */
public class IMService extends Service {

    private Logger logger = Logger.getLogger(IMService.class);

    /**
     * binder
     */
    private IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // 所有的管理类
    private IMSocketManager socketMgr = IMSocketManager.getInstance();
    private IMLoginManager loginMgr = IMLoginManager.getInstance();
    private IMContactManager contactMgr = IMContactManager.getInstance();
    private IMGroupManager groupMgr = IMGroupManager.getInstance();
    private IMMessageManager messageMgr = IMMessageManager.getInstance();
    private IMSessionManager sessionMgr = IMSessionManager.getInstance();
    private IMReconnectManager reconnectMgr = IMReconnectManager.getInstance();
    private IMUnreadMsgManager unReadMsgMgr = IMUnreadMsgManager.getInstance();
    private IMNotificationManager notificationMgr = IMNotificationManager.getInstance();
    private IMHeartBeatManager heartBeatManager = IMHeartBeatManager.getInstance();

    private ConfigurationSp configSp;
    private LoginSp loginSp = LoginSp.getInstance();
    private DBInterface dbInterface = DBInterface.getInstance();

    @Override
    public void onCreate() {
        logger.i("IMService onCreate");
        super.onCreate();
        EventBus.getDefault().register(this);
        startForeground((int) System.currentTimeMillis(), new Notification());
    }

    @Override
    public void onDestroy() {
        logger.i("IMService onDestroy");
        // todo 在onCreate中使用startForeground
        // 在这个地方是否执行 stopForeground呐
        EventBus.getDefault().unregister(this);
        handleLoginout();
        // DB的资源的释放
        dbInterface.close();

        IMNotificationManager.getInstance().cancelAllNotifications();
        super.onDestroy();
    }

    @Subscribe(priority = SysConstant.SERVICE_EVENTBUS_PRIORITY)
    public void onEvent(PriorityEvent event) {
        if (event.getEvent() == PriorityEvent.Event.MSG_RECEIVED_MESSAGE) {
            MessageEntity entity = (MessageEntity) event.getObject();
            // 非当前会话
            logger.d("messageactivity#not this session msg -> id:%s", entity.getFromId());
            messageMgr.ackReceiveMsg(entity);
            unReadMsgMgr.add(entity);
        }
    }

    @Subscribe(priority = SysConstant.SERVICE_EVENTBUS_PRIORITY)
    public void onEvent(LoginEvent event) {
        switch (event) {
            case LOGIN_OK:
                onNormalLoginOk();
                break;
            case LOCAL_LOGIN_SUCCESS:
                onLocalLoginOk();
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
                onLocalNetOk();
                break;
            case LOGIN_OUT:
                handleLoginout();
                break;
        }
    }

    /**
     * 负责初始化  每个manager
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.i("IMService onStartCommand");
        // 应用开启初始化 下面这几个怎么释放 todo
        Context context = getApplicationContext();
        loginSp.init(context);
        // 放在这里还有些问题 todo
        socketMgr.onStartIMManager(context);
        loginMgr.onStartIMManager(context);
        contactMgr.onStartIMManager(context);
        messageMgr.onStartIMManager(context);
        groupMgr.onStartIMManager(context);
        sessionMgr.onStartIMManager(context);
        unReadMsgMgr.onStartIMManager(context);
        notificationMgr.onStartIMManager(context);
        reconnectMgr.onStartIMManager(context);
        heartBeatManager.onStartIMManager(context);
        return START_STICKY;
    }

    /**
     * 用户输入登陆流程
     * userName/pwd -> reqMessage ->connect -> loginMessage ->loginSuccess
     */
    private void onNormalLoginOk() {
        logger.d("imservice#onLogin Successful");
        //初始化其他manager todo 这个地方注意上下文的清除
        Context context = getApplicationContext();
        int loginId =  loginMgr.getLoginId();
        configSp = ConfigurationSp.getInstance(context, loginId);
        dbInterface.initDbHelp(context, loginId);

        contactMgr.onNormalLoginOk();
        sessionMgr.onNormalLoginOk();
        groupMgr.onNormalLoginOk();
        unReadMsgMgr.onNormalLoginOk();

        reconnectMgr.onNormalLoginOk();
        //依赖的状态比较特殊
        messageMgr.onLoginSuccess();
        notificationMgr.onLoginSuccess();
        heartBeatManager.onloginNetSuccess();
        // 这个时候loginManager中的localLogin 被置为true
    }

    /**
     * 自动登陆/离线登陆成功
     * autoLogin -> DB(loginInfo,loginId...) -> loginSucsess
     */
    private void onLocalLoginOk() {
        Context ctx = getApplicationContext();
        int loginId =  loginMgr.getLoginId();
        configSp = ConfigurationSp.getInstance(ctx,loginId);
        dbInterface.initDbHelp(ctx,loginId);

        contactMgr.onLocalLoginOk();
        groupMgr.onLocalLoginOk();
        sessionMgr.onLocalLoginOk();
        reconnectMgr.onLocalLoginOk();
        notificationMgr.onLoginSuccess();
        messageMgr.onLoginSuccess();
    }

    /**
     * 1.从本机加载成功之后，请求MessageService建立链接成功(loginMessageSuccess)
     * 2. 重练成功之后
     */
    private void onLocalNetOk() {
        // 为了防止直接把loginId与userName的对应直接改了,重刷一遍

        Context ctx = getApplicationContext();
        int loginId =  loginMgr.getLoginId();
        configSp = ConfigurationSp.getInstance(ctx,loginId);
        dbInterface.initDbHelp(ctx,loginId);

        contactMgr.onLocalNetOk();
        groupMgr.onLocalNetOk();
        sessionMgr.onLocalNetOk();
        unReadMsgMgr.onLocalNetOk();
        reconnectMgr.onLocalNetOk();
        heartBeatManager.onloginNetSuccess();
    }

    private void handleLoginout() {
        logger.d("imservice#handleLoginout");

        // login需要监听socket的变化,在这个地方不能释放，设计上的不合理?
        socketMgr.reset();
        loginMgr.reset();
        contactMgr.reset();
        messageMgr.reset();
        groupMgr.reset();
        sessionMgr.reset();
        unReadMsgMgr.reset();
        notificationMgr.reset();
        reconnectMgr.reset();
        heartBeatManager.reset();
        configSp = null;
        EventBus.getDefault().removeAllStickyEvents();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        logger.d("imservice#onTaskRemoved");
        // super.onTaskRemoved(rootIntent);
        this.stopSelf();
    }

    public IMLoginManager getLoginManager() {
        return loginMgr;
    }

    public IMContactManager getContactManager() {
        return contactMgr;
    }

    public IMMessageManager getMessageManager() {
        return messageMgr;
    }


    public IMGroupManager getGroupManager() {
        return groupMgr;
    }

    public IMSessionManager getSessionManager() {
        return sessionMgr;
    }

    public IMReconnectManager getReconnectManager() {
        return reconnectMgr;
    }


    public IMUnreadMsgManager getUnReadMsgManager() {
        return unReadMsgMgr;
    }

    public IMNotificationManager getNotificationManager() {
        return notificationMgr;
    }

    public DBInterface getDbInterface() {
        return dbInterface;
    }

    public ConfigurationSp getConfigSp() {
        return configSp;
    }

    public LoginSp getLoginSp() {
        return loginSp;
    }
}
