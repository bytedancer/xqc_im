package com.bonade.xxp.xqc_android_im.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;

public class CommonDialog extends Dialog implements View.OnClickListener {

    public static final String UPDATE = "update";
    protected TextView mTitle, mMsgTextView;
    protected Button mRightBtn, mLeftBtn;
    protected View mMsgLine;
    protected ViewGroup mLayoutContent;
    protected View dialog_divider_line;
    protected View dialog_bottom;
    protected ClickCallbackListener mCallbackListener;

    public CommonDialog(Context context) {
        this(context, false, R.layout.dialog_common_layout, false);
    }

    public CommonDialog(Context context, boolean hideCancel) {
        this(context, hideCancel, false);
    }

    public CommonDialog(Context context, boolean hideCancel, boolean textCanScroll) {
        this(context, hideCancel, R.layout.dialog_common_layout, textCanScroll);
    }

    public CommonDialog(Context context, boolean hideCancel, int dialog_common_layout, boolean textCanScroll) {
        super(context, R.style.CommonDialogStyle);
        View parent = LayoutInflater.from(context).inflate(dialog_common_layout, null);
        mRightBtn = (Button) parent.findViewById(R.id.tips_dialog_btn_ok);
        mRightBtn.setOnClickListener(this);
        if (hideCancel) {
            View line = parent.findViewById(R.id.tips_dialog_line);
            if (line != null) {
                line.setVisibility(View.GONE);
            }
            mRightBtn.setBackgroundResource(R.drawable.selector_dialog_single_button);
        }
        mLeftBtn = (Button) parent.findViewById(R.id.tips_dialog_btn_cancel);
        mLeftBtn.setOnClickListener(this);
        mLeftBtn.setVisibility(hideCancel ? View.GONE : View.VISIBLE);
        mTitle = (TextView) parent.findViewById(R.id.tips_dialog_title);
        mLayoutContent = (ViewGroup) parent.findViewById(R.id.layout_content);
        mMsgTextView = (TextView) parent.findViewById(R.id.tips_dialog_content);
        if (textCanScroll) {
            mMsgTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        mMsgLine = parent.findViewById(R.id.tips_dialog_content_line);
        dialog_divider_line = parent.findViewById(R.id.dialog_divider_line);
        dialog_bottom = parent.findViewById(R.id.dialog_bottom);
        setContentView(parent);
    }

    public void setCancleBtnVisiblity(int visiblity) {
        mLeftBtn.setVisibility(visiblity);

    }

//    @Override
//    protected View obtainView(Context context) {
//        View parent = LayoutInflater.from(context).inflate(R.layout.dialog_common_layout, null);
//        parent.findViewById(R.id.layout_sure).setOnClickListener(this);
//        parent.findViewById(R.id.layout_cancel).setOnClickListener(this);
//        mTitle = (TextView) parent.findViewById(R.id.title);
//        return parent;
//    }

    public void setTitle(String title) {
        if (title == null || title.isEmpty()) {
            mTitle.setVisibility(View.GONE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
        }
    }

    public void setTitle(int titleResId) {
        if (titleResId == 0) {
            mTitle.setVisibility(View.GONE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(titleResId);
        }
    }

    public void setMessage(Spanned message) {
        if (message == null) {
            mMsgTextView.setVisibility(View.GONE);
        } else {
            mMsgTextView.setVisibility(View.VISIBLE);
            mMsgTextView.setText(message);
        }
    }

    public void setMessage(String message) {
        if (message == null || message.isEmpty()) {
            mMsgTextView.setVisibility(View.GONE);
        } else {
            mMsgTextView.setVisibility(View.VISIBLE);
            mMsgTextView.setText(message);
        }
    }


    public void setMessage(int msgResId) {
        if (msgResId != 0) {
            mMsgTextView.setVisibility(View.VISIBLE);
            mMsgTextView.setText(msgResId);
        } else {
            mMsgTextView.setVisibility(View.GONE);
        }
    }

    public void setMessageGravity(int gravity) {
        mMsgTextView.setGravity(gravity);
    }

    public void setMessageLineVisibility(int visibility) {
        mMsgLine.setVisibility(visibility);
    }

    public void setLeftButtonText(int resId) {
        if (resId != 0) {
            mLeftBtn.setText(resId);
        }
    }

    public void setLeftButtonText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mLeftBtn.setText(text);
        }
    }

    public void setRightButtonText(int resId) {
        if (resId != 0) {
            mRightBtn.setText(resId);
        }
    }

    public void setRightButtonText(String text) {
        if (!TextUtils.isEmpty(text)) {
            mRightBtn.setText(text);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tips_dialog_btn_ok:
                fromSure();
                break;
            case R.id.tips_dialog_btn_cancel:
                fromCancel();
                break;
            default:
                break;
        }
    }

    protected void fromSure() {
        if (mCallbackListener != null) {
            mCallbackListener.fromSure(this);
        }
    }

    protected void fromCancel() {
        if (mCallbackListener != null) {
            mCallbackListener.fromCancel(this);
        }
    }

    public void setClickCallbackListener(ClickCallbackListener listener) {
        mCallbackListener = listener;
    }

    public interface ClickCallbackListener {

        void fromSure(CommonDialog dialog);

        void fromCancel(CommonDialog dialog);
    }

    public void setView(View view) {
        if (mLayoutContent != null) {
            mLayoutContent.removeAllViews();
            mLayoutContent.addView(view);
        }
    }

    public void requestInputMethod() {
        Window window = this.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void hideInputMethod() {
        Window window = this.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void hideDialogBottom() {
        if (dialog_divider_line != null) {
            dialog_divider_line.setVisibility(View.GONE);
        }
        if (dialog_bottom != null) {
            dialog_bottom.setVisibility(View.GONE);
        }
    }
}
