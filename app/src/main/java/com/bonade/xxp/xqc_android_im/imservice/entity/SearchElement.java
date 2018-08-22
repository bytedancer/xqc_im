package com.bonade.xxp.xqc_android_im.imservice.entity;

public class SearchElement {

    public int startIndex = -1;
    public int endIndex = -1;

    @Override
    public String toString() {
        return "SearchElement [startIndex=" + startIndex + ", endIndex="
                + endIndex + "]";
    }

    public void reset() {
        startIndex = -1;
        endIndex = -1;
    }
}
