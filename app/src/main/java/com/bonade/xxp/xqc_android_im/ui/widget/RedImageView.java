package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;


/**
 * Created by xjm on 2017/11/28.
 * 实现小红点图标
 */

public class RedImageView extends android.support.v7.widget.AppCompatImageView {
    //圆形
    private static final int SHAPE_CIRCULAR = 1;
    //方形
    private static final int SHAPE_SQUARE = 2;
    //图片形状 圆形SHAPE_CIRCULAR/方形SHAPE_SQUARE
    private int shape;
    //小红点的右边距
    private int marginRight;
    //红点画笔
    private Paint paintRedPoint;
    //红点内字体画笔
    private Paint paintText;
    //红点半径
    private static float r;
    //红点半径与圆形图标半径的百分比（按百分比计算，适配所有分辨率）
    private float r_percent = 0.2f;
    //字体和红点半径的百分比
    private static float textSize_percent = 1.3f;
    //是否显示红点 View.VISIBLE 显示  | View.INVISIBLE 隐藏
    private int pointVisible = View.INVISIBLE;
    //圆点内显示的文字
    private String text;
    //是否要处理红点显示文字超过99显示...
    private boolean isDealwithText = true;
    //红点圆心位置
    private float pointX, pointY;

    public RedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public RedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RedImageView);
        r_percent = typedArray.getFloat(R.styleable.RedImageView_r_percent, 0.2f);
        marginRight = typedArray.getDimensionPixelSize(R.styleable.RedImageView_marginRight, 0);
        shape = typedArray.getInteger(R.styleable.RedImageView_shape, SHAPE_CIRCULAR);
        initPaint();
        typedArray.recycle();
    }

    public RedImageView(Context context) {
        this(context, null);
    }

    //初始化画笔
    private void initPaint() {
        paintRedPoint = new Paint();
        paintRedPoint.setColor(Color.parseColor("#FE3824"));
        paintRedPoint.setStrokeWidth(1);
        paintRedPoint.setStyle(Paint.Style.FILL);
        paintRedPoint.setAntiAlias(true);
        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setColor(Color.parseColor("#FFFFFF"));
        paintText.setStrokeWidth(1);
        paintText.setStyle(Paint.Style.FILL_AND_STROKE);
        paintText.setTextAlign(Paint.Align.CENTER);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //判断是否要显示小红点
        if (pointVisible == View.VISIBLE) {
            float width = getWidth();
            float height = getHeight();
            if (shape == SHAPE_CIRCULAR) {
                //图片为圆形的图片
                double distanceX = 0.0f;
                double distanceY = 0.0f;
                //判断该图片（View）的宽高长度，计算圆形图标中心点到小圆点的X轴和Y轴距离
                if (width > height) {
                    r = height * r_percent;
                    //图片圆心30度角的时候图标标中心点到小圆点Y轴距离刚刚好是图标高度的1/4
                    distanceX = height * Math.cos(30 * Math.PI / 180) / 2;
                    distanceY = height * Math.sin(30 * Math.PI / 180) / 2;
                } else {
                    r = width * r_percent;
                    //图片圆心30度角的时候图标标中心点到小圆点X轴距离刚刚好是图标高度的1/4
                    distanceX = width * Math.cos(60 * Math.PI / 180) / 2;
                    distanceY = width * Math.sin(60 * Math.PI / 180) / 2;
                }
                /**
                 * 画红点
                 * 圆形图标的中心点为（width / 2，height / 2）
                 * 圆形图标中心点到小圆点的X轴和Y轴距离均为distance
                 * 小红点半径为R
                 */
                pointX = (float) (distanceX + (width / 2) - marginRight);
                pointY = (height / 2) - (float) distanceY;
            } else if (shape == SHAPE_SQUARE) {
                //形状为方形
                r = width > height ? height * r_percent : width * r_percent;
                pointX = width - r - marginRight;
                pointY = r;
            }
            //设置圆点内字体大小比圆小4个像素点
            paintText.setTextSize(textSize_percent * r);
            // 画红点
            canvas.drawCircle(pointX, pointY, r, paintRedPoint);
            if (text != null) {
                //处理如果text为数字&&text超过99&&isDealwithText = true的情况显示...
                String textStr = dealwithText(text);
                //计算字体高度（非绝对高度，有可能为负数）
                float textHeight = paintText.descent() + paintText.ascent();
                //画文字，与画红点同理
                canvas.drawText(textStr, pointX, pointY - textHeight / 2, paintText);
            }
        }
    }

    //设置红点是否显示
    public void setRedPointVisible(int visible) {
        pointVisible = visible;
        invalidate();
    }

    //设置字体
    public void setText(String text) {
        this.text = text;
        if (text != null && !"0".equals(text)) {
            setRedPointVisible(View.VISIBLE);
        } else {
            setRedPointVisible(View.GONE);
        }
    }

    /**
     * 是否需要处理text为数字的时候并且大于99
     */
    public void dealwithForText99(boolean isDealwithText) {
        this.isDealwithText = isDealwithText;
    }

    public String dealwithText(String text) {
        if (isDealwithText) {
            //处理数字超过99的情况，显示 ···
            try {
                int textInt = Integer.valueOf(text);
                if (textInt > 99) {
                    text = "···";
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.i("RedImageView", "text is must int");
            }
        }
        return text;
    }
}
