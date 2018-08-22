package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.bonade.xxp.xqc_android_im.DB.sp.LoginSp;
import com.bonade.xxp.xqc_android_im.DB.sp.SystemConfigSp;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.UrlConstant;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.SocketEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.util.IMUIHelper;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, LoginActivity.class));
    }

    private Logger logger = Logger.getLogger(LoginActivity.class);

    @BindView(R.id.et_user_id)
    EditText mUserIdView;

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

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }

                    handleGotLoginIdentity(loginIdentity);
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
     * @param loginIdentity
     */
    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, "登陆失败,请重试", Toast.LENGTH_SHORT).show();
                    ViewUtil.createProgressDialog(LoginActivity.this, "登录中...");
                }
                imService.getLoginManager().login(loginIdentity);
            }
        }, 500);
    }

    private void attemptLogin() {
        String userId = "1";

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "请输入用户Id", Toast.LENGTH_SHORT).show();
        }

        ViewUtil.createProgressDialog(this, "登录中...");
        if (imService != null) {
            userId = userId.trim();
            imService.getLoginManager().login(userId);
        }
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
