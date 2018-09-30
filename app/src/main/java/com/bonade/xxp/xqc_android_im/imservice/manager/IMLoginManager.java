package com.bonade.xxp.xqc_android_im.imservice.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.LoginSp;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.callback.Packetlistener;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.model.DataUserInfo;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMLogin;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.google.protobuf.CodedInputStream;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 很多情况下都是一种权衡
 * 登录控制
 */
public class IMLoginManager extends IMManager {

    private Logger logger = Logger.getLogger(IMLoginManager.class);

    private static IMLoginManager instance = new IMLoginManager();

    public static IMLoginManager getInstance() {
        return instance;
    }

    private IMLoginManager() {
        logger.d("login#creating IMLoginManager");
    }

    IMSocketManager imSocketManager = IMSocketManager.getInstance();

    // 登录参数，以便重试
    private int loginId;
    private UserEntity loginInfo;

    /**
     * loginManger 自身的状态
     * todo 状态太多就采用enum的方式
     */
    // 身份变化
    private boolean identityChanged = false;
    // 被踢
    private boolean isKickout = false;
    // PC登录
    private boolean isPcOnline = false;
    //以前是否登陆过，用户重新登陆的判断
    private boolean everLogined = false;
    //本地包含登陆信息了[可以理解为支持离线登陆了]
    private boolean isLocalLogin = false;

    private LoginEvent loginStatus = LoginEvent.NONE;


    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {
        loginId = -1;
        loginInfo = null;
        identityChanged = false;
        isKickout = false;
        isPcOnline = false;
        everLogined = false;
        loginStatus = LoginEvent.NONE;
        isLocalLogin = false;
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(LoginEvent event) {
        loginStatus = event;
        EventBus.getDefault().postSticky(event);
    }

    /**
     * 如果没有登录，什么也不做
     * 发送登出消息，重新连接不会异常反应 当重新连接重新开始工作
     * isEverLogined关闭socket
     * 主页跳到登录页
     */
    public void logOut() {
        logger.d("login#logOut");
        logger.d("login#stop reconnecting");
        everLogined = false;
        isLocalLogin = false;
        reqLoginOut();
    }

    /**
     * 登出
     */
    public void reqLoginOut() {

    }

    /**
     * 现在这种模式 req与rsp之间没有必然的耦合关系。是不是太松散了
     */
    public void onRepLoginOut() {

    }

    /**
     * 重新请求登陆 IMReconnectManager
     * 1.检测当前的状态
     * 2.请求msg server的地址
     * 3.建立连接
     * 4.验证登录信息
     */
    public void relogin() {
        imSocketManager.reqMsgServerAddrs();
    }

    /**
     * 自动登录
     */
    public void autoLogin(int loginId) {
        identityChanged = false;

        // 初始化数据库
        DBInterface.getInstance().initDbHelp(context, loginId);
        UserEntity loginEntity = DBInterface.getInstance().getByLoginId(loginId);
        do {
            if (loginEntity == null) {
                break;
            }
            this.loginInfo = loginEntity;
            this.loginId = loginEntity.getPeerId();
            // 这两个状态不要忘记掉
            isLocalLogin = true;
            everLogined = true;
            triggerEvent(LoginEvent.LOCAL_LOGIN_SUCCESS);
        } while (false);

        imSocketManager.reqMsgServerAddrs();
    }

    public void login(int userId) {
        logger.i("login#login -> userId:%s", userId);

        int loginId = LoginSp.getInstance().getLoginId();
        if (loginId == userId) {
            autoLogin(loginId);
        }

        this.loginId = userId;
        identityChanged = true;
        imSocketManager.reqMsgServerAddrs();
    }

    /**
     * 连接成功之后
     */
    public void reqLoginMsgServer() {
        logger.i("login#reqLoginMsgServer");
        triggerEvent(LoginEvent.LOGINING);
        String userId = String.valueOf(loginId);
        long timestamp = System.currentTimeMillis();

        IMLogin.IMLoginReq imLoginReq = IMLogin.IMLoginReq.newBuilder()
                .setUserId(userId)
                .setTimestamp(timestamp)
                .build();

        short flag = SysConstant.PROTOCOL_FLAG_LOGIN;
        int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
        int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_USERLOGIN_VALUE;
        imSocketManager.sendRequest(imLoginReq, flag, sid, cid, new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                try {
                    IMLogin.IMLoginRes imLoginRes = IMLogin.IMLoginRes.parseFrom((CodedInputStream) response);
                    onRepMsgServerLogin(imLoginRes);
                } catch (IOException e) {
                    triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
                    logger.e("login failed,cause by %s" + e.getCause());
                }
            }

            @Override
            public void onFaild() {
                triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
            }

            @Override
            public void onTimeout() {
                triggerEvent(LoginEvent.LOGIN_INNER_FAILED);
            }
        });
    }

    /**
     * 验证登陆信息结果
     */
    public void onRepMsgServerLogin(IMLogin.IMLoginRes loginRes) {
        logger.i("login#onRepMsgServerLogin");

        if (loginRes == null) {
            logger.e("login#decode LoginResponse failed");
            triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
            return;
        }

        ApiFactory.getUserApi().getUserInfo(loginId, loginId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseResponse<UserEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
                    }

                    @Override
                    public void onNext(BaseResponse<UserEntity> baseResponse) {
                        if (baseResponse == null || baseResponse.getData() == null) {
                            triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
                            return;
                        }
                        loginInfo = baseResponse.getData();
                        onLoginOk();
                    }
                });

//        int result = loginRes.getResult();
//        switch (result) {
//            case 1:
//                loginId = Integer.parseInt(loginRes.getUserId());
//                ApiFactory.getUserApi().getUserInfo(loginId, loginId)
//                        .subscribeOn(Schedulers.io())
//                        .subscribe(new Observer<BaseResponse<UserEntity>>() {
//                            @Override
//                            public void onCompleted() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
//                            }
//
//                            @Override
//                            public void onNext(BaseResponse<UserEntity> baseResponse) {
//                                if (baseResponse == null || baseResponse.getData() == null) {
//                                    triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
//                                    return;
//                                }
//                                loginInfo = baseResponse.getData();
//                                onLoginOk();
//                            }
//                        });
//                break;
//            case 0:
//                triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
//                break;
//            default:
//                triggerEvent(LoginEvent.LOGIN_AUTH_FAILED);
//                break;
//        }
    }

    public void onLoginOk() {
        logger.i("login#onLoginOk");
        everLogined = true;
        isKickout = false;

        // 判断登录的类型
        if (isLocalLogin) {
            triggerEvent(LoginEvent.LOCAL_LOGIN_MSG_SERVICE);
        } else {
            isLocalLogin = true;
            triggerEvent(LoginEvent.LOGIN_OK);
        }

        if (identityChanged) {
            LoginSp.getInstance().setLoginInfo(loginId);
            identityChanged = false;
        }
    }

    public int getLoginId() {
        return loginId;
    }

    public void setLoginId(int loginId) {
        logger.d("login#setLoginId -> loginId:%d", loginId);
        this.loginId = loginId;

    }

    public UserEntity getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(UserEntity loginInfo) {
        this.loginInfo = loginInfo;
    }

    public boolean isKickout() {
        return isKickout;
    }

    public void setKickout(boolean kickout) {
        isKickout = kickout;
    }

    public boolean isEverLogined() {
        return everLogined;
    }

    public void setEverLogined(boolean everLogined) {
        this.everLogined = everLogined;
    }

    public LoginEvent getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(LoginEvent loginStatus) {
        this.loginStatus = loginStatus;
    }
}
