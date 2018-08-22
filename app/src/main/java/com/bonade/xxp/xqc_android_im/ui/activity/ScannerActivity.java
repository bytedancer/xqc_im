package com.bonade.xxp.xqc_android_im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.yongchun.library.view.ImageSelectorActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import rx.functions.Action1;

import static com.yongchun.library.view.ImageSelectorActivity.MODE_SINGLE;

public class ScannerActivity extends BaseActivity implements QRCodeView.Delegate {

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, ScannerActivity.class));
    }

    private static final String TAG = ScannerActivity.class.getSimpleName();

    @BindView(R.id.zxing)
    ZXingView mZXingView;

    @BindView(R.id.tv_switch_light)
    TextView mSwitchLightView;

    // 判断手电筒是否打开
    private boolean isLightOn = false;

    @OnClick(R.id.tv_switch_light)
    void switchLightClick() {
        int drawableRes;
        if (isLightOn) {
            mZXingView.closeFlashlight();
            mSwitchLightView.setText(R.string.scan_qrcode_light_on);
            drawableRes = R.mipmap.ic_add_white_24dp;
        } else {
            mZXingView.openFlashlight();
            mSwitchLightView.setText(R.string.scan_qrcode_light_off);
            drawableRes = R.mipmap.ic_add_white_24dp;
        }
        Drawable drawable = getResources().getDrawable(drawableRes);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSwitchLightView.setCompoundDrawables(null, drawable, null, null);
        isLightOn = !isLightOn;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scanner;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        setupToolbar();
        mZXingView.setDelegate(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        mZXingView.showScanRect();
        mZXingView.startSpot();
    }

    @Override
    public void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comm_one, menu);
        menu.findItem(R.id.action).setTitle(R.string.scan_qrcode_photo);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action:
                new RxPermissions(this).requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Action1<Permission>() {
                            @Override
                            public void call(Permission permission) {
                                if (permission.granted) {
                                    // 用户已经同意该权限
                                    ImageSelectorActivity.start(ScannerActivity.this, 1, MODE_SINGLE, false, false, false);
                                } else if (permission.shouldShowRequestPermissionRationale) {
                                    // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                    ViewUtil.showMessage("用户拒绝了该权限");
                                } else {
                                    // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                                    ViewUtil.showMessage("权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用");
                                }
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.scan_qrcode_title);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        ViewUtil.showMessage("扫描结果为：" + result);
        vibrate();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            ArrayList<String> images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
            String imagePath = images.get(0);
            mZXingView.stopSpot();
            ViewUtil.createProgressDialog(this, "扫描中...");
            new RecognizeTask(this).execute(imagePath);
        }
    }

    private void processRecognizedResult(String result) {
        ViewUtil.dismissProgressDialog();
        if (TextUtils.isEmpty(result)) {
            new MaterialDialog.Builder(this)
                    .title("提示")
                    .content("未发现二维码")
                    .positiveText("确定")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            mZXingView.startSpot(); // 延迟0.5秒后开始识别
                        }
                    })
                    .show();
        } else {
            ViewUtil.showMessage("扫描结果为：" + result);
        }
    }

    static class RecognizeTask extends AsyncTask<String, Void, String> {

        private WeakReference<Activity> weakReferenceActivity;

        public RecognizeTask(Activity activity) {
            this.weakReferenceActivity = new WeakReference<Activity>(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            return QRCodeDecoder.syncDecodeQRCode(strings[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            ScannerActivity activity;
            if((activity = (ScannerActivity) weakReferenceActivity.get())!=null){
                activity.processRecognizedResult(result);
            }
        }
    }
}
