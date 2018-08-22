package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.Group;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.activity.ScannerActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentArgs;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.QRCodeBottomDialog;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;

public class QRCodeFragment extends BaseFragment {

    public static void launchForUser(Activity from, Person person) {
        FragmentArgs args = new FragmentArgs();
        args.add(FRAGMENT_ARGS_PERSON, person);
        args.add(FRAGMENT_ARGS_TITLE, "我的二维码");
        FragmentContainerActivity.launch(from, QRCodeFragment.class, args);
    }

    public static void launchForGroup(Activity from, Group group) {
        FragmentArgs args = new FragmentArgs();
        args.add(FRAGMENT_ARGS_GROUP, group);
        args.add(FRAGMENT_ARGS_TITLE, "群二维码");
        FragmentContainerActivity.launch(from, QRCodeFragment.class, args);
    }

    private static final String FRAGMENT_ARGS_TITLE = "FRAGMENT_ARGS_TITLE";
    private static final String FRAGMENT_ARGS_PERSON = "FRAGMENT_ARGS_PERSON";
    private static final String FRAGMENT_ARGS_GROUP = "FRAGMENT_ARGS_GROUP";

    @BindView(R.id.iv_qrcode)
    ImageView mQRCodeView;

    private Person mPerson;
    private Group mGroup;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_qrcode;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        mPerson = (Person) getArguments().getSerializable(FRAGMENT_ARGS_PERSON);
        mGroup = (Group) getArguments().getSerializable(FRAGMENT_ARGS_GROUP);
        TextView descView = ButterKnife.findById(view, R.id.tv_desc);

        String name;
        String scanQRCodeDesc;
        if (mPerson != null) {
            name = mPerson.getName();
            scanQRCodeDesc = "扫一扫二维码加我为好友";
            descView.setVisibility(View.VISIBLE);
            descView.setText("安徽伯仲信息科技有限公司");
        } else {
            name = mGroup.getName() + "(" + mGroup.getCount() + ")";
            scanQRCodeDesc = "扫一扫二维码加群";
            descView.setVisibility(View.GONE);
        }
        ((TextView) ButterKnife.findById(view, R.id.tv_name)).setText(name);
        ((TextView) ButterKnife.findById(view, R.id.tv_scan_qrcode_desc)).setText(scanQRCodeDesc);
        createEnglishQRCodeWithLogo();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comm_one,menu);
        menu.findItem(R.id.action).setTitle(R.string.more);
        menu.findItem(R.id.action).setIcon(R.mipmap.ic_more_horiz_white_36dp);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action:
                new QRCodeBottomDialog(_mActivity)
                        .setOnQRCodeBottomDialogItemClickListener(new QRCodeBottomDialog.OnQRCodeBottomDialogItemClickListener() {
                            @Override
                            public void onItemClick(View view) {
                                switch (view.getId()) {
                                    case R.id.tv_save:
                                        ViewUtil.showMessage("薪起程项目中已有，这里不实现");
                                        break;
                                    case R.id.tv_scanner:
                                        ScannerActivity.launch(_mActivity);
                                        break;
                                }
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        String title = getArguments().getString(FRAGMENT_ARGS_TITLE);
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(title);
        setHasOptionsMenu(true);
    }

    private void createEnglishQRCodeWithLogo() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
                return QRCodeEncoder.syncEncodeQRCode("liping", BGAQRCodeUtil.dp2px(_mActivity, 220), Color.BLACK, Color.WHITE,
                        logoBitmap);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    mQRCodeView.setImageBitmap(bitmap);
                } else {
                    ViewUtil.showMessage("生成二维码失败");
                }
            }
        }.execute();
    }
}
