package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.FriendInfoSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.QRCodeFragment;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FriendInfoActivity extends BaseActivity {

    public static void launch(Context from, int contactId) {
        Intent intent = new Intent(from, FriendInfoActivity.class);
        intent.putExtra(KEY_CONTACT_ID, contactId);
        from.startActivity(intent);
    }

    private static final String KEY_CONTACT_ID = "KEY_CONTACT_ID";

    @BindView(R.id.iv_avatar)
    ImageView mAvatarView;

    @BindView(R.id.tv_name)
    TextView mNameView;

    @BindView(R.id.tv_company)
    TextView mCompanyView;

    @BindView(R.id.tv_company_des)
    TextView mCompanyDesView;

    @BindView(R.id.tv_department)
    TextView mDepartmentView;

    @BindView(R.id.tv_job)
    TextView mJobView;

    @BindView(R.id.tv_phone)
    TextView mPhoneView;

    @BindView(R.id.tv_email)
    TextView mEmailView;

    @BindView(R.id.ll_remark)
    LinearLayout mRemarkView;

    @BindView(R.id.fl_add_friend)
    FrameLayout mAddFriendView;

    @OnClick(R.id.iv_back)
    void backClick() {
        finish();
    }

    @OnClick(R.id.iv_more)
    void moreClick() {
        FriendInfoSettingFragment.launch(this);
    }

    @OnClick(R.id.iv_qrcode)
    void qrcodeClick() {
        QRCodeFragment.launchForUser(this, new Person("张三", "zhangsan"));
    }

    @OnClick(R.id.fl_call)
    void callClick() {
        String mobile = mUserEntity.getMobile();
        if (TextUtils.isEmpty(mUserEntity.getMobile())) {
            ViewUtil.showMessage("手机号码为空");
            return;
        }
        callPhone(mobile);
    }

    @OnClick(R.id.fl_send_msg)
    void sendMsgClick() {
        ChatActivity.launch(this, mUserEntity.getSessionKey());
    }

    @OnClick(R.id.ll_remark)
    void remarkFriends() {
        ViewUtil.showMessage("添加备注");
    }

    @OnClick(R.id.fl_add_friend)
    void addFriendClick() {
        int userId = IMLoginManager.getInstance().getLoginId();
        ApiFactory.getContactApi().addFriends(userId, mUserEntity.getPeerId())
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
                        // todo 对方同意后才算成功，后面需要修改
                        if (stringBaseResponse.getData().equals("true")) {
                            mUserEntity.setIsFriend(1); // 是否是好友 0不是 1是
                            updateFriendsView(mUserEntity);
                        }
                    }
                });
    }

    private int mContactId;
    private UserEntity mUserEntity;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_friend_info;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mContactId = getIntent().getIntExtra(KEY_CONTACT_ID, 0);
        loadData();
    }

    private void loadData() {
        int loginId = IMLoginManager.getInstance().getLoginId();
        ApiFactory.getUserApi().getUserInfo(loginId, mContactId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UserEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ViewUtil.showMessage(e.getMessage());
                    }

                    @Override
                    public void onNext(BaseResponse<UserEntity> response) {
                        if (response == null || response.getData() == null) {
                            return;
                        }

                        onRepUserInfo(response.getData());
                    }
                });
    }

    private void onRepUserInfo(UserEntity userEntity) {
        mUserEntity = userEntity;
        Glide.with(this)
                .load(userEntity.getAvatar())
                .error(R.mipmap.im_default_user_avatar)
                .placeholder(R.mipmap.im_default_user_avatar)
                .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                .crossFade()
                .into(mAvatarView);

        if (TextUtils.isEmpty(userEntity.getMainName()))
            mNameView.setText(userEntity.getMainName());
        if (!TextUtils.isEmpty(userEntity.getCompanyName()))
        {
            mCompanyView.setText(userEntity.getCompanyName());
            mCompanyDesView.setText(userEntity.getCompanyName());
        }
        if (!TextUtils.isEmpty(userEntity.getDeptName()))
            mDepartmentView.setText(userEntity.getDeptName());
        if (!TextUtils.isEmpty(userEntity.getJobName()))
            mJobView.setText(userEntity.getJobName());
        if (!TextUtils.isEmpty(userEntity.getMobile()))
            mPhoneView.setText(userEntity.getMobile());
        if (!TextUtils.isEmpty(userEntity.getEmail()))
            mEmailView.setText(userEntity.getEmail());

        updateFriendsView(userEntity);
    }

    private void updateFriendsView(UserEntity userEntity) {
        boolean isFriends = userEntity.isFriend();
        mRemarkView.setVisibility(isFriends ? View.VISIBLE : View.GONE);
        mAddFriendView.setVisibility(isFriends ? View.GONE : View.VISIBLE);
    }

    /**
     * 拨打电话（直接拨打电话）
     * @param phoneNum 电话号码
     */
//    public void callPhone(String phoneNum){
//        Intent intent = new Intent(Intent.ACTION_CALL);
//        Uri data = Uri.parse("tel:" + phoneNum);
//        intent.setData(data);
//        startActivity(intent);
//    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }
}
