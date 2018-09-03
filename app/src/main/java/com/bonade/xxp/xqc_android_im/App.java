package com.bonade.xxp.xqc_android_im;

import android.app.Application;
import android.content.Intent;
import android.util.DisplayMetrics;

import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.PackageUtils;

public class App extends Application {

    private Logger logger = Logger.getLogger(App.class);
    private static App mContext;

    private String mDpi;
    private int mScreenWidth;
    private int mScreenHeight;
    private String mAppVersionName;//app版本号
    private String mAccount;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.i("Application starts");
        mContext = this;

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mDpi = mScreenWidth + "x" + mScreenHeight;
        mAppVersionName = PackageUtils.getVersionName(this);

        startIMService();
    }

    private void startIMService() {
        startService(new Intent(this, IMService.class));
    }

    public static App getContext() {
        return mContext;
    }

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String mAccount) {
        this.mAccount = mAccount;
    }

    public String getAppVersionName() {
        return mAppVersionName;
    }

    public String getDpi() {
        return mDpi;
    }
}
