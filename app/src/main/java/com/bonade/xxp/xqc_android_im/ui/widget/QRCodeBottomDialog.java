package com.bonade.xxp.xqc_android_im.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bonade.xxp.xqc_android_im.R;

public class QRCodeBottomDialog extends Dialog {

    public interface OnQRCodeBottomDialogItemClickListener {

        void onItemClick(View view);
    }

    private OnQRCodeBottomDialogItemClickListener mOnQRCodeBottomDialogItemClickListener;

    public QRCodeBottomDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Window win = this.getWindow();
        win.requestFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_qrcode_more, null);
        view.findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null != mOnQRCodeBottomDialogItemClickListener) {
                    mOnQRCodeBottomDialogItemClickListener.onItemClick(v);
                }
            }
        });

        view.findViewById(R.id.tv_scanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (null != mOnQRCodeBottomDialogItemClickListener) {
                    mOnQRCodeBottomDialogItemClickListener.onItemClick(v);
                }
            }
        });
        setContentView(view);

        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.windowAnimations = R.style.QRCodeMore;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
        win.setBackgroundDrawableResource(android.R.color.transparent);
    }

    public QRCodeBottomDialog setOnQRCodeBottomDialogItemClickListener(
            OnQRCodeBottomDialogItemClickListener onQRCodeBottomDialogItemClickListener) {
        mOnQRCodeBottomDialogItemClickListener = onQRCodeBottomDialogItemClickListener;
        return this;
    }


}
