package com.bonade.xxp.xqc_android_im.imservice.event;

/**
 * 用户是否的登陆: 依赖loginManager的状态
 * 没有: 底层socket重连
 * 有: 底层socket重连，relogin
 */
public enum ReconnectEvent {
    NONE,
    SUCCESS,
    DISABLE
}
