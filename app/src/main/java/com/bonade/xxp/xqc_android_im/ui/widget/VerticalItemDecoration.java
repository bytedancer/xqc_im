package com.bonade.xxp.xqc_android_im.ui.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalItemDecoration extends RecyclerView.ItemDecoration {
    private int mHeight;

    public VerticalItemDecoration(int height) {
        mHeight = height;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, mHeight);
    }

}
