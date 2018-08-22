package com.bonade.xxp.xqc_android_im.ui.fragment.chat;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.CommonDialog;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ChatGroupSettingFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, ChatGroupSettingFragment.class, null);
    }

    @BindView(R.id.rv_member)
    RecyclerView mRecyclerView;

    @OnClick(R.id.tv_clear_record)
    void clearRecordClick() {
        CommonDialog dialog = new CommonDialog(_mActivity);
        dialog.setTitle("温馨提示");
        dialog.setMessage("您确定要清空聊天记录吗？");
        dialog.setLeftButtonText(R.string.common_cancel);
        dialog.setRightButtonText(R.string.common_ok);
        dialog.setClickCallbackListener(new CommonDialog.ClickCallbackListener() {
            @Override
            public void fromSure(CommonDialog dialog) {
                ViewUtil.showMessage("确定");
                dialog.dismiss();
            }

            @Override
            public void fromCancel(CommonDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_group_setting;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("聊天信息");
    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(false);
        GridLayoutManager layoutManager = new GridLayoutManager(_mActivity, 5);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new GroupMemberAdapter(R.layout.item_group_member, getDatas()));
    }

    private List<String> getDatas() {
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            datas.add(i + "");
        }
        return datas;
    }

    class GroupMemberAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public GroupMemberAdapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
            GroupMemberAdapter.this.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (position == adapter.getData().size() - 1) {
                        ViewUtil.showMessage("添加成员");
                    } else {
                        ViewUtil.showMessage(position + "");
                    }
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            TextView memberNameView = helper.getView(R.id.tv_member_name);
            if ("16".equals(item)) {
                helper.setImageResource(R.id.iv_member_avatar, R.mipmap.ic_add_member);
                memberNameView.setVisibility(View.INVISIBLE);
            } else {
                helper.setImageResource(R.id.iv_member_avatar, R.mipmap.logo);
                memberNameView.setVisibility(View.VISIBLE);
            }
        }
    }
}
