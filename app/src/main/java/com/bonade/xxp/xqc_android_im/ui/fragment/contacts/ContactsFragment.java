package com.bonade.xxp.xqc_android_im.ui.fragment.contacts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseLazyFragment;

import butterknife.ButterKnife;

public class ContactsFragment extends BaseLazyFragment {

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.comm_fragment_container;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {

    }

    @Override
    protected void setupLazyView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            loadRootFragment(R.id.fl_container, ContactsContentFragment.newInstance());
        } else { // 这里可能会出现该Fragment没被初始化时,就被强杀导致的没有load子Fragment
            if (findChildFragment(ContactsContentFragment.class) == null) {
                loadRootFragment(R.id.fl_container, ContactsContentFragment.newInstance());
            }
        }
    }
}
