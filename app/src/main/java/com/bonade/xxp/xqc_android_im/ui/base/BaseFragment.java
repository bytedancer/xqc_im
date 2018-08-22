package com.bonade.xxp.xqc_android_im.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

public abstract class BaseFragment extends SupportFragment {

    protected Bundle mSavedInstanceState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getLayoutId() != 0) {
            View view  = inflater.inflate(getLayoutId(), null);
            ButterKnife.bind(this, view);
            setupViews(view, savedInstanceState);
            return view;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    protected abstract int getLayoutId();

    protected abstract void setupViews(View view, Bundle savedInstanceState);
}
