package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.FriendInfoSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.QRCodeFragment;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import butterknife.OnClick;

public class FriendInfoActivity extends BaseActivity {

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, FriendInfoActivity.class));
    }

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
        callPhone("13685536183");
    }

    @OnClick(R.id.fl_send_msg)
    void sendMsgClick() {
        ViewUtil.showMessage("发消息");
    }

    @OnClick(R.id.fl_add_friend)
    void addFriendClick() {
        ViewUtil.showMessage("加好友");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_friend_info;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {

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
