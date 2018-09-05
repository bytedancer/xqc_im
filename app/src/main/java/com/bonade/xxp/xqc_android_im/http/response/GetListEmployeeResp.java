package com.bonade.xxp.xqc_android_im.http.response;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;

import java.util.List;

public class GetListEmployeeResp {

    private int total;

    private int current;

    private int size;

    private List<UserEntity> records;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<UserEntity> getRecords() {
        return records;
    }

    public void setRecords(List<UserEntity> records) {
        this.records = records;
    }
}
