package com.bonade.xxp.xqc_android_im.http;

import android.util.Log;

public class Logger implements LoggingInterceptor.Logger  {

    @Override
    public void log(String message) {
        Log.i("http", message);
    }
}
