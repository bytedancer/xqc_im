package com.bonade.xxp.xqc_android_im.DB.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.imservice.event.SessionEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

/**
 * 特定用户的配置文件
 * User_userId.ini
 *
 * 1.lastUpdate 2.lastVersion 3.listMsg 需要保存嘛
 *
 * 群置顶的功能也会放在这里
 * 现在的有两种key  sessionKey 以及 DBConstant的Global
 * 多端的状态最好不放在这里。备注: 例如屏蔽的状态
 */
public class ConfigurationSp {
    private Context context;
    private int loginId;
    private String fileName;
    private SharedPreferences sharedPreferences;

    private static ConfigurationSp configurationSp = null;

    public static ConfigurationSp getInstance(Context context, int loginId) {
        if (configurationSp == null || configurationSp.loginId != loginId) {
            synchronized (ConfigurationSp.class) {
                configurationSp = new ConfigurationSp(context, loginId);
                return configurationSp;
            }
        }
        return configurationSp;
    }

    private ConfigurationSp(Context context, int loginId) {
        this.context = context;
        this.loginId = loginId;
        this.fileName = "User_" + loginId + ".ini";
        this.sharedPreferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
    }

    /**
     * 获取全部置顶的session
     * @return
     */
    public HashSet<String> getSessionTopList() {
        Set<String> topList = sharedPreferences.getStringSet(ConfigDimension.SESSIONTOP.name(), null);
        if (null == topList) {
            return null;
        }

        return (HashSet<String>) topList;
    }

    public boolean isTopSession(String sessionKey) {
        HashSet<String> list =  getSessionTopList();
        if (list != null && list.size() > 0 && list.contains(sessionKey)) {
            return true;
        }
        return false;
    }

    /**
     * 设定set的时候有个蛋疼的点
     * 参考:http://stackoverflow.com/questions/12528836/shared-preferences-only-saved-first-time
     *
     * @param sessionKey
     * @param isTop
     */
    public void setSessionTop(String sessionKey, boolean isTop) {
        if (TextUtils.isEmpty(sessionKey)) {
            return;
        }

        Set<String> topList = sharedPreferences.getStringSet(ConfigDimension.SESSIONTOP.name(), null);
        Set<String> newList = new HashSet<>();
        if (topList != null && topList.size() > 0) {
            newList.addAll(topList);
        }

        if (isTop) {
            newList.add(sessionKey);
        } else {
            if (newList.contains(sessionKey)) {
                newList.remove(sessionKey);
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(ConfigDimension.SESSIONTOP.name(), newList);
        editor.apply();
        EventBus.getDefault().post(SessionEvent.SET_SESSION_TOP);
    }

    public boolean getConfig(String key, ConfigDimension dimension) {
        boolean defaultOnOff = dimension == ConfigDimension.NOTIFICATION ? false : true;
        boolean onOff = sharedPreferences.getBoolean(dimension.name() + key, defaultOnOff);
        return onOff;
    }

    public void setConfig(String key, ConfigDimension dimension, boolean onoff) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(dimension.name() + key, onoff);
        editor.commit();
    }

    /**
     * 勿扰、声音、振动
     * 通知的方式 one session/ one message
     */
    public enum ConfigDimension {
        NOTIFICATION,
        SOUND,
        VIBRATION,

        //置顶session 设定
        SESSIONTOP,
    }


}
