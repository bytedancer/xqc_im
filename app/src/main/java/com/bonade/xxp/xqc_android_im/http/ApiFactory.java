package com.bonade.xxp.xqc_android_im.http;

import com.bonade.xxp.xqc_android_im.http.api.ContactApi;
import com.bonade.xxp.xqc_android_im.http.api.UserApi;

public class ApiFactory {

    protected static final Object monitor = new Object();

    protected static UserApi userApi;
    protected static ContactApi contactApi;

    public static UserApi getUserApi() {
        if (userApi == null) {
            synchronized (monitor) {
                userApi = RetrofitManager.getInstance().create(UserApi.class);
            }
        }
        return userApi;
    }

    public static ContactApi getContactApi() {
        if (contactApi == null) {
            synchronized (monitor) {
                contactApi = RetrofitManager.getInstance().create(ContactApi.class);
            }
        }
        return contactApi;
    }

    public static void reset() {
        userApi = null;
        contactApi = null;
    }
}
