package com.bonade.xxp.xqc_android_im.ui.fragment.contacts;

import android.os.Bundle;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;

public class ContactsContentFragment extends BaseFragment {

    public static ContactsContentFragment newInstance() {
        return new ContactsContentFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {

    }
}
