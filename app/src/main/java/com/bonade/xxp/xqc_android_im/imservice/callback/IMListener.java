package com.bonade.xxp.xqc_android_im.imservice.callback;

public interface IMListener<T> {

    public abstract void onSuccess(T response);

    public abstract void onFaild();

    public abstract void onTimeout();
}
