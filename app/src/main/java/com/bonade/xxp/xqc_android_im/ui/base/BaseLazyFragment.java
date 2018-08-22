package com.bonade.xxp.xqc_android_im.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

public abstract class BaseLazyFragment extends BaseFragment {

    private boolean mInited = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            if (!isHidden()) {
                mInited = true;
                setupLazyView(null);
            }
        } else {
            // isSupportHidden()仅在saveIns tanceState!=null时有意义,是库帮助记录Fragment状态的方法
            if (isSupportVisible()) {
                mInited = true;
                setupLazyView(savedInstanceState);
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!mInited && !hidden) {
            mInited = true;
            setupLazyView(mSavedInstanceState);
        }
    }

    /**
     * 懒加载
     */
    protected abstract void setupLazyView(@Nullable Bundle savedInstanceState);
}
