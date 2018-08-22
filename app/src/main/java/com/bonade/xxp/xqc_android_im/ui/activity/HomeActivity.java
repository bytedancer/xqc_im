package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.TabSelectedEvent;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.contacts.ContactsContentFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.contacts.ContactsFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.conversation.ConversationFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.discover.DiscoverFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.mine.MineFragment;
import com.bonade.xxp.xqc_android_im.ui.widget.BottomBar;
import com.bonade.xxp.xqc_android_im.ui.widget.BottomBarTab;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

public class HomeActivity extends BaseActivity {

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, HomeActivity.class));
    }

    public static final int FIRST   = 0;
    public static final int SECOND  = 1;
    public static final int THIRD   = 2;
    public static final int FOURTH  = 3;

    private static final int[] TAB_ICON_RES = {
            R.mipmap.ic_thumb_up_white_48dp,
            R.mipmap.ic_thumb_up_white_48dp,
            R.mipmap.ic_thumb_up_white_48dp,
            R.mipmap.ic_thumb_up_white_48dp
    };

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitleView;

    @BindView(R.id.bottom_bar)
    BottomBar mBottomBar;

    private String[] mTabTexts;
    private SupportFragment[] mFragments = new SupportFragment[4];

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mTabTexts = new String[]{
                getString(R.string.home_tab_conversation),
                getString(R.string.home_tab_contacts),
                getString(R.string.home_tab_discover),
                getString(R.string.home_tab_mine)};
        mToolbarTitleView.setText(mTabTexts[0]);
        setupToolbar();
        setupFragments(savedInstanceState);
        setupBottomBar();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    private void setupFragments(Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            mFragments[FIRST] = ConversationFragment.newInstance();
            mFragments[SECOND] = ContactsFragment.newInstance();
            mFragments[THIRD] = DiscoverFragment.newInstance();
            mFragments[FOURTH] = MineFragment.newInstance();

            loadMultipleRootFragment(R.id.fl_container, FIRST,
                    mFragments[FIRST],
                    mFragments[SECOND],
                    mFragments[THIRD],
                    mFragments[FOURTH]);
        } else {
            mFragments[FIRST] = findFragment(ConversationFragment.class);
            mFragments[SECOND] = findFragment(ContactsFragment.class);
            mFragments[THIRD] = findFragment(DiscoverFragment.class);
            mFragments[FOURTH] = findFragment(MineFragment.class);
        }
    }

    private void setupBottomBar() {
        for (int i = 0; i < mTabTexts.length; i++) {
            mBottomBar.addItem(new BottomBarTab(this, TAB_ICON_RES[i], mTabTexts[i]));
        }

        mBottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                mToolbarTitleView.setText(mTabTexts[position]);
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                // 这里推荐使用EventBus来实现 -> 解耦
                // 在FirstPagerFragment,FirstHomeFragment中接收, 因为是嵌套的Fragment
                // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                EventBus.getDefault().post(new TabSelectedEvent(position));
            }
        });
    }
}
