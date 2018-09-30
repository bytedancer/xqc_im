package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.bonade.xxp.xqc_android_im.App;
import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.LoginSp;
import com.bonade.xxp.xqc_android_im.DB.sp.SystemConfigSp;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.UrlConstant;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.SocketEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.model.DataBindUserToken;
import com.bonade.xxp.xqc_android_im.model.DataUserInfo;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.util.IMUIHelper;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bonade.xxp.xqc_android_im.util.XqcPwdEncryptUtils;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, LoginActivity.class));
    }

    private Logger logger = Logger.getLogger(LoginActivity.class);

    @BindView(R.id.et_username)
    EditText mUsernameView;

    @BindView(R.id.et_pwd)
    EditText mPwdView;

    private IMService imService;
    private boolean autoLogin = false;
    private boolean loginSuccess = false;
    private Handler uiHandler = new Handler();

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        return;
                    }
                    IMLoginManager imLoginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (imLoginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    int loginId = loginSp.getLoginId();
                    if (loginId == 0) {
                        // 之前没有保存用户信息，跳转到登陆页面
                        break;
                    }

                    handleGotLoginIdentity(loginId);
                } while (false);

                // 异常分支都会执行这个
                handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                logger.w("loadIdentity failed");
                handleNoLoginIdentity();
            }
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    @OnClick(R.id.btn_login)
    void loginClick() {
        attemptLogin();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mUsernameView.setText("13776625960");
        mPwdView.setText("111111");
        SystemConfigSp.getInstance().init(getApplicationContext());
        // 检查SP中是否有入口地址
        if (TextUtils.isEmpty(SystemConfigSp.getInstance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.getInstance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }

        imServiceConnector.connect(LoginActivity.this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this);
    }

    private void handleNoLoginIdentity() {
        logger.i("login#handleNoLoginIdentity");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewUtil.dismissProgressDialog();
            }
        }, 1000);
    }

    /**
     * 自动登录
     * @param loginId
     */
    private void handleGotLoginIdentity(final int loginId) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, "登陆失败,请重试", Toast.LENGTH_SHORT).show();
                    ViewUtil.createProgressDialog(LoginActivity.this, "登录中...");
                }
                imService.getLoginManager().login(loginId);
            }
        }, 500);
    }

    private void attemptLogin() {
        final String username = mUsernameView.getText().toString().trim();
        String password = mPwdView.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        ViewUtil.createProgressDialog(this, "登录中...");
        imService.getLoginManager().login(6476);
//        if (imService != null) {
//            password = XqcPwdEncryptUtils.loginPwdEncrypt(password);
//
//            ApiFactory.getUserApi().login(username, password)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<DataBindUserToken>() {
//                        @Override
//                        public void onCompleted() {
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            ViewUtil.dismissProgressDialog();
//                            ViewUtil.showMessage(e.getMessage());
//                        }
//
//                        @Override
//                        public void onNext(DataBindUserToken dataBindUserToken) {
//                            String accessToken = "";
//                            if (dataBindUserToken != null && dataBindUserToken.getData() != null && !TextUtils.isEmpty(dataBindUserToken.getData().getAccess_token())) {
//                                accessToken = dataBindUserToken.getData().getAccess_token();
//                            }
//                            App.getContext().setAccount(username);
//                            ApiFactory.getUserApi().getUserInfoForAppLogin(accessToken, "false")
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe(new Observer<DataUserInfo>() {
//                                        @Override
//                                        public void onCompleted() {
//
//                                        }
//
//                                        @Override
//                                        public void onError(Throwable e) {
//                                            ViewUtil.dismissProgressDialog();
//                                            ViewUtil.showMessage(e.getMessage());
//                                        }
//
//                                        @Override
//                                        public void onNext(DataUserInfo dataUserInfo) {
//                                            int loginId = (int) dataUserInfo.getData().getUserInfo().getId();
//                                            imService.getLoginManager().login(loginId);
//                                        }
//                                    });
//                        }
//                    });
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                if (!loginSuccess)
                    onSocketFailure(event);
                break;
        }
    }

    private void onLoginSuccess() {
        ViewUtil.dismissProgressDialog();
        HomeActivity.launch(this);
        finish();
    }

    private void onLoginFailure(LoginEvent event) {
        ViewUtil.dismissProgressDialog();
//        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        ViewUtil.dismissProgressDialog();
        Toast.makeText(this, "socket连接失败", Toast.LENGTH_SHORT).show();
    }
}
