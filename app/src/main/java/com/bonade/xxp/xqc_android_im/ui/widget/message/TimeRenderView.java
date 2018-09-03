package com.bonade.xxp.xqc_android_im.ui.widget.message;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.util.DateUtil;

import java.util.Date;

public class TimeRenderView extends LinearLayout {

    TextView mTimeTitleView;

    public TimeRenderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTimeTitleView = findViewById(R.id.tv_time_title);
    }

//    public static TimeRenderView inflater(Context context) {
//        TimeRenderView timeRenderView = (TimeRenderView) LayoutInflater.from(context).inflate(R.layout.item_message_title_time, null);
//        return timeRenderView;
//    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        mTimeTitleView = findViewById(R.id.tv_time_title);
//    }

    /**
     * 与数据绑定
     * @param msgTime
     */
    public void setTime(Integer msgTime) {
        long timeStamp  = (long) msgTime;
        Date msgTimeDate = new Date(timeStamp * 1000);
        mTimeTitleView.setText(DateUtil.getTimeDiffDesc(msgTimeDate));
    }
}
