package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.ui.helper.Emoparser;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class EmoGridView extends LinearLayout {

    private Context _context;
    private ViewPager _viewPager;
    private LinearLayout _llDot;

    private OnEmoGridViewItemClick onEmoGridViewItemClick;

    private ImageView[] dots;
    /**
     * ViewPager当前页
     */
    private int currentIndex;
    /**
     * ViewPager页数
     */
    private int viewPager_size;
    /**
     * 默认一页20个item
     */
    private double pageItemCount = 20d;

    /**
     * 保存每个页面的GridView视图
     */
    private List<GridView> list_Views;

    /**
     * viewpage高度
     */

    public EmoGridView(Context cxt) {
        super(cxt);
        _context = cxt;
        initViewPage();
        initFootDots();
    }

    public EmoGridView(Context cxt, AttributeSet attrs) {
        super(cxt, attrs);
        _context = cxt;
        initViewPage();
        initFootDots();
    }

    private void initViewPage() {
        setOrientation(VERTICAL);
        _viewPager = new ViewPager(_context);
        _llDot = new LinearLayout(_context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                CommonUtil.getDefaultPannelHeight(_context));
        params.gravity = Gravity.BOTTOM;
        _viewPager.setLayoutParams(params);
        _llDot.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        _llDot.setGravity(Gravity.CENTER);
        _llDot.setOrientation(HORIZONTAL);
        addView(_viewPager);
        addView(_llDot);
    }

    // 底部的三点
    private void initFootDots() {
        viewPager_size = (int) Math.ceil(Emoparser.getInstance(_context)
                .getResIdList().length / pageItemCount);
        int mod = Emoparser.getInstance(_context).getResIdList().length
                % (SysConstant.pageSize - 1);
        if (mod == 1)
            --viewPager_size;
        if (0 < viewPager_size) {
            if (viewPager_size == 1) {
                _llDot.setVisibility(View.GONE);
            } else {
                _llDot.setVisibility(View.VISIBLE);
                for (int i = 0; i < viewPager_size; i++) {
                    ImageView image = new ImageView(_context);
                    image.setTag(i);
                    // LinearLayout.LayoutParams params = new
                    // LinearLayout.LayoutParams(
                    // 20, 20);
                    LayoutParams params = new LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT);
                    params.setMargins(5,
                            CommonUtil.getElementSzie(_context) / 2, 5,
                            CommonUtil.getElementSzie(_context) / 2);
                    image.setBackgroundResource(R.drawable.im_default_emo_dots);
                    image.setEnabled(false);
                    _llDot.addView(image, params);
                }
            }
        }
        if (1 != viewPager_size) {
            dots = new ImageView[viewPager_size];
            for (int i = 0; i < viewPager_size; i++) {
                dots[i] = (ImageView) _llDot.getChildAt(i);
                dots[i].setEnabled(true);
                dots[i].setTag(i);
            }
            currentIndex = 0;
            dots[currentIndex].setEnabled(false);
            _viewPager
                    .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                        @Override
                        public void onPageSelected(int arg0) {
                            setCurDot(arg0);
                        }

                        @Override
                        public void onPageScrolled(int arg0, float arg1,
                                                   int arg2) {

                        }

                        @Override
                        public void onPageScrollStateChanged(int arg0) {
                        }
                    });
        }
    }

    private void setCurDot(int position) {
        if (position < 0 || position > viewPager_size - 1
                || currentIndex == position) {
            return;
        }
        dots[position].setEnabled(false);
        dots[currentIndex].setEnabled(true);
        currentIndex = position;
    }

    public void setAdapter() {
        if (onEmoGridViewItemClick == null) {
            return;
        }
        list_Views = new ArrayList<>();
        for (int i = 0; i < viewPager_size; i++) {
            list_Views.add(getViewPagerItem(i));
        }
        _viewPager.setAdapter(new ViewPageAdapter(list_Views));

    }

    /**
     * 生成gridView数据
     */
    private int[] getGridViewData(int index) {
        ++index;
        int startPos = (index - 1) * (SysConstant.pageSize - 1);
        int endPos = index * (SysConstant.pageSize - 1);
        int length = 0;

        if (endPos > Emoparser.getInstance(_context).getResIdList().length) {
            endPos = Emoparser.getInstance(_context).getResIdList().length;
        }
        length = endPos - startPos + 1;
        int[] tmps = new int[length];

        int num = 0;
        for (int i = startPos; i < endPos; i++) {
            tmps[num] = Emoparser.getInstance(_context).getResIdList()[i];
            num++;
        }
        if (length > 1)
            tmps[length - 1] = R.drawable.im_default_emo_back_normal;
        return tmps;
    }

    private GridView getViewPagerItem(final int index) {
        GridView gridView = new GridView(_context);
        gridView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        gridView.setNumColumns(7);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setPadding(6, 6, 6, 0);
        gridView.setVerticalSpacing(CommonUtil.getElementSzie(_context) / 2
                + CommonUtil.getElementSzie(_context) / 3);
//        gridView.setVerticalSpacing(30);
        gridView.setBackgroundColor(Color.TRANSPARENT);
        gridView.setAdapter(new EmoGridViewAdapter(_context,
                getGridViewData(index)));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int start = index * (SysConstant.pageSize - 1);
                onEmoGridViewItemClick.onItemClick(position + start, index);
            }
        });
        return gridView;
    }

    public void setOnEmoGridViewItemClick(
            OnEmoGridViewItemClick onFaceGridViewItemClick) {
        this.onEmoGridViewItemClick = onFaceGridViewItemClick;
    }

    public interface OnEmoGridViewItemClick {
        void onItemClick(int facesPos, int viewIndex);
    }

    private static class ViewPageAdapter extends PagerAdapter {

        private List<GridView> mListViews;
        private Logger logger = Logger.getLogger(ViewPageAdapter.class);

        public ViewPageAdapter(List<GridView> mListViews) {
            this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            try {
                container.removeView(mListViews.get(position));// 删除页卡
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            try {
                container.addView(mListViews.get(position), 0);// 添加页卡
                return mListViews.get(position);
            } catch (Exception e) {
                logger.e(e.getMessage());
                return null;
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方建议这样写
        }
    }

    private class EmoGridViewAdapter extends BaseAdapter {

        private Context context;
        private int[] emoResIds;
        private Logger logger = Logger.getLogger(EmoGridViewAdapter.class);

        public EmoGridViewAdapter(Context context, int[] emoResIds) {
            this.context = context;
            this.emoResIds = emoResIds;
        }

        @Override
        public int getCount() {
            return emoResIds.length;
        }

        @Override
        public Object getItem(int position) {
            return emoResIds[position];
        }

        @Override
        public long getItemId(int position) {
            return emoResIds[position];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                GridViewHolder gridViewHolder = null;
                if (null == convertView && null != context) {
                    gridViewHolder = new GridViewHolder();
                    convertView = gridViewHolder.layoutView;
                    if (convertView != null) {
                        convertView.setTag(gridViewHolder);
                    }
                } else {
                    gridViewHolder = (GridViewHolder) convertView.getTag();
                }
                if (null == gridViewHolder || null == convertView) {
                    return null;
                }
                gridViewHolder.faceView.setImageBitmap(getBitmap(position));

                if (position == emoResIds.length - 1) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
                    params.topMargin = CommonUtil.getElementSzie(context) / 3;
                    gridViewHolder.faceView.setLayoutParams(params);
                }
                return convertView;
            } catch (Exception e) {
                logger.e(e.getMessage());
                return null;
            }
        }

        private Bitmap getBitmap(int position) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        emoResIds[position]);
            } catch (Exception e) {
                logger.e(e.getMessage());
            }
            return bitmap;
        }

        private class GridViewHolder {

            public LinearLayout layoutView;
            public ImageView faceView;

            public GridViewHolder() {
                try {
                    AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                            AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
                    layoutView = new LinearLayout(context);
                    faceView = new ImageView(context);
                    faceView.setScaleType(ImageView.ScaleType.FIT_XY);
                    layoutView.setLayoutParams(layoutParams);
                    layoutView.setOrientation(LinearLayout.VERTICAL);
                    layoutView.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            CommonUtil.getElementSzie(context),
                            CommonUtil.getElementSzie(context));
                    params.gravity = Gravity.CENTER;
                    layoutView.addView(faceView, params);
                } catch (Exception e) {
                    logger.e(e.getMessage());
                }
            }
        }
    }
}
