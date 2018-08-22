package com.bonade.xxp.xqc_android_im.ui.fragment.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.CommonDialog;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import butterknife.OnClick;

public class ChatSingleSettingFragment extends BaseFragment {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, ChatSingleSettingFragment.class, null);
    }

    @OnClick(R.id.tv_clear_record)
    void clearRecordClick() {
        CommonDialog dialog = new CommonDialog(_mActivity);
        dialog.setTitle("温馨提示");
        dialog.setMessage("您确定要清空聊天记录吗？");
        dialog.setLeftButtonText(R.string.common_cancel);
        dialog.setRightButtonText(R.string.common_ok);
        dialog.setClickCallbackListener(new CommonDialog.ClickCallbackListener() {
            @Override
            public void fromSure(CommonDialog dialog) {
                ViewUtil.showMessage("确定");
                dialog.dismiss();
            }

            @Override
            public void fromCancel(CommonDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_single_setting;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("聊天信息");
    }
}
