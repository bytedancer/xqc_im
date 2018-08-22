package com.bonade.xxp.xqc_android_im.ui.fragment.conversation;

import android.os.Bundle;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;

public class ConversationFragment extends BaseFragment {

    public static ConversationFragment newInstance() {
        return new ConversationFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.comm_fragment_container;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_container, ConversationContentFragment.newInstance());
        }
    }
}
