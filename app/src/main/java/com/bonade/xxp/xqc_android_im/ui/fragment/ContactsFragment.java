package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.api.ContactApi;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.activity.FriendInfoActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.chat.ChatGroupSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SectionItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SideIndexBar;
import com.bonade.xxp.xqc_android_im.ui.widget.VerticalItemDecoration;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
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
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
        loadData();
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
        mRecyclerView.addItemDecoration(new VerticalItemDecoration(CommonUtil.dip2px(_mActivity, 1)));
        mAdapter = new ContactsAdapter(_mActivity, R.layout.item_contacts, new ArrayList<>());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        int userId = IMLoginManager.getInstance().getLoginId();
        int companyId = IMLoginManager.getInstance().getLoginInfo().getCompanyId();
        ApiFactory.getContactApi().getListEmployee(userId, companyId, 20, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<GetListEmployeeResp>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ViewUtil.showMessage(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseResponse<GetListEmployeeResp> response) {
                        List<UserEntity> userEntities = response.getData().getRecords();
                        mAdapter.addData(CommContact.getCommContacts());
                        mAdapter.addData(userEntities);
                    }
                });
    }

    public class ContactsAdapter extends BaseQuickAdapter<Object, BaseViewHolder> {

        private RequestManager mRequestManager;
        private Transformation mTransformation;

        public ContactsAdapter(Context context, int layoutResId, @Nullable List<Object> data) {
            super(layoutResId, data);

            mRequestManager = Glide.with(context);
            mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());

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
            String name;
            Object object;
            if (item instanceof CommContact) {
                CommContact commContact = (CommContact) item;
                name = commContact.getName();
                object = commContact.getDrawableRes();
            } else {
                UserEntity userEntity = (UserEntity) item;
                name = userEntity.getUserName();
                object = userEntity.getAvatar();
            }
            helper.setText(R.id.tv_name, name);
            mRequestManager
                    .load(object)
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .crossFade()
                    .into((ImageView) helper.getView(R.id.iv_avatar));
        }
    }

    private static class CommContact {

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

        public static List<CommContact> getCommContacts() {
            List<CommContact> commContacts = new ArrayList<>();
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "新的好友", ACTION_NEW_FRIEND));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "群聊", ACTION_GROUP_CHAT));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, "新的好友", ACTION_XQC_FRIEND));
            commContacts.add(new CommContact(R.mipmap.im_default_user_avatar, IMLoginManager.getInstance().getLoginInfo().getCompanyName(), ACTION_NONE));
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
