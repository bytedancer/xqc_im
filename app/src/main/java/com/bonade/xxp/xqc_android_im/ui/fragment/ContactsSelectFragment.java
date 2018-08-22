package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.model.Group;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SectionItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SideIndexBar;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ContactsSelectFragment extends BaseFragment implements SideIndexBar.OnIndexTouchedChangedListener {

    public static void launch(Activity from) {
        FragmentContainerActivity.launch(from, ContactsSelectFragment.class, null);
    }

    @BindView(R.id.tv_group_name)
    TextView mGroupNameView;

    @BindView(R.id.rv_contacts)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_overlay)
    TextView mOverlayView;

    @BindView(R.id.side_index_bar)
    SideIndexBar mSideIndexBar;

    @OnClick(R.id.rl_select_group)
    void selectGroupClick() {
        GroupChatFragment.launch(_mActivity);
    }

    private ContactsSelectAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts_select;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setupToolbar();
        setupRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comm_one,menu);
        int size = mAdapter.getSelectedPeople().size();
        String title;
        if (size > 0) {
            title = "完成(" + size + ")";
        } else {
            title = "完成";
        }
        menu.findItem(R.id.action).setTitle(title);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action:
                ViewUtil.showMessage("完成");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupToolbar() {
        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("选择联系人");
        setHasOptionsMenu(true);
    }

    private void setupRecyclerView() {
        List<Person> people = Person.getAllPersons();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(getActivity(), people, 0), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity), 1);
        mAdapter = new ContactsSelectAdapter(R.layout.item_contacts_select, people);
        mAdapter.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mSideIndexBar.setOverlayTextView(mOverlayView)
                .setOnIndexChangedListener(this);
    }

    @Override
    public void onIndexChanged(String index, int position) {
        //滚动RecyclerView到索引位置
        mAdapter.scrollToSection(index);
    }

    public class ContactsSelectAdapter extends BaseQuickAdapter<Person, BaseViewHolder> {

        private LinearLayoutManager mLayoutManager;
        private List<Person> mSelectedPeople;

        public ContactsSelectAdapter(int layoutResId, @Nullable final List<Person> data) {
            super(layoutResId, data);
            mSelectedPeople = new ArrayList<>();
            ContactsSelectAdapter.this.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Person person = data.get(position);
                    if (mSelectedPeople.contains(person)) {
                        mSelectedPeople.remove(person);
                    } else {
                        mSelectedPeople.add(person);
                    }
                    _mActivity.invalidateOptionsMenu();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, Person item) {
            helper.setText(R.id.tv_name, item.getName());
            helper.setImageResource(R.id.iv_select, mSelectedPeople.contains(item) ?  R.mipmap.cb_rectangle_checked : R.mipmap.cb_rectangle_default);
        }

        public void setLayoutManager(LinearLayoutManager manager){
            this.mLayoutManager = manager;
        }

        public List<Person> getSelectedPeople() {
            return mSelectedPeople;
        }

        /**
         * 滚动RecyclerView到索引位置
         * @param index
         */
        public void scrollToSection(String index){
            if (mData == null || mData.isEmpty()) return;
            if (TextUtils.isEmpty(index)) return;
            int size = mData.size();
            for (int i = 0; i < size; i++) {
                if (TextUtils.equals(index.substring(0, 1), mData.get(i).getSection().substring(0, 1))){
                    if (mLayoutManager != null){
                        mLayoutManager.scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        }
    }
}
