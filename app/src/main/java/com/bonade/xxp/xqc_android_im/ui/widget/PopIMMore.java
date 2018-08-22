package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.bonade.xxp.xqc_android_im.R;

public class PopIMMore extends PopupWindow implements View.OnClickListener {
    private final static String TAG = PopIMMore.class.getSimpleName();

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public PopIMMore(Context context) {
        super(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mContext = context;
        View contentView = LayoutInflater.from(context).inflate(R.layout.popup_window_im_more, null);
        setContentView(contentView);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setAnimationStyle(R.style.AnimIMMore);
        contentView.findViewById(R.id.tv_chatroom).setOnClickListener(this);
        contentView.findViewById(R.id.tv_add_friend).setOnClickListener(this);
        contentView.findViewById(R.id.tv_scanner).setOnClickListener(this);
    }

    public PopIMMore setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (null == mOnItemClickListener) return;
        mOnItemClickListener.onItemClick(v);
        dismiss();
    }

    public void show(View parentView) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        int x = (int) (16 * scale + 0.5f);
        if (isShowing()) {
            dismiss();
        } else {
            showAtLocation(parentView, Gravity.TOP | Gravity.RIGHT, x, 0);
        }
    }
}
