package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMGroupManager;
import com.bonade.xxp.xqc_android_im.model.Group;
import com.bonade.xxp.xqc_android_im.ui.activity.ChatActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentArgs;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.groupimageview.NineGridImageView;
import com.bonade.xxp.xqc_android_im.ui.widget.groupimageview.NineGridImageViewAdapter;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
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
        List<GroupEntity> groupEntities = IMGroupManager.getInstance().getNormalGroupList();
        mRecyclerView.setAdapter(new GroupChatAdapter(_mActivity, R.layout.item_group_chat, groupEntities));
    }

    private static class GroupChatAdapter extends BaseQuickAdapter<GroupEntity, BaseViewHolder> {

        private RequestManager mRequestManager;

        public GroupChatAdapter(final Context context, int layoutResId, @Nullable final List<GroupEntity> data) {
            super(layoutResId, data);
            mRequestManager = Glide.with(context);
            GroupChatAdapter.this.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    ChatActivity.launch(context, getItem(position).getSessionKey());
                    ((Activity) context).finish();
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, GroupEntity item) {
            helper.setText(R.id.tv_group_name, item.getMainName());

            List<String> avatarList = new ArrayList<>();
            for (Integer userId : item.getGroupMemberIds()) {
                UserEntity entity = IMContactManager.getInstance().findContact(userId);

                if (entity != null) {
                    avatarList.add(entity.getAvatar());
                }
                if (avatarList.size() >= 9) {
                    break;
                }
            }

            NineGridImageViewAdapter<String> adapter = new NineGridImageViewAdapter<String>() {
                @Override
                protected void onDisplayImage(Context context, ImageView imageView, String url) {
                    mRequestManager
                            .load(url)
                            .error(R.mipmap.im_default_user_avatar)
                            .placeholder(R.mipmap.im_default_user_avatar)
                            .into(imageView);
                }

                @Override
                protected ImageView generateImageView(Context context) {
                    return super.generateImageView(context);
                }
            };

            NineGridImageView avatarView = helper.getView(R.id.iv_group_avatar);
            avatarView.setAdapter(adapter);
            avatarView.setImagesData(avatarList);

            helper.setText(R.id.tv_group_count, "(" + item.getUserCount() + ")");
        }

    }
}
