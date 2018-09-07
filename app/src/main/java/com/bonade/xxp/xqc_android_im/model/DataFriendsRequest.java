package com.bonade.xxp.xqc_android_im.model;

public class DataFriendsRequest {
    private boolean isRequest = true; // 标志位 请求/推荐
    private int id;
    private String userLogo;
    private String userName;
    private int requestStatus; // 请求状态 0 请求 1 同意 2 隐藏

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserLogo() {
        return userLogo;
    }

    public void setUserLogo(String userLogo) {
        this.userLogo = userLogo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }
}
