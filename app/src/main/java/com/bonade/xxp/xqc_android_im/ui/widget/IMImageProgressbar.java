package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;

public class IMImageProgressbar extends LinearLayout {

    private ProgressBar mProgressBar;
    private TextView mLoadingView;
    private Button mRefreshView;
    private boolean mTextShow = true;

    public interface OnRefreshBtnListener {

        void onRefresh();
    }

    public IMImageProgressbar(Context context) {
        this(context, null);
    }

    public IMImageProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.view_im_image_progress, this, true);
        mProgressBar = findViewById(R.id.pb_image_loading);
        mLoadingView = findViewById(R.id.tv_loading);
        mRefreshView = findViewById(R.id.btn_refresh);

        hideProgress();
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mTextShow) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
        mRefreshView.setVisibility(View.GONE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mRefreshView.setVisibility(View.GONE);
    }

    public void showRefreshBtn() {
        mRefreshView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
    }

    public void setRefreshBtnListener(final OnRefreshBtnListener listener) {
        mRefreshView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                listener.onRefresh();
            }
        });
    }

    public void setShowText(boolean bShow) {
        mTextShow = bShow;
    }

    public void setText(String text) {
        mLoadingView.setText(text);
    }
}
