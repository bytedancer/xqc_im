package com.bonade.xxp.xqc_android_im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.UploadAvatarResp;
import com.bonade.xxp.xqc_android_im.imservice.event.UserInfoEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.FriendInfoSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.QRCodeFragment;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yongchun.library.view.ImageSelectorActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.yongchun.library.view.ImageSelectorActivity.MODE_SINGLE;

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

    @BindView(R.id.ll_bottom)
    LinearLayout mBottomView;

    @BindView(R.id.fl_add_friend)
    FrameLayout mAddFriendView;

    @OnClick(R.id.iv_back)
    void backClick() {
        finish();
    }

    private int mContactId;
    private UserEntity mUserEntity;
    private RequestManager mRequestManager;
    private Transformation mTransformation;

    @OnClick(R.id.iv_avatar)
    void avatarClick() {
        if (mUserEntity.getPeerId() == IMLoginManager.getInstance().getLoginId()) {
            ImageSelectorActivity.start(this, 1, MODE_SINGLE, true, false, true);
        } else {

        }
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
        if (mUserEntity == null)
            return;
        IMContactManager.getInstance().addContact(mUserEntity);
        ChatActivity.launch(this, mUserEntity.getSessionKey());
    }

    @OnClick(R.id.ll_remark)
    void remarkFriends() {
        ViewUtil.showMessage("添加备注");
        if (mUserEntity == null)
            return;
        IMContactManager.getInstance().addContact(mUserEntity);
        ChatActivity.launch(this, IMLoginManager.getInstance().getLoginInfo().getSessionKey());
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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_friend_info;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mContactId = getIntent().getIntExtra(KEY_CONTACT_ID, 0);
        mRequestManager = Glide.with(this);
        mTransformation = new CropCircleTransformation(Glide.get(this).getBitmapPool());
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
        updateAvatarView();

        if (!TextUtils.isEmpty(userEntity.getMainName()))
            mNameView.setText(userEntity.getMainName());

        if (TextUtils.isEmpty(userEntity.getCompanyName())) {
            mCompanyView.setVisibility(View.INVISIBLE);
            mCompanyDesView.setText("企业信息");
        } else {
            mCompanyView.setVisibility(View.VISIBLE);
            mCompanyView.setText(userEntity.getCompanyName());
            mCompanyDesView.setText("企业信息(" + userEntity.getCompanyName() + ")");
        }

        mDepartmentView.setText(!TextUtils.isEmpty(userEntity.getDeptName()) ? userEntity.getDeptName() : "无");
        mJobView.setText(!TextUtils.isEmpty(userEntity.getJobName()) ? userEntity.getJobName() : "无");
        mPhoneView.setText(!TextUtils.isEmpty(userEntity.getMobile()) ? userEntity.getMobile() : "无");
        mEmailView.setText(!TextUtils.isEmpty(userEntity.getEmail()) ? userEntity.getEmail() : "无");
        updateFriendsView(userEntity);
    }

    private void updateAvatarView() {
        int loginId = IMLoginManager.getInstance().getLoginId();
        if (mUserEntity.getPeerId() == loginId) {
            mRequestManager
                    .load(CommonUtil.getUserAvatarSavePath(loginId))
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .bitmapTransform(mTransformation)
                    .into(mAvatarView);
        } else {
            mRequestManager
                    .load(mUserEntity.getAvatar())
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .into(mAvatarView);
        }
    }

    private void updateFriendsView(UserEntity userEntity) {
        int loginId = IMLoginManager.getInstance().getLoginId();
        if (loginId == userEntity.getPeerId()) {
            mBottomView.setVisibility(View.GONE);
            mRemarkView.setVisibility(View.GONE);
        } else {
            mBottomView.setVisibility(View.VISIBLE);
            boolean isFriends = userEntity.isFriend();
            mRemarkView.setVisibility(isFriends ? View.VISIBLE : View.GONE);
//            mAddFriendView.setVisibility(isFriends ? View.GONE : View.VISIBLE);
            mAddFriendView.setVisibility(View.GONE);
        }
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
    private void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    private void uploadAvatar(final String imagePath) {
        File file = new File(imagePath);
        if (!file.exists()) {
            ViewUtil.showMessage("图片不存在");
            return;
        }

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("fileUpload",
                        file.getName(),
                        RequestBody.create(MediaType.parse("multipart/form-data"), file));

        RequestBody userId =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), String.valueOf(mUserEntity.getPeerId()));

        ViewUtil.createProgressDialog(this, "");
        ApiFactory.getUserApi().uploadAvatar(userId, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UploadAvatarResp>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ViewUtil.dismissProgressDialog();
                        ViewUtil.showMessage("修改头像失败");
                    }

                    @Override
                    public void onNext(BaseResponse<UploadAvatarResp> response) {
                        ViewUtil.dismissProgressDialog();
                        if (response == null || response.getData() == null) {
                            ViewUtil.showMessage("修改头像失败");
                            return;
                        }
                        BufferedOutputStream bos = null;
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            bitmap = zoomBitmap(bitmap, CommonUtil.dip2px(FriendInfoActivity.this, 60), CommonUtil.dip2px(FriendInfoActivity.this, 60));
                            String filePath = CommonUtil.getUserAvatarSavePath(mUserEntity.getPeerId());
                            bos = new BufferedOutputStream(new FileOutputStream(filePath));
                            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos)) {
                                mUserEntity.setAvatar(response.getData().getUserLogo());
                                IMLoginManager.getInstance().setLoginInfo(mUserEntity);
                                DBInterface.getInstance().insertOrUpdateUser(mUserEntity);
                                updateAvatarView();
                                new EventBus().post(UserInfoEvent.USER_INFO_UPDATE);
                            } else {
                                ViewUtil.showMessage("图片保存失败");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                bos.flush();
                                bos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                });
    }

    private Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        if (null == bitmap) {
            return null;
        }
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) width / w);
            float scaleHeight = ((float) height / h);
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
            return newbmp;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
            case ImageSelectorActivity.REQUEST_IMAGE:
                ArrayList<String> images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
                if (images != null && !images.isEmpty()) {
                    uploadAvatar(images.get(0));
                }
                break;
        }
    }
}
