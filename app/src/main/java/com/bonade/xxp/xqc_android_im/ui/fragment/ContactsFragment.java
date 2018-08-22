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

import com.bonade.xxp.xqc_android_im.R;
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

public class ContactsFragment extends BaseFragment implements SideIndexBar.OnIndexTouchedChangedListener {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, ContactsFragment.class, null);
    }

    @BindView(R.id.rv_contacts)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_overlay)
    TextView mOverlayView;

    @BindView(R.id.side_index_bar)
    SideIndexBar mSideIndexBar;

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
        List<Person> people = Person.getAllPersons();
        people.add(0, new Person("我的", "我的"));
        people.add(1, new Person("新的好友", "新的好友"));
        people.add(2, new Person("群聊", "群聊"));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(getActivity(), people, 3), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity), 1);
        mAdapter = new ContactsAdapter(R.layout.item_contacts, people);
        mAdapter.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mSideIndexBar.setOverlayTextView(mOverlayView)
                .setOnIndexChangedListener(this);
    }

    @Override
    public void onIndexChanged(String index, int position) {
        //滚动RecyclerView到索引位置
        mAdapter.scrollToSection(index);
    }

    public class ContactsAdapter extends BaseQuickAdapter<Person, BaseViewHolder> {

        private LinearLayoutManager mLayoutManager;

        public ContactsAdapter(int layoutResId, @Nullable List<Person> data) {
            super(layoutResId, data);
            ContactsAdapter.this.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (position == 0) {
                        QRCodeFragment.launchForUser(_mActivity, new Person("李平", "liping"));
                    } else if (position == 1) {
                        NewFriendFragment.launch(_mActivity);
                    } else if (position == 2) {
                        GroupChatFragment.launch(_mActivity);
                    } else {
                        FriendInfoActivity.launch(_mActivity);
                    }
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, Person item) {
            helper.setText(R.id.tv_name, item.getName());
        }

        public void setLayoutManager(LinearLayoutManager manager){
            this.mLayoutManager = manager;
        }

        /**
         * 滚动RecyclerView到索引位置
         * @param index
         */
        public void scrollToSection(String index){
            if (mData == null || mData.isEmpty()) return;
            if (TextUtils.isEmpty(index)) return;
            int size = mData.size();
            for (int i = 0; i < size; i++) {
                if (TextUtils.equals(index.substring(0, 1), mData.get(i).getSection().substring(0, 1))){
                    if (mLayoutManager != null){
                        mLayoutManager.scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        }
    }
}
