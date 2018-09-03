package com.bonade.xxp.xqc_android_im.http;

import com.bonade.xxp.xqc_android_im.http.api.UserApi;

public class ApiFactory {

    protected static final Object monitor = new Object();

    protected static UserApi userApi;

    public static UserApi getUserApi() {
        if (userApi == null) {
            synchronized (monitor) {
                userApi = RetrofitManager.getInstance().create(UserApi.class);
            }
        }
        return userApi;
    }

    public static void reset() {
        userApi = null;
    }
}
