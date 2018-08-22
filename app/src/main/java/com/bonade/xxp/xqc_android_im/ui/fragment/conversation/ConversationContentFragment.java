package com.bonade.xxp.xqc_android_im.ui.fragment.conversation;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.ui.activity.ScannerActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.AddFriendFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsSelectFragment;
import com.bonade.xxp.xqc_android_im.ui.widget.PopIMMore;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.functions.Action1;

public class ConversationContentFragment extends BaseFragment {

    public static ConversationContentFragment newInstance() {
        return new ConversationContentFragment();
    }

    @BindView(R.id.rv_conversation)
    RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setupRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_conversation,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.contacts:
                ContactsFragment.launch(_mActivity);
                return true;

            case R.id.more:
                new PopIMMore(_mActivity)
                        .setOnItemClickListener(new PopIMMore.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view) {
                                switch (view.getId()) {
                                    case R.id.tv_chatroom:
                                        ContactsSelectFragment.launch(_mActivity);
                                        break;
                                    case R.id.tv_add_friend:
                                        AddFriendFragment.launch(_mActivity);
                                        break;
                                    case R.id.tv_scanner:
                                        new RxPermissions(_mActivity).requestEach(Manifest.permission.CAMERA)
                                                .subscribe(new Action1<Permission>() {
                                                    @Override
                                                    public void call(Permission permission) {
                                                        if (permission.granted) {
                                                            // 用户已经同意该权限
                                                            ScannerActivity.launch(_mActivity);
                                                        } else if (permission.shouldShowRequestPermissionRationale) {
                                                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                                            ViewUtil.showMessage("用户拒绝了该权限");
                                                        } else {
                                                            // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                                                            ViewUtil.showMessage("权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用");
                                                        }
                                                    }
                                                });
                                        break;
                                }
                            }
                        })
                        .show(mRecyclerView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(new ConversationAdapter(R.layout.item_conversation, getData()));
    }

    private List<String> getData() {
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            datas.add("" + i);
        }
        return datas;
    }

    class ConversationAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public ConversationAdapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {

        }
    }
}
