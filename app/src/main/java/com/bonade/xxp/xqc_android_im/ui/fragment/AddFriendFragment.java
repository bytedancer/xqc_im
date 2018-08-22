package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.activity.ScannerActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class AddFriendFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, AddFriendFragment.class, null);
    }

    @OnClick(R.id.rl_scan)
    void scanClick() {
        ScannerActivity.launch(_mActivity);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_add_friend;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("添加朋友");
    }
}
