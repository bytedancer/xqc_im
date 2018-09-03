package com.bonade.xxp.xqc_android_im.imservice.manager;

import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMLogin;
import com.google.protobuf.CodedInputStream;

import java.io.IOException;

/**
 * 消息分发中心，处理消息服务器返回的数据包
 * 1. decode  header与body的解析
 * 2. 分发
 */
public class IMPacketDispatcher {

    public static void loginPacketDispatcher(int commandId, CodedInputStream buffer) {
//        try {
            switch (commandId) {
//            case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_USERLOGIN_VALUE :
//                IMLogin.IMLoginRes  imLoginRes = IMLogin.IMLoginRes.parseFrom(buffer);
//                IMLoginManager.instance().onRepMsgServerLogin(imLoginRes);
//                return;

                case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_LOGINOUT_VALUE:
//                    IMLogin.IMLogoutRsp imLogoutRsp = IMLogin.IMLogoutRsp.parseFrom(buffer);
//                    IMLoginManager.getInstance().onRepLoginOut(imLogoutRsp);
                    return;

                case IMBaseDefine.LoginCmdID.CID_LOGIN_KICK_USER_VALUE:
//                    IMLogin.IMKickUser imKickUser = IMLogin.IMKickUser.parseFrom(buffer);
//                    IMLoginManager.getInstance().onKickout(imKickUser);
            }
//        } catch (IOException e) {
//        }
    }

    public static void buddyPacketDispatcher(int commandId, CodedInputStream buffer) {

    }

    public static void msgPacketDispatcher(int commandId, CodedInputStream buffer) {

    }

    public static void groupPacketDispatcher(int commandId,CodedInputStream buffer) {

    }
}
