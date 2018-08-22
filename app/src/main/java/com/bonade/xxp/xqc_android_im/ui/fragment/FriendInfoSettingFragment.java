package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;

public class FriendInfoSettingFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, FriendInfoSettingFragment.class, null);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_friend_info_setting;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("资料设置");
    }
}
