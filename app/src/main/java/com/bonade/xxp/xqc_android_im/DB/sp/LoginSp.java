package com.bonade.xxp.xqc_android_im.DB.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.google.gson.Gson;

public class LoginSp {

    private final String fileName = "login.ini";
    private final String KEY_LOGIN_ID = "loginId";

    SharedPreferences sharedPreferences;

    private static LoginSp loginSp = null;
    public static LoginSp getInstance(){
        if (loginSp == null) {
            synchronized (LoginSp.class){
                loginSp = new LoginSp();
            }
        }
        return loginSp;
    }
    private LoginSp(){}

    public void init(Context context){
        sharedPreferences = context.getSharedPreferences
                (fileName, context.MODE_PRIVATE);
    }

    public void setLoginInfo(int loginId){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LOGIN_ID, loginId);
        editor.commit();
    }

    public int getLoginId(){
        return sharedPreferences.getInt(KEY_LOGIN_ID,0);
    }
}
