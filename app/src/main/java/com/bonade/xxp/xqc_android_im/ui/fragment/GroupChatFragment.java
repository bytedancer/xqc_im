package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.Group;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentArgs;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class GroupChatFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, GroupChatFragment.class, null);
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.comm_recycler_view;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("群聊");
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity));
        GroupChatAdapter adapter = new GroupChatAdapter(R.layout.item_group_chat, getDatas());
        adapter.setOnItemClickListener(new GroupChatAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ViewUtil.showMessage("进去群聊界面");
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private List<Group> getDatas() {
        List<Group> groups = new ArrayList<>();
        groups.add(new Group("伯仲技术中心交流群", 41));
        groups.add(new Group("产品沟通群", 20));
        groups.add(new Group("安徽伯仲原生交流群", 8));
        return groups;
    }

    private static class GroupChatAdapter extends BaseQuickAdapter<Group, BaseViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(BaseQuickAdapter adapter, View view, int position);
        }

        private OnItemClickListener mOnItemClickListener;


        public GroupChatAdapter(int layoutResId, @Nullable final List<Group> data) {
            super(layoutResId, data);
            GroupChatAdapter.this.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(adapter, view, position);
                    }
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, Group item) {
            helper.setText(R.id.tv_name, item.getName());
        }

        private void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            mOnItemClickListener = onItemClickListener;
        }
    }
}
