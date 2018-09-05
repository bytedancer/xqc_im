package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.activity.FriendInfoActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.chat.ChatGroupSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SectionItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SideIndexBar;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, ContactsFragment.class, null);
    }

    @BindView(R.id.rv_contacts)
    RecyclerView mRecyclerView;

    private ContactsAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts_2;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("联系人");
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ContactsAdapter(R.layout.item_contacts, new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadData() {

    }

    public class ContactsAdapter extends BaseQuickAdapter<Object, BaseViewHolder> {

        public ContactsAdapter(int layoutResId, @Nullable List<Object> data) {
            super(layoutResId, data);

            ContactsAdapter.this.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Object object = adapter.getItem(position);
                    if (object instanceof CommContact) {
                        CommContact commContact = (CommContact) object;
                        int action = commContact.getAction();
                        switch (action) {
                            case CommContact.ACTION_NEW_FRIEND:
                                break;
                            case CommContact.ACTION_GROUP_CHAT:
                                break;
                            case CommContact.ACTION_XQC_FRIEND:
                                break;
                            default:
                                break;
                        }
                    } else if (object instanceof UserEntity){
                        UserEntity userEntity = (UserEntity) object;
                        FriendInfoActivity.launch(_mActivity);
                    }
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, Object item) {
//            helper.setText(R.id.tv_name, item.getName());
        }
    }

    private class CommContact {

        public static final int ACTION_NONE           = 0;
        public static final int ACTION_NEW_FRIEND     = 1;
        public static final int ACTION_GROUP_CHAT = 2;
        public static final int ACTION_XQC_FRIEND = 3;

        private int drawableRes;
        private String name;
        private int action;

        public CommContact(int drawableRes, String name, int action) {
            this.drawableRes = drawableRes;
            this.name = name;
            this.action = action;
        }

        public List<CommContact> getCommContacts() {
            List<CommContact> commContacts = new ArrayList<>();
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "新的好友", ACTION_NEW_FRIEND));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "群聊", ACTION_GROUP_CHAT));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "新的好友", ACTION_XQC_FRIEND));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "薪起程好友", ACTION_NONE));
            return commContacts;
        }

        public int getDrawableRes() {
            return drawableRes;
        }

        public void setDrawableRes(int drawableRes) {
            this.drawableRes = drawableRes;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }
    }
}
