package com.bonade.xxp.xqc_android_im.ui.fragment.conversation;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.RecentInfo;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.ReconnectEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.SessionEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.SocketEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.UnreadEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.UserInfoEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMUnreadMsgManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.ui.activity.ChatActivity;
import com.bonade.xxp.xqc_android_im.ui.activity.HomeActivity;
import com.bonade.xxp.xqc_android_im.ui.activity.ScannerActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.AddFriendFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsFragment;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsSelectFragment;
import com.bonade.xxp.xqc_android_im.ui.widget.FloatMenu;
import com.bonade.xxp.xqc_android_im.ui.widget.PopIMMore;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.DateUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.NetworkUtil;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.functions.Action1;

public class ConversationContentFragment extends BaseFragment {

    public static ConversationContentFragment newInstance() {
        return new ConversationContentFragment();
    }

    private Logger logger = Logger.getLogger(ConversationContentFragment.class);

    @BindView(R.id.ll_no_network)
    LinearLayout mNoNetworkView;

    @BindView(R.id.iv_notify)
    ImageView mNotifyView;

    @BindView(R.id.tv_disconnect)
    TextView mDisconnectView;

    @BindView(R.id.ll_no_conversation)
    LinearLayout mNoConversationView;

    @BindView(R.id.rv_conversation)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_loading)
    ProgressBar mReconnectLoadingView;

    private IMService imService;
    private ConversationAdapter mConversationAdapter;

    //是否是手动点击重练。fasle:不显示各种弹出小气泡. true:显示小气泡直到错误出现
    private volatile boolean isManualMConnect = false;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chatfragment#recent#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }

            // 依赖联系人回话、未读消息、用户的信息三者的状态
            onRecentContactDataReady();
            EventBus.getDefault().register(ConversationContentFragment.this);

        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(ConversationContentFragment.this)) {
                EventBus.getDefault().unregister(ConversationContentFragment.this);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {
        imServiceConnector.connect(_mActivity);
        setHasOptionsMenu(true);
        setupConversationRecyclerView();
        mReconnectLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_conversation, menu);
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

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(ConversationContentFragment.this)) {
            EventBus.getDefault().unregister(ConversationContentFragment.class);
        }
        imServiceConnector.disconnect(_mActivity);
        super.onDestroy();
    }

    private void setupConversationRecyclerView() {
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(_mActivity, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mConversationAdapter = new ConversationAdapter(_mActivity, new ArrayList<RecentInfo>()));
        mConversationAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                RecentInfo recentInfo = mConversationAdapter.getItem(position);
                if (recentInfo == null) {
                    logger.e("recent#null recentInfo -> position:%d", position);
                    return;
                }
                ChatActivity.launch(_mActivity, recentInfo.getSessionKey());
            }
        });

        mConversationAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                final RecentInfo recentInfo = mConversationAdapter.getItem(position);
                if (recentInfo == null) {
                    logger.e("recent#null recentInfo -> position:%d", position);
                    return false;
                }

                final boolean isTop = imService.getConfigSp().isTopSession(recentInfo.getSessionKey());
                String topMessage = isTop ? getString(R.string.cancel_top_message) : getString(R.string.top_message);

                FloatMenu floatMenu = new FloatMenu(_mActivity, view);
                floatMenu.items(topMessage, getString(R.string.delete_session));
                floatMenu.setOnItemClickListener(new FloatMenu.OnItemClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        switch (position) {
                            case 0:
                                imService.getConfigSp().setSessionTop(recentInfo.getSessionKey(),!isTop);
                                break;
                            case 1:
                                imService.getSessionManager().reqRemoveSession(recentInfo);
                                break;
                        }
                    }
                });
                floatMenu.show();
                return true;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SessionEvent sessionEvent) {
        logger.d("conversationContentFragment#SessionEvent# -> %s", sessionEvent);
        switch (sessionEvent) {
            case RECENT_SESSION_LIST_UPDATE:
            case RECENT_SESSION_LIST_SUCCESS:
            case SET_SESSION_TOP:
                onRecentContactDataReady();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupEvent event) {
        switch (event.getEvent()) {
            case GROUP_INFO_SUCCESS:
            case CHANGE_GROUP_MEMBER_SUCCESS:
                onRecentContactDataReady();
//                searchDataReady();
                break;

            case GROUP_INFO_UPDATED:
                onRecentContactDataReady();
//                searchDataReady();
                break;
            case SHIELD_GROUP_SUCCESS:
                // 更新最下栏的未读计数、更新session
                onShieldSuccess(event.getGroupEntity());
                break;
            case SHIELD_GROUP_FAIL:
            case SHIELD_GROUP_TIMEOUT:
                onShieldFail();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UnreadEvent event) {
        switch (event.getEvent()) {
            case UNREAD_MSG_RECEIVED:
            case UNREAD_MSG_LIST_OK:
            case SESSION_READED_UNREAD_MSG:
                onRecentContactDataReady();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
            case USER_INFO_OK:
                onRecentContactDataReady();
//                searchDataReady();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginEvent loginEvent) {
        logger.d("conversationContentFragment#LoginEvent# -> %s", loginEvent);
        switch (loginEvent) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGINING:
                logger.d("conversationContentFragment#login#recv handleDoingLogin event");
                if (mReconnectLoadingView != null) {
                    mReconnectLoadingView.setVisibility(View.VISIBLE);
                }
                break;
            case LOCAL_LOGIN_MSG_SERVICE:
            case LOGIN_OK:
                isManualMConnect = false;
                logger.d("conversationContentFragment#loginOk");
                mNoNetworkView.setVisibility(View.GONE);

                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                onLoginFailure(loginEvent);
                break;
            case PC_OFFLINE:
            case KICK_PC_SUCCESS:
//                onPCLoginStatusNotify(false);
                break;
            case KICK_PC_FAILED:
//                Toast.makeText(getActivity(), getString(R.string.kick_pc_failed), Toast.LENGTH_SHORT).show();
                break;
            case PC_ONLINE:
//                onPCLoginStatusNotify(true);
                break;
            default:
                mReconnectLoadingView.setVisibility(View.GONE);
                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SocketEvent socketEvent) {
        switch (socketEvent) {
            case MSG_SERVER_DISCONNECTED:
                handleServerDisconnected();
                break;
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                handleServerDisconnected();
                onSocketFailure(socketEvent);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReconnectEvent reconnectEvent) {
        switch (reconnectEvent) {
            case DISABLE:
                handleServerDisconnected();
                break;
        }
    }

    private void onLoginFailure(LoginEvent event) {
        if (!isManualMConnect) {
            return;
        }
        isManualMConnect = false;
        String errorTip = getString(R.string.login_error_unexpected);
        mReconnectLoadingView.setVisibility(View.GONE);
        ViewUtil.showMessage(errorTip);
    }

    private void onSocketFailure(SocketEvent event) {
        if (!isManualMConnect) {
            return;
        }
        isManualMConnect = false;
        String errorTip = getString(R.string.login_error_unexpected);
        mReconnectLoadingView.setVisibility(View.GONE);
        ViewUtil.showMessage(errorTip);
    }

    // 更新页面以及下面的未读总计数
    private void onShieldSuccess(GroupEntity entity) {
        if (entity == null) {
            return;
        }
        // 更新某个sessionId
        mConversationAdapter.updateRecentInfoByShield(entity);
        IMUnreadMsgManager unreadMsgManager = imService.getUnReadMsgManager();

        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        logger.d("unread#total cnt %d", totalUnreadMsgCnt);
        ((HomeActivity) _mActivity).setUnreadMessageCount(totalUnreadMsgCnt);
    }

    private void onShieldFail() {
        ViewUtil.showMessage("操作失败，请稍后重试");
    }

    /**
     * 搜索数据OK
     * 群组数据与user数据都已经完毕
     */
//    public void searchDataReady() {
//        if (imService.getImFriendManager().isUserDataReady() &&
//                imService.getGroupManager().isGroupReady()) {
//            showSearchFrameLayout();
//        }
//    }

    /**
     * 多端，PC端在线状态通知
     * //     * @param isOnline
     */
//    public void onPCLoginStatusNotify(boolean isOnline){
//        logger.d("chatfragment#onPCLoginStatusNotify");
//        if(isOnline){
//            reconnectingProgressBar.setVisibility(View.GONE);
//            noNetworkView.setVisibility(View.VISIBLE);
//            notifyImage.setImageResource(R.drawable.pc_notify);
//            displayView.setText(R.string.pc_status_notify);
//            /**添加踢出事件*/
//            noNetworkView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    reconnectingProgressBar.setVisibility(View.VISIBLE);
//                    imService.getLoginManager().reqKickPCClient();
//                }
//            });
//        }else{
//            noNetworkView.setVisibility(View.GONE);
//        }
//    }
    private void handleServerDisconnected() {
        logger.d("conversationContentFragment#handleServerDisconnected");

        if (mReconnectLoadingView != null) {
            mReconnectLoadingView.setVisibility(View.GONE);
        }

        if (mNoNetworkView != null) {
            mNotifyView.setImageResource(R.mipmap.warning);
            mNoNetworkView.setVisibility(View.VISIBLE);
            if (imService != null) {
                if (imService.getLoginManager().isKickout()) {
                    mDisconnectView.setText(R.string.disconnect_kickout);
                } else {
                    mDisconnectView.setText(R.string.no_network);
                }
            }

            // 重连【断线、被其他移动端挤掉】
            mNoNetworkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logger.d("conversationContentFragment#noNetworkView clicked");
                    if (!NetworkUtil.isNetWorkAvalible(getActivity())) {
                        ViewUtil.showMessage(R.string.no_network_toast);
                        return;
                    }

                    mReconnectLoadingView.setVisibility(View.VISIBLE);
                    isManualMConnect = true;
                    IMLoginManager.getInstance().relogin();
                }
            });
        }
    }

    /**
     * 这个处理过于粗暴
     */
    private void onRecentContactDataReady() {
        boolean isUserData = imService.getContactManager().isUserDataReady();
        boolean isSessionData = imService.getSessionManager().isSessionListReady();
        boolean isGroupData = imService.getGroupManager().isGroupReady();
        if (!(isUserData && isSessionData && isGroupData)) {
            return;
        }

        IMUnreadMsgManager unreadMsgManager = imService.getUnReadMsgManager();
        int totalUnreadMsgCnt = unreadMsgManager.getTotalUnreadCount();
        logger.d("unread#total cnt %d", totalUnreadMsgCnt);
        // 设置未读消息数
        ((HomeActivity) _mActivity).setUnreadMessageCount(totalUnreadMsgCnt);

        List<RecentInfo> recentSessionList = imService.getSessionManager().getRecentListInfo();
        setNoConversationView(recentSessionList);
        mConversationAdapter.setNewData(recentSessionList);
        mReconnectLoadingView.setVisibility(View.GONE);
    }

    private void setNoConversationView(List<RecentInfo> recentSessionList) {
        if (recentSessionList == null || recentSessionList.isEmpty()) {
            mNoConversationView.setVisibility(View.VISIBLE);
        } else {
            mNoConversationView.setVisibility(View.GONE);
        }
    }

    public static class ConversationAdapter extends BaseQuickAdapter<RecentInfo, BaseViewHolder> {

        public static final int ITEM_TYPE_INVALID = 0;
        public static final int ITEM_TYPE_USER = 1;
        public static final int ITEM_TYPE_GROUP = 2;

        private RequestManager mRequestManager;
        private Transformation mTransformation;
        private Context mContext;

        public ConversationAdapter(Context context, @Nullable List<RecentInfo> data) {
            super(data);

            mContext = context;
            mRequestManager = Glide.with(context);
            mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());

            setMultiTypeDelegate(new MultiTypeDelegate<RecentInfo>() {
                @Override
                protected int getItemType(RecentInfo recentInfo) {
                    int sessionType = recentInfo.getSessionType();
                    if (sessionType == DBConstant.SESSION_TYPE_SINGLE) {
                        return ITEM_TYPE_USER;
                    } else if (sessionType == DBConstant.SESSION_TYPE_GROUP) {
                        return ITEM_TYPE_GROUP;
                    } else {
                        return ITEM_TYPE_INVALID;
                    }
                }
            });

            getMultiTypeDelegate()
                    .registerItemType(ITEM_TYPE_INVALID, 0)
                    .registerItemType(ITEM_TYPE_USER, R.layout.item_conversation_user)
                    .registerItemType(ITEM_TYPE_GROUP, R.layout.item_conversation_group);
        }

        @Override
        protected void convert(BaseViewHolder helper, RecentInfo item) {
            setBaseView(helper, item);
            switch (helper.getItemViewType()) {
                case ITEM_TYPE_USER:
                    setUserView(helper, item);
                    break;

                case ITEM_TYPE_GROUP:
                    setGroupView(helper, item);
                    break;
            }
        }

        private void setBaseView(BaseViewHolder helper, RecentInfo item) {
            String avatarUrl = null;
            if (item.getAvatar() != null) {
                avatarUrl = item.getAvatar();
            }

            mRequestManager
                    .load(avatarUrl)
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .crossFade()
                    .into((ImageView) helper.getView(R.id.iv_avatar));

            helper.setText(R.id.tv_name, item.getName());
            helper.setText(R.id.tv_message_body, item.getLatestMsgData());
            helper.setText(R.id.tv_message_time, DateUtil.getSessionTime(item.getUpdateTime()));

            TextView unReadCountView = helper.getView(R.id.tv_message_count_notify);
            int unReadCount = item.getUnReadCount();
            // 设置未读消息计数
            if (unReadCount > 0) {
                String countString = String.valueOf(unReadCount);
                if (unReadCount > 99) {
                    countString = "99+";
                }
                unReadCountView.setVisibility(View.VISIBLE);
                unReadCountView.setText(countString);
            } else {
                unReadCountView.setVisibility(View.GONE);
            }

            if (unReadCount > 0) {
                if (item.isForbidden()) {
                    unReadCountView.setBackgroundResource(R.mipmap.im_message_notify_no_disturb);
                    unReadCountView.setVisibility(View.VISIBLE);
                    unReadCountView.setText("");
                    ((RelativeLayout.LayoutParams) unReadCountView.getLayoutParams()).leftMargin = CommonUtil.dip2px(mContext, -7);
                    ((RelativeLayout.LayoutParams) unReadCountView.getLayoutParams()).topMargin = CommonUtil.dip2px(mContext, 6);
                    unReadCountView.getLayoutParams().width = CommonUtil.dip2px(mContext, 10);
                    unReadCountView.getLayoutParams().height = CommonUtil.dip2px(mContext, 10);

                } else {
                    unReadCountView.setBackgroundResource(R.mipmap.im_message_notify);
                    unReadCountView.setVisibility(View.VISIBLE);
                    ((RelativeLayout.LayoutParams) unReadCountView.getLayoutParams()).leftMargin = CommonUtil.dip2px(mContext, -10);
                    ((RelativeLayout.LayoutParams) unReadCountView.getLayoutParams()).topMargin = CommonUtil.dip2px(mContext, 3);
                    unReadCountView.getLayoutParams().width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    unReadCountView.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    unReadCountView.setPadding(CommonUtil.dip2px(mContext, 3), 0, CommonUtil.dip2px(mContext, 3), 0);

                    String strCountString = String.valueOf(unReadCount);
                    if (unReadCount > 99) {
                        strCountString = "99+";
                    }
                    unReadCountView.setVisibility(View.VISIBLE);
                    unReadCountView.setText(strCountString);
                }

            } else {
                unReadCountView.setVisibility(View.GONE);
            }

            // 将置顶的item背景置灰
            int backgroundRes;
            if (item.isTop()) {
                backgroundRes = R.color.list_item_top_color;
            } else {
                backgroundRes = R.color.white;
            }
            helper.setBackgroundColor(R.id.rl_root, ContextCompat.getColor(mContext, backgroundRes));

            helper.setVisible(R.id.iv_no_disturb, item.isForbidden());
        }

        private void setUserView(BaseViewHolder helper, RecentInfo item) {

        }

        private void setGroupView(BaseViewHolder helper, RecentInfo item) {

        }

        /**
         * 更新单个RecentInfo 屏蔽群组信息
         *
         * @param entity
         */
        public void updateRecentInfoByShield(GroupEntity entity) {
            String sessionKey = entity.getSessionKey();
            for (RecentInfo recentInfo : getData()) {
                if (recentInfo.getSessionKey().equals(sessionKey)) {
                    int status = entity.getStatus();
                    boolean isForbidden = status == DBConstant.STATUS_SHIELD;
                    recentInfo.setForbidden(isForbidden);
                    notifyDataSetChanged();
                    break;
                }
            }
        }

        public int getUnreadPositionOnView(int currentPosition) {
            int nextIndex = currentPosition + 1;
            int sum = getItemCount();
            if (nextIndex > sum) {
                currentPosition = 0;
            }

            // 从当前点到末尾
            for (int i = nextIndex; i < sum; i++) {
                int unCount = getData().get(i).getUnReadCount();
                if (unCount > 0) {
                    return i;
                }
            }

            // 从末尾到当前点
            for (int i = 0; i < currentPosition; i++) {
                int unCount = getData().get(i).getUnReadCount();
                if (unCount > 0) {
                    return i;
                }
            }

            // 最后返回到最上面
            return 0;
        }
    }
}
