package com.bonade.xxp.xqc_android_im.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.model.Group;
import com.bonade.xxp.xqc_android_im.model.Person;
import com.bonade.xxp.xqc_android_im.ui.activity.ChatActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentArgs;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.widget.DividerItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SectionItemDecoration;
import com.bonade.xxp.xqc_android_im.ui.widget.SideIndexBar;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bonade.xxp.xqc_android_im.util.pinyin.PinYin;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 1. 创建群的时候，跳到聊天页面
 * 2. 新增人员的时候，返回到聊天详情页面
 */
public class ContactsSelectFragment extends BaseFragment implements SideIndexBar.OnIndexTouchedChangedListener {

    public static void launch(Activity from, String sessionKey) {
        FragmentArgs args = new FragmentArgs();
        args.add(KEY_SESSION_KEY, sessionKey);
        FragmentContainerActivity.launch(from, ContactsSelectFragment.class, args);
    }

    private static Logger logger = Logger.getLogger(ContactsSelectFragment.class);
    private static final String KEY_SESSION_KEY = "KEY_SESSION_KEY";

    @BindView(R.id.tv_select_group)
    TextView mSelectGroupView;

    @BindView(R.id.rv_contacts)
    RecyclerView mRecyclerView;

    @BindView(R.id.tv_overlay)
    TextView mOverlayView;

    @BindView(R.id.side_index_bar)
    SideIndexBar mSideIndexBar;

    private IMService mImService;
    private PeerEntity mPeerEntity;
    private String mCurrentSessionKey;
    private List<UserEntity> mAllContacts;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("groupselmgr#onIMServiceConnected");
            mImService = imServiceConnector.getIMService();
            mPeerEntity = mImService.getSessionManager().findPeerEntity(mCurrentSessionKey);
            // 已经处于选中状态的list
            Set<Integer> alreadyList = getAlreadyCheckedList();
            mSelectGroupView.setVisibility((alreadyList == null || alreadyList.size() <= 1) ? View.VISIBLE : View.GONE);
            setupContacts(alreadyList);
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    private ContactsSelectAdapter mAdapter;

    @OnClick(R.id.tv_select_group)
    void selectGroupClick() {
        GroupChatFragment.launch(_mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        imServiceConnector.connect(_mActivity);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts_select;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        mCurrentSessionKey = getArguments().getString(KEY_SESSION_KEY);
        setupToolbar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(_mActivity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comm_one, menu);
        int size = 0;
        if (mAdapter != null) {
            size = mAdapter.getCheckSet().size();
        }
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
                Set<Integer> checkSet = mAdapter.getCheckSet();
                if (checkSet.isEmpty()) {
                    ViewUtil.showMessage("请先选择群组成员");
                    return true;
                }

                if (checkSet.size() == 1) {
                    UserEntity userEntity = null;
                    for (UserEntity user : mAdapter.getAllUsers()) {
                        if (checkSet.contains(user.getPeerId())) {
                            userEntity = user;
                            break;
                        }
                    }

                    if (userEntity != null) {
                        IMContactManager.getInstance().addContact(userEntity);
                        ChatActivity.launch(_mActivity, userEntity.getSessionKey());
                        _mActivity.finish();
                    }
                    return true;
                }

                // 从个人过来的，创建群，默认自己是加入的，对方的sessionId也是加入的
                // 自己与自己对话，也能创建群的，这个时候要判断，群组成员一定要大于2个
                int sessionType = mPeerEntity.getType();
                if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
                    int loginId = mImService.getLoginManager().getLoginId();
                    logger.d("tempgroup#loginId:%d", loginId);
                    checkSet.add(loginId);
                    checkSet.add(mPeerEntity.getPeerId());
                    logger.d("tempgroup#memberList size:%d", checkSet.size());
                    ViewUtil.createProgressDialog(_mActivity, "");
                    List<UserEntity> userEntities = new ArrayList<>();
                    for (UserEntity userEntity : mAllContacts) {
                        if (checkSet.contains(userEntity.getPeerId())) {
                            userEntities.add(userEntity);
                        }
                    }
                    mImService.getGroupManager().reqCreateTempGroup(userEntities);
                } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
                    ViewUtil.createProgressDialog(_mActivity, "");
                    List<UserEntity> userEntities = new ArrayList<>();
                    for (UserEntity userEntity : mAllContacts) {
                        if (checkSet.contains(userEntity.getPeerId())) {
                            userEntities.add(userEntity);
                        }
                    }
                    mImService.getGroupManager().reqAddGroupMember(mPeerEntity.getPeerId(), userEntities);
                }
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

    @Override
    public void onIndexChanged(String index, int position) {
        //滚动RecyclerView到索引位置
        mAdapter.scrollToSection(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case CHANGE_GROUP_MEMBER_SUCCESS:
                handleGroupMemChangeSuccess(event);
                break;
            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:
                handleChangeGroupMemFail();
                break;
            case CREATE_GROUP_SUCCESS:
                handleCreateGroupSuccess(event);
                break;
            case CREATE_GROUP_FAIL:
            case CREATE_GROUP_TIMEOUT:
                handleCreateGroupFail();
                break;

        }
    }

    /**
     * 处理群创建成功事件
     *
     * @param event
     */
    private void handleCreateGroupSuccess(GroupEvent event) {
        logger.d("groupmgr#on CREATE_GROUP_OK");
        ViewUtil.dismissProgressDialog();
        String groupSessionKey = event.getGroupEntity().getSessionKey();
        ChatActivity.launch(_mActivity, groupSessionKey);
        _mActivity.finish();
    }

    /**
     * 处理群创建失败事件
     */
    private void handleCreateGroupFail() {
        logger.d("groupmgr#on CREATE_GROUP_FAIL");
        ViewUtil.dismissProgressDialog();
        ViewUtil.showMessage("请求创建失败,请检查网络");
    }

    /**
     * 处理 群成员增加删除成功事件
     * 直接返回群详情管理页面
     *
     * @param event
     */
    private void handleGroupMemChangeSuccess(GroupEvent event) {
        logger.d("groupmgr#on handleGroupMemChangeSuccess");
        ViewUtil.dismissProgressDialog();
        _mActivity.finish();
    }

    /**
     * 处理 群成员增加删除失败事件
     * 直接返回群详情管理页面
     */
    private void handleChangeGroupMemFail() {
        logger.d("groupmgr#on handleChangeGroupMemFail");
        ViewUtil.dismissProgressDialog();
        ViewUtil.showMessage("修改群失败");
    }

    private Set<Integer> getAlreadyCheckedList() {
        Set<Integer> alreadyListSet = new HashSet<>();
        if (mPeerEntity == null) {
            ViewUtil.showMessage("不合法群组选择信息");
            _mActivity.finish();
            logger.e("[fatal error,groupInfo is null,cause by SESSION_TYPE_GROUP]");
        }

        switch (mPeerEntity.getType()) {
            case DBConstant.SESSION_TYPE_GROUP:
                GroupEntity entity = (GroupEntity) mPeerEntity;
                alreadyListSet.addAll(entity.getGroupMemberIds());
                break;
            case DBConstant.SESSION_TYPE_SINGLE:
                int loginId = mImService.getLoginManager().getLoginId();
                alreadyListSet.add(loginId);
                alreadyListSet.add(mPeerEntity.getPeerId());
                break;
        }
        return alreadyListSet;
    }

    private void setupContacts(final Set<Integer> alreadyList) {
        final UserEntity loginUser = mImService.getLoginManager().getLoginInfo();
        if (mAllContacts == null)
            mAllContacts = new ArrayList<>();
        ApiFactory.getContactApi().getListEmployee(loginUser.getPeerId(), loginUser.getCompanyId(), 1, Integer.MAX_VALUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<GetListEmployeeResp>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResponse<GetListEmployeeResp> response) {
                        if (response != null
                                && response.getData() != null
                                && response.getData().getRecords() != null) {
                            mAllContacts.addAll(response.getData().getRecords());
                        }
                        ApiFactory.getContactApi().getListFriend(loginUser.getPeerId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<BaseResponse<List<UserEntity>>>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(BaseResponse<List<UserEntity>> response) {
                                        if (response != null
                                                && response.getData() != null) {
                                            mAllContacts.addAll(response.getData());
                                        }

                                        processGetAllContactsResponse(mAllContacts, alreadyList);
                                    }
                                });
                    }
                });
    }

    private void processGetAllContactsResponse(List<UserEntity> userEntities, Set<Integer> alreadyList) {
        if (userEntities.isEmpty())
            return;

        for (UserEntity userEntity : userEntities) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userEntity.getMainName(), userEntity.getPinyinElement());
        }

        List<UserEntity> list = getUsersSortedList(userEntities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(getActivity(), list, 0), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity), 1);
        mAdapter = new ContactsSelectAdapter(_mActivity, R.layout.item_contacts_select, list, alreadyList, mImService);
        mAdapter.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mSideIndexBar.setOverlayTextView(mOverlayView)
                .setOnIndexChangedListener(this);
    }

    private List<UserEntity> getUsersSortedList(List<UserEntity> userEntities) {
        // todo eric efficiency
        Collections.sort(userEntities, new Comparator<UserEntity>() {
            @Override
            public int compare(UserEntity entity1, UserEntity entity2) {
                if (entity2.getPinyinElement().pinyin.startsWith("#")) {
                    return -1;
                } else if (entity1.getPinyinElement().pinyin.startsWith("#")) {
                    // todo eric guess: latter is > 0
                    return 1;
                } else {
                    if (entity1.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity1.getMainName(), entity1.getPinyinElement());
                    }
                    if (entity2.getPinyinElement().pinyin == null) {
                        PinYin.getPinYin(entity2.getMainName(), entity2.getPinyinElement());
                    }
                    return entity1.getPinyinElement().pinyin.compareToIgnoreCase(entity2.getPinyinElement().pinyin);
                }
            }
        });
        return userEntities;
    }

    public class ContactsSelectAdapter extends BaseQuickAdapter<UserEntity, BaseViewHolder> {

        private Logger logger = Logger.getLogger(ContactsSelectAdapter.class);

        private LinearLayoutManager mLayoutManager;
        private List<UserEntity> mAllUsers;
        private List<UserEntity> mBackupUsers;
        private Set<Integer> mAlreadySet = new HashSet<>();
        private Set<Integer> mCheckSet = new HashSet<>();
        private IMService mIMService;
        private RequestManager mRequestManager;
        private Transformation mTransformation;


        public ContactsSelectAdapter(Context context, int layoutResId, @Nullable final List<UserEntity> data, Set<Integer> alreadySet, IMService service) {
            super(layoutResId, data);

            mRequestManager = Glide.with(context);
            mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());

            mAllUsers = data;
            mBackupUsers = data;
            if (alreadySet != null && !alreadySet.isEmpty()) {
                mAlreadySet.addAll(alreadySet);
            }

            ContactsSelectAdapter.this.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    UserEntity userEntity = getItem(position);
                    if (userEntity == null || mAlreadySet.contains(userEntity.getPeerId())) {
                        return;
                    }

                    int userId = userEntity.getPeerId();
                    if (mCheckSet.contains(userId)) {
                        mCheckSet.remove(userId);
                    } else {
                        mCheckSet.add(userId);
                    }
                    _mActivity.invalidateOptionsMenu();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        protected void convert(BaseViewHolder helper, UserEntity item) {
            helper.setText(R.id.tv_name, item.getMainName());

            mRequestManager
                    .load(item.getAvatar())
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .crossFade()
                    .into((ImageView) helper.getView(R.id.iv_avatar));

            boolean checked = mCheckSet.contains(item.getPeerId());
            CheckBox selectContactView = helper.getView(R.id.cb_contact);
            selectContactView.setChecked(checked);
            boolean disable = mAlreadySet.contains(item.getPeerId());
            selectContactView.setEnabled(!disable);
        }

        public Set<Integer> getCheckSet() {
            return mCheckSet;
        }

        public List<UserEntity> getAllUsers() {
            return mAllUsers;
        }

        public void setLayoutManager(LinearLayoutManager manager) {
            this.mLayoutManager = manager;
        }

        /**
         * 滚动RecyclerView到索引位置
         *
         * @param index
         */
        public void scrollToSection(String index) {
            if (mData == null || mData.isEmpty()) return;
            if (TextUtils.isEmpty(index)) return;
            int size = mData.size();
            for (int i = 0; i < size; i++) {
                if (TextUtils.equals(index.substring(0, 1), mData.get(i).getSection().substring(0, 1))) {
                    if (mLayoutManager != null) {
                        mLayoutManager.scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        }
    }
}
