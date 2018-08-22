package com.bonade.xxp.xqc_android_im;

import android.app.Application;
import android.content.Intent;

import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.util.Logger;

public class App extends Application {

    private Logger logger = Logger.getLogger(App.class);
    private static Application mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.i("Application starts");
        mContext = this;
        startIMService();
    }

    private void startIMService() {
        startService(new Intent(this, IMService.class));
    }

    public static Application getContext() {
        return mContext;
    }
}
