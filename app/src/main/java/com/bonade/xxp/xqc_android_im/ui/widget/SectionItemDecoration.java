package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsFragment;

import java.util.List;

public class SectionItemDecoration extends RecyclerView.ItemDecoration {

    private List<Person> mData;
    private int mStartDrawIndex;
    private Paint mBgPaint;
    private TextPaint mTextPaint;
    private Rect mBounds;

    private int mSectionHeight;
    private int mBgColor;
    private int mTextColor;
    private int mTextSize;

    public SectionItemDecoration(Context context, List<Person> data, int startDrawIndex) {
        this.mData = data;
        this.mStartDrawIndex = startDrawIndex;

        mBgColor = Color.parseColor("#EDEDED");
        mSectionHeight = (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, context.getResources().getDisplayMetrics()));
        mTextSize = (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, context.getResources().getDisplayMetrics()));
        mTextColor = Color.parseColor("#999999");

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(mBgColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mBounds = new Rect();
    }

    public void setData(List<Person> data) {
        this.mData = data;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            int position = params.getViewLayoutPosition();
            if (mData != null && !mData.isEmpty() && position <= mData.size() - 1 && position > (mStartDrawIndex - 1)) {
                if (position == mStartDrawIndex) {
                    drawSection(c, left, right, child, params, position);
                } else {
                    if (null != mData.get(position).getSection()
                            && !mData.get(position).getSection().equals(mData.get(position - 1).getSection())) {
                        drawSection(c, left, right, child, params, position);
                    }
                }
            }
        }
    }

    private void drawSection(Canvas c, int left, int right, View child,
                             RecyclerView.LayoutParams params, int position) {
        c.drawRect(left,
                child.getTop() - params.topMargin - mSectionHeight,
                right,
                child.getTop() - params.topMargin, mBgPaint);
        mTextPaint.getTextBounds(mData.get(position).getSection(),
                0,
                mData.get(position).getSection().length(),
                mBounds);
        c.drawText(mData.get(position).getSection(),
                child.getPaddingLeft(),
                child.getTop() - params.topMargin - (mSectionHeight / 2 - mBounds.height() / 2),
                mTextPaint);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int pos = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
        if (pos < mStartDrawIndex) return;
        if (mData == null || mData.isEmpty()) return;
        String section = mData.get(pos).getSection();
        View child = parent.findViewHolderForLayoutPosition(pos).itemView;

        boolean flag = false;
        if ((pos + 1) < mData.size()) {
            if (null != section && !section.equals(mData.get(pos + 1).getSection())) {
                if (child.getHeight() + child.getTop() < mSectionHeight) {
                    c.save();
                    flag = true;
                    c.translate(0, child.getHeight() + child.getTop() - mSectionHeight);
                }
            }
        }
        c.drawRect(parent.getPaddingLeft(),
                parent.getPaddingTop(),
                parent.getRight() - parent.getPaddingRight(),
                parent.getPaddingTop() + mSectionHeight, mBgPaint);
        mTextPaint.getTextBounds(section, 0, section.length(), mBounds);
        c.drawText(section,
                child.getPaddingLeft(),
                parent.getPaddingTop() + mSectionHeight - (mSectionHeight / 2 - mBounds.height() / 2),
                mTextPaint);
        if (flag)
            c.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        if (mData != null && !mData.isEmpty() && position <= mData.size() - 1 && position > (mStartDrawIndex - 1)) {
            if (position == mStartDrawIndex) {
                outRect.set(0, mSectionHeight, 0, 0);
            } else {
                if (null != mData.get(position).getSection()
                        && !mData.get(position).getSection().equals(mData.get(position - 1).getSection())) {
                    outRect.set(0, mSectionHeight, 0, 0);
                }
            }
        }
    }
}
