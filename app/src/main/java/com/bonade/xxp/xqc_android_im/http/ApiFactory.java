package com.bonade.xxp.xqc_android_im.http;

import com.bonade.xxp.xqc_android_im.http.api.ContactApi;
import com.bonade.xxp.xqc_android_im.http.api.GroupApi;
import com.bonade.xxp.xqc_android_im.http.api.MessageApi;
import com.bonade.xxp.xqc_android_im.http.api.UserApi;

public class ApiFactory {

    protected static final Object monitor = new Object();

    protected static UserApi userApi;
    protected static ContactApi contactApi;
    protected static MessageApi messageApi;
    protected static GroupApi groupApi;

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

    public static MessageApi getMessageApi() {
        if (messageApi == null) {
            synchronized (monitor) {
                messageApi = RetrofitManager.getInstance().create(MessageApi.class);
            }
        }
        return messageApi;
    }

    public static GroupApi getGroupApi() {
        if (groupApi == null) {
            synchronized (monitor) {
                groupApi = RetrofitManager.getInstance().create(GroupApi.class);
            }
        }
        return groupApi;
    }

    public static void reset() {
        userApi = null;
        contactApi = null;
        messageApi = null;
        groupApi = null;
    }
}
