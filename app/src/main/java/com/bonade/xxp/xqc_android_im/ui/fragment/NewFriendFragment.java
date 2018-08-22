package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class NewFriendFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, NewFriendFragment.class, null);
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
        activity.getSupportActionBar().setTitle("新的好友");
    }

    private void setupRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity));
        mRecyclerView.setAdapter(new NewFriendAdapter(R.layout.item_new_friend, getDatas()));
    }

    public static class NewFriendAdapter extends BaseQuickAdapter<Item, BaseViewHolder> {

        public NewFriendAdapter(int layoutResId, @Nullable List<Item> data) {
            super(layoutResId, data);

        }

        @Override
        protected void convert(BaseViewHolder helper, Item item) {
            helper.setText(R.id.tv_name, item.getName());
            TextView titleView = helper.getView(R.id.tv_title);
            TextView acceptView = helper.getView(R.id.tv_accept);
            TextView addFriendView = helper.getView(R.id.tv_add_friend);
            int position = helper.getAdapterPosition();
            if (position == 0) {
                titleView.setVisibility(View.VISIBLE);
                if (item.isRequest()) {
                    titleView.setText("好友请求");
                } else {
                    titleView.setText("为您推荐");
                }
            } else {
                Item preItem = getData().get(position - 1);
                if (preItem != null && preItem.isRequest() && !item.isRequest()) {
                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText("为您推荐");
                } else {
                    titleView.setVisibility(View.GONE);
                }
            }

            if (item.isRequest()) {
                acceptView.setVisibility(View.VISIBLE);
                addFriendView.setVisibility(View.GONE);

                acceptView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewUtil.showMessage("接受");
                    }
                });
            } else {
                acceptView.setVisibility(View.GONE);
                addFriendView.setVisibility(View.VISIBLE);

                addFriendView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewUtil.showMessage("加好友");
                    }
                });
            }
        }
    }

    private List<Item> getDatas() {
        List<Item> datas = new ArrayList<>();
        datas.add(new Item("李平", true));
        datas.add(new Item("张三", true));
        datas.add(new Item("李四", false));
        datas.add(new Item("王五", false));
        return datas;
    }

    public static class Item {

        private String name;
        private boolean isRequest;

        public Item(String name, boolean isRequest) {
            this.name = name;
            this.isRequest = isRequest;
        }

        public String getName() {
            return name;
        }

        public boolean isRequest() {
            return isRequest;
        }
    }
}
