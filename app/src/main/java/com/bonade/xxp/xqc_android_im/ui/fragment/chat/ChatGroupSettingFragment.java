package com.bonade.xxp.xqc_android_im.ui.fragment.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.UserInfoEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMContactManager;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.ui.activity.FriendInfoActivity;
import com.bonade.xxp.xqc_android_im.ui.base.BaseFragment;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentArgs;
import com.bonade.xxp.xqc_android_im.ui.base.FragmentContainerActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.ContactsSelectFragment;
import com.bonade.xxp.xqc_android_im.ui.helper.CheckboxConfigHelper;
import com.bonade.xxp.xqc_android_im.ui.widget.CommonDialog;
import com.bonade.xxp.xqc_android_im.ui.widget.groupimageview.NineGridImageView;
import com.bonade.xxp.xqc_android_im.ui.widget.groupimageview.NineGridImageViewAdapter;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ChatGroupSettingFragment extends BaseFragment {

    public static void launch(Activity from, String sessionKey) {
        FragmentArgs args = new FragmentArgs();
        args.add(KEY_SESSION_KEY, sessionKey);
        FragmentContainerActivity.launch(from, ChatGroupSettingFragment.class, args);
    }

    private static final String KEY_SESSION_KEY = "KEY_SESSION_KEY";

    @BindView(R.id.iv_group_avatar)
    NineGridImageView mGroupAvatarView;

    @BindView(R.id.tv_group_name_top)
    TextView mGroupNameTopView;

    @BindView(R.id.tv_group_name)
    TextView mGroupNameView;

    @BindView(R.id.tv_member_count)
    TextView mMemberCountView;

    @BindView(R.id.rv_member)
    RecyclerView mRecyclerView;

    @BindView(R.id.cb_top_session)
    CheckBox mTopSessionView;

    @BindView(R.id.cb_no_disturb)
    CheckBox mNoDisturbView;

    private IMService mIMService;
    private String mCurSessionKey;
    private PeerEntity mPeerEntity;

    private RequestManager mRequestManager;
    private GroupMemberAdapter mGroupMemberAdapter;

    // 详情的配置 勿扰以及指定聊天
    private CheckboxConfigHelper mCheckboxConfigHelper = new CheckboxConfigHelper();

    private IMServiceConnector mIMServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("groupmgr#onIMServiceConnected");
            mIMService = mIMServiceConnector.getIMService();
            if (mIMService == null) {
                ViewUtil.showMessage("无法连接到后台服务");
                return;
            }
            mCheckboxConfigHelper.init(mIMService.getConfigSp());
            initView();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    @OnClick(R.id.tv_group_name)
    void groupNameClick() {
        new MaterialDialog.Builder(_mActivity)
                .title("修改群名称")
                .widgetColor(Color.GRAY)//输入框光标的颜色
                .inputType(InputType.TYPE_TEXT_VARIATION_NORMAL)
                .input("请输入群名称", mPeerEntity.getMainName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String text = dialog.getInputEditText().getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            ViewUtil.showMessage("群名称不能为空");
                            return;
                        }

                        ViewUtil.createProgressDialog(_mActivity, "");

                    }
                })
                .show();
    }

    @OnClick(R.id.tv_clear_record)
    void clearRecordClick() {
        CommonDialog dialog = new CommonDialog(_mActivity);
        dialog.setTitle("温馨提示");
        dialog.setMessage("您确定要清空聊天记录吗？");
        dialog.setLeftButtonText(R.string.common_cancel);
        dialog.setRightButtonText(R.string.common_ok);
        dialog.setClickCallbackListener(new CommonDialog.ClickCallbackListener() {
            @Override
            public void fromSure(CommonDialog dialog) {
                ViewUtil.showMessage("确定");
                dialog.dismiss();
            }

            @Override
            public void fromCancel(CommonDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestManager = Glide.with(this);
        mIMServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_group_setting;
    }

    @Override
    protected void setupViews(View view, Bundle savedInstanceState) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mIMServiceConnector.disconnect(getActivity());
    }

    private void initView() {
        mCurSessionKey = getArguments().getString(KEY_SESSION_KEY);
        if (TextUtils.isEmpty(mCurSessionKey)) {
            return;
        }
        mPeerEntity = mIMService.getSessionManager().findPeerEntity(mCurSessionKey);
        if (mPeerEntity == null) {
            return;
        }

        FragmentContainerActivity activity = (FragmentContainerActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle("聊天信息");

        mGroupAvatarView.setAdapter(new NineGridImageViewAdapter<UserEntity>() {
            @Override
            protected void onDisplayImage(Context context, ImageView imageView, UserEntity userEntity) {
                int loginId = IMLoginManager.getInstance().getLoginId();
                if (userEntity.getPeerId() == loginId) {
                    File file = new File(CommonUtil.getUserAvatarSavePath(loginId));
                    if (file.exists()) {
                        mRequestManager
                                .load(file)
                                .error(R.mipmap.im_default_user_avatar)
                                .placeholder(R.mipmap.im_default_user_avatar)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .crossFade()
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.mipmap.im_default_user_avatar);
                    }
                } else {
                    mRequestManager
                            .load(userEntity.getAvatar())
                            .error(R.mipmap.im_default_user_avatar)
                            .placeholder(R.mipmap.im_default_user_avatar)
                            .crossFade()
                            .into(imageView);
                }
            }

            @Override
            protected ImageView generateImageView(Context context) {
                return super.generateImageView(context);
            }
        });

        initGroupAvatar();
        initGroupName();
        initGroupCount();
        initCheckbox();
        setupRecyclerView();
    }

    private void initGroupAvatar() {
        List<UserEntity> userEntities = new ArrayList<>();
        for (Integer userId : ((GroupEntity) mPeerEntity).getGroupMemberIds()) {
            UserEntity entity = IMContactManager.getInstance().findContact(userId);

            if (entity != null) {
                userEntities.add(entity);
            }
            if (userEntities.size() >= 9) {
                break;
            }
        }
        mGroupAvatarView.setImagesData(userEntities);
    }

    private void initGroupName() {
        mGroupNameTopView.setText(mPeerEntity.getMainName());
        mGroupNameView.setText(mPeerEntity.getMainName());
    }

    private void initGroupCount() {
        mMemberCountView.setText(((GroupEntity) mPeerEntity).getUserCount() + "人");
    }

    private void initCheckbox() {
        mCheckboxConfigHelper.initTopCheckBox(mTopSessionView, mCurSessionKey);
    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(false);
        GridLayoutManager layoutManager = new GridLayoutManager(_mActivity, 5);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mGroupMemberAdapter = new GroupMemberAdapter(_mActivity, R.layout.item_group_member, mIMService, mPeerEntity));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupEvent groupEvent) {
        switch (groupEvent.getEvent()) {
            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT: {
                ViewUtil.showMessage("修改群失败");
                return;
            }
            case CHANGE_GROUP_MEMBER_SUCCESS: {
                onMemberChangeSuccess(groupEvent);
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
                mGroupMemberAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void onMemberChangeSuccess(GroupEvent groupEvent) {
        int groupId = groupEvent.getGroupEntity().getPeerId();
        if (groupId != mPeerEntity.getPeerId()) {
            return;
        }

        List<Integer> changeList = groupEvent.getChangeList();
        if (changeList == null || changeList.isEmpty()) {
            return;
        }

        mPeerEntity = groupEvent.getGroupEntity();
        initGroupAvatar();
        initGroupCount();

        int changeType = groupEvent.getChangeType();

        switch (changeType) {
            case DBConstant.GROUP_MODIFY_TYPE_ADD:
                ArrayList<UserEntity> newList = new ArrayList<>();
                for (Integer userId : changeList) {
                    UserEntity userEntity = mIMService.getContactManager().findContact(userId);
                    if (userEntity != null) {
                        newList.add(userEntity);
                    }
                }

                mGroupMemberAdapter.addMembers(newList);
                break;
            case DBConstant.GROUP_MODIFY_TYPE_DEL:
                for (Integer userId : changeList) {
                    mGroupMemberAdapter.removeById(userId);
                }
                break;
        }
    }

    public static class GroupMemberAdapter extends BaseQuickAdapter<Object, BaseViewHolder> {

        private Logger logger = Logger.getLogger(GroupMemberAdapter.class);

        // 用于控制是否是删除状态，也就是那个减号是否出现
        private boolean mRemoveState = false;
        private boolean mShowMinusTag = false;
        private boolean mShowPlusTag = false;

        private Context mContext;
        private IMService mIMService;
        private PeerEntity mPeerEntity;
        private int mGroupCreatorId;

        private RequestManager mRequestManager;
        private Transformation mTransformation;

        public GroupMemberAdapter(Context context, int layoutResId, IMService imService, PeerEntity peerEntity) {
            super(layoutResId);
            getData().clear();

            mContext = context;
            mIMService = imService;
            mPeerEntity = peerEntity;

            mRequestManager = Glide.with(context);
            mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());
            setMemberData();
        }

        @Override
        protected void convert(BaseViewHolder helper, Object item) {
            TextView memberNameView = helper.getView(R.id.tv_member_name);
            ImageView memberAvatarView = helper.getView(R.id.iv_member_avatar);
            if (item instanceof UserEntity) {
                final UserEntity userEntity = (UserEntity) item;
                memberNameView.setVisibility(View.VISIBLE);
                memberNameView.setText(userEntity.getMainName());

                int loginId = mIMService.getLoginManager().getLoginId();
                if (loginId == userEntity.getPeerId()) {
                    mRequestManager
                            .load(CommonUtil.getUserAvatarSavePath(loginId))
                            .error(R.mipmap.im_default_user_avatar)
                            .placeholder(R.mipmap.im_default_user_avatar)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .bitmapTransform(mTransformation)
                            .into(memberAvatarView);
                } else {
                    mRequestManager
                            .load(userEntity.getAvatar())
                            .error(R.mipmap.im_default_user_avatar)
                            .placeholder(R.mipmap.im_default_user_avatar)
                            .bitmapTransform(mTransformation)
                            .into(memberAvatarView);
                }
                memberAvatarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendInfoActivity.launch(mContext, userEntity.getPeerId());
                    }
                });
            } else {
                final MemberOption memberOption = (MemberOption) item;
                memberNameView.setVisibility(View.GONE);
                memberAvatarView.setImageResource(memberOption.getDrawableRes());
                memberAvatarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (memberOption.getOption()) {
                            case MemberOption.OPTION_PLUS:
                                ContactsSelectFragment.launch((Activity) mContext, mPeerEntity.getSessionKey());
                                break;
                            case MemberOption.OPTION_MINUS:
                                ContactsSelectFragment.launch((Activity) mContext, mPeerEntity.getSessionKey());
                                break;
                        }
                    }
                });
            }
        }

        private void setMemberData() {
            int sessiontype = mPeerEntity.getType();
            switch (sessiontype) {
                case DBConstant.SESSION_TYPE_GROUP: {
                    setGroupData((GroupEntity) mPeerEntity);
                }
                break;
                case DBConstant.SESSION_TYPE_SINGLE: {
//                    setSingleData((UserEntity) mPeerEntity);
                }
                break;
            }
        }

        private void setGroupData(GroupEntity entity) {
            int loginId = mIMService.getLoginManager().getLoginId();
            int ownerId = entity.getCreatorId();
            IMContactManager imContactManager = mIMService.getContactManager();
            for (int memberId : entity.getGroupMemberIds()) {
                UserEntity userEntity = imContactManager.findContact(memberId);
                if (userEntity != null) {
                    if (ownerId == userEntity.getPeerId()) {
                        mGroupCreatorId = ownerId;
                        getData().add(0, userEntity);
                    } else {
                        getData().add(userEntity);
                    }
                }
            }

            // TODO: 2018/9/21 后台接口没好，暂时不加
//            if (loginId == ownerId) {
//                // 展示 + -
//                mShowMinusTag = true;
//                mShowPlusTag = true;
//                getData().add(MemberOption.getPlusOption());
//                getData().add(MemberOption.getMinusOption());
//            } else {
//                // 展示 +
//                mShowPlusTag = true;
//                getData().add(MemberOption.getPlusOption());
//            }
        }

        public void removeById(int contactId) {
            for (Object object : getData()) {
                if (object instanceof UserEntity) {
                    UserEntity userEntity = (UserEntity) object;
                }
            }
        }

        public void addMembers(List<UserEntity> userEntities) {
            if (mShowMinusTag) {
                addData(getData().size() - 2, userEntities);
            } else {
                addData(getData().size() - 1, userEntities);
            }
        }

        private static class MemberOption {

            public static final int OPTION_PLUS = 1;
            public static final int OPTION_MINUS = 2;

            private int drawableRes;
            private int option;

            public MemberOption(int drawableRes, int option) {
                this.drawableRes = drawableRes;
                this.option = option;
            }

            public static MemberOption getPlusOption() {
                return new MemberOption(R.mipmap.ic_add_member, OPTION_PLUS);
            }

            public static MemberOption getMinusOption() {
                return new MemberOption(R.mipmap.ic_remove_member, OPTION_MINUS);
            }

            public int getDrawableRes() {
                return drawableRes;
            }

            public int getOption() {
                return option;
            }
        }
    }
}
