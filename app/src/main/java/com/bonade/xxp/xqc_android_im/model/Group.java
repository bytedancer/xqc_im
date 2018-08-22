package com.bonade.xxp.xqc_android_im.model;

import java.io.Serializable;

public class Group implements Serializable {

    private String name;
    private int count;

    public Group(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
