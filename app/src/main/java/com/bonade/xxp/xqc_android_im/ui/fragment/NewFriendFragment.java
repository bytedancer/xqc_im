package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.model.DataFriendsRequest;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NewFriendFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, NewFriendFragment.class, null);
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private NewFriendAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.comm_recycler_view;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        setupRecyclerView();
        loadData();
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
        mAdapter = new NewFriendAdapter(_mActivity, R.layout.item_new_friend, new ArrayList<DataFriendsRequest>());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        int userId = IMLoginManager.getInstance().getLoginId(); // 6476
        ApiFactory.getContactApi().getFriendsRequestList(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<DataFriendsRequest>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ViewUtil.showMessage(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseResponse<List<DataFriendsRequest>> listBaseResponse) {
                        List<DataFriendsRequest> newData = new ArrayList<>();
                        for (DataFriendsRequest item : listBaseResponse.getData()) {
                            if (item.getRequestStatus() == 0) {
                                newData.add(item);
                            }
                        }
                        mAdapter.replaceData(newData);
                    }
                });
    }

    public static class NewFriendAdapter extends BaseQuickAdapter<DataFriendsRequest, BaseViewHolder> {
        private RequestManager mRequestManager;
        private Transformation mTransformation;

        public NewFriendAdapter(Context context, int layoutResId, @Nullable List<DataFriendsRequest> data) {
            super(layoutResId, data);
            mRequestManager = Glide.with(context);
            mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());
        }

        @Override
        protected void convert(BaseViewHolder helper, final DataFriendsRequest item) {
            mRequestManager
                    .load(item.getUserLogo())
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .crossFade()
                    .into((ImageView) helper.getView(R.id.iv_avatar));
            helper.setText(R.id.tv_name, item.getUserName());
//            helper.setText(R.id.tv_desc, item.getUserName()); // 二期加上
            TextView titleView = helper.getView(R.id.tv_title);
            TextView acceptView = helper.getView(R.id.tv_accept);
            TextView rejectView = helper.getView(R.id.tv_reject_hide);
            TextView opStatusView = helper.getView(R.id.tv_status);
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
                DataFriendsRequest preItem = getData().get(position - 1);
                if (preItem != null && preItem.isRequest() && !item.isRequest()) {
                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText("为您推荐");
                } else {
                    titleView.setVisibility(View.GONE);
                }
            }

            if (item.isRequest()) {
                switch (item.getRequestStatus()) {
                    // 0 请求 1 同意 2 隐藏
                    case 0:
                        acceptView.setVisibility(View.VISIBLE);
                        rejectView.setVisibility(View.VISIBLE);
                        opStatusView.setVisibility(View.GONE);
                        break;
                    case 1:
                        acceptView.setVisibility(View.GONE);
                        rejectView.setVisibility(View.GONE);
                        opStatusView.setVisibility(View.VISIBLE);
                        opStatusView.setText("已添加");
                        break;
                    case 2:
                        acceptView.setVisibility(View.GONE);
                        rejectView.setVisibility(View.GONE);
                        opStatusView.setVisibility(View.VISIBLE);
                        opStatusView.setText("已隐藏");
                        break;
                }
                addFriendView.setVisibility(View.GONE);

                acceptView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int userId = IMLoginManager.getInstance().getLoginId();
                        ApiFactory.getContactApi().acceptFriends(userId, item.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<BaseResponse<String>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        ViewUtil.showMessage(e.getMessage());
                                    }

                                    @Override
                                    public void onNext(BaseResponse<String> stringBaseResponse) {
                                        if (stringBaseResponse.getData().equals("true")) {
                                            item.setRequestStatus(1);
                                            notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
                rejectView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int userId = IMLoginManager.getInstance().getLoginId();
                        ApiFactory.getContactApi().rejectFriends(userId, item.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<BaseResponse<String>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        ViewUtil.showMessage(e.getMessage());
                                    }

                                    @Override
                                    public void onNext(BaseResponse<String> stringBaseResponse) {
                                        if (stringBaseResponse.getData().equals("true")) {
                                            item.setRequestStatus(2);
                                            notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
            } else {
                acceptView.setVisibility(View.GONE);
                rejectView.setVisibility(View.GONE);
                opStatusView.setVisibility(View.GONE);
                addFriendView.setVisibility(View.VISIBLE);

                addFriendView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int userId = IMLoginManager.getInstance().getLoginId();
                        ApiFactory.getContactApi().addFriends(userId, item.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<BaseResponse<String>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        ViewUtil.showMessage(e.getMessage());
                                    }

                                    @Override
                                    public void onNext(BaseResponse<String> stringBaseResponse) {
                                        // todo 二期加上
                                    }
                                });
                    }
                });
            }
        }
    }
}
