package com.bonade.xxp.xqc_android_im.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.SystemConfigSp;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.HandlerConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.AudioMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.TextMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;
import com.bonade.xxp.xqc_android_im.imservice.event.MessageEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.PriorityEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.UserInfoEvent;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.imservice.support.IMServiceConnector;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.fragment.chat.ChatGroupSettingFragment;
import com.bonade.xxp.xqc_android_im.ui.helper.AudioPlayerHandler;
import com.bonade.xxp.xqc_android_im.ui.helper.AudioRecordHandler;
import com.bonade.xxp.xqc_android_im.ui.helper.Emoparser;
import com.bonade.xxp.xqc_android_im.ui.helper.listener.OnDoubleClickListener;
import com.bonade.xxp.xqc_android_im.ui.widget.CustomEditView;
import com.bonade.xxp.xqc_android_im.ui.widget.EmoGridView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.AudioRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.ImageRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.RenderType;
import com.bonade.xxp.xqc_android_im.ui.widget.message.TextRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.TimeRenderView;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.DateUtil;
import com.bonade.xxp.xqc_android_im.util.FileUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.yongchun.library.view.ImageSelectorActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import butterknife.OnTouch;
import rx.functions.Action1;

import static com.yongchun.library.view.ImageSelectorActivity.MODE_MULTIPLE;
import static com.yongchun.library.view.ImageSelectorActivity.MODE_SINGLE;

/**
 * 聊天界面
 */
public class ChatActivity extends BaseActivity {

    public static void launch(Context from, String sessionKey) {
        from.startActivity(new Intent(from, ChatActivity.class).putExtra(KEY_SESSION_KEY, sessionKey));
    }

    private Logger logger = Logger.getLogger(ChatActivity.class);
    public static final String KEY_SESSION_KEY = "KEY_SESSION_KEY";
    // 处理语音
    private static Handler uiHandler = null;

    @BindView(R.id.ll_root)
    LinearLayout mRootView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.lv_msg)
    ListView mListView;

    @BindView(R.id.et_msg_text)
    CustomEditView mEditMsgView;

    @BindView(R.id.tv_send_msg)
    TextView mSendMsgView;

    @BindView(R.id.btn_record_voice)
    Button mRecordAudioView;

    @BindView(R.id.iv_show_keyboard)
    ImageView mShowKeyboardView;

    @BindView(R.id.iv_voice)
    ImageView mVoiceView;

    @BindView(R.id.iv_show_add_photo)
    ImageView mShowAddPhotoView;

    @BindView(R.id.iv_show_emo)
    ImageView mShowEmoView;

    @BindView(R.id.ll_emo)
    LinearLayout mEmoLayout;

    @BindView(R.id.gv_emo)
    EmoGridView mEmoGridView;

    @BindView(R.id.tv_new_msg_tip)
    TextView mNewMsgTipView;

    @BindView(R.id.ll_add_others_panel)
    LinearLayout mAddOthersPanelView;

    private ChatAdapter mAdapter;
    private String mAudioSavePath;
    private InputMethodManager mInputMethodManager;
    private AudioRecordHandler mAudioRecordHandler;
    private Thread mAudioRecorderThread;

    // 录音view相关
    private Dialog mSoundVolumeDialog;
    private ImageView mSoundVolumeView;
    private LinearLayout mSoundVolumeLayout;

    // 语音相关的
//    private boolean audioReday = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private String mTakePhotoSavePath = "";
    private IMService imService;
    private UserEntity mLoginUser;
    private PeerEntity mPeerEntity;

    private String mCurrentSessionKey;
    private int mHistoryTimes = 0;

    // 键盘布局相关参数
    private int mRootBottom = Integer.MIN_VALUE;
    private int mKeyboardHeight = 0;
    private SwitchInputMethodReceiver mSwitchInputMethodReceiver;
    private String mCurrentInputMethod;

    // 获取列表最后一个可见view位置
    private int mLastItemPosition;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("chat_activity#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            initData();
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    @OnTouch(R.id.lv_msg)
    boolean msgTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mEditMsgView.clearFocus();
            if (mEmoLayout.getVisibility() == View.VISIBLE) {
                mEmoLayout.setVisibility(View.GONE);
            }

            if (mAddOthersPanelView.getVisibility() == View.VISIBLE) {
                mAddOthersPanelView.setVisibility(View.GONE);
            }
            mInputMethodManager.hideSoftInputFromWindow(mEditMsgView.getWindowToken(), 0);
        }
        return false;
    }

    @OnClick(R.id.tv_new_msg_tip)
    void newMsgTipClick() {
        scrollToBottomListItem();
        mNewMsgTipView.setVisibility(View.GONE);
    }

    @OnFocusChange(R.id.et_msg_text)
    void msgTextFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mKeyboardHeight == 0) {
                mAddOthersPanelView.setVisibility(View.GONE);
                mEmoLayout.setVisibility(View.GONE);
            } else {
                ChatActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                if (mAddOthersPanelView.getVisibility() == View.GONE) {
                    mAddOthersPanelView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @OnTextChanged(R.id.et_msg_text)
    void msgTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            mSendMsgView.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mEditMsgView
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.iv_show_emo);
            mShowAddPhotoView.setVisibility(View.GONE);
        } else {
            mShowAddPhotoView.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mEditMsgView
                    .getLayoutParams();
            param.addRule(RelativeLayout.LEFT_OF, R.id.iv_show_emo);
            mSendMsgView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.iv_show_add_photo)
    void addPhotoClick() {
        mRecordAudioView.setVisibility(View.GONE);
        mShowKeyboardView.setVisibility(View.GONE);
        mEditMsgView.setVisibility(View.VISIBLE);
        // TODO: 2018/9/21 没有语音功能暂时隐藏
        mVoiceView.setVisibility(View.GONE);
        mShowEmoView.setVisibility(View.VISIBLE);

        if (mKeyboardHeight != 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        if (mAddOthersPanelView.getVisibility() == View.VISIBLE) {
            if (!mEditMsgView.hasFocus()) {
                mEditMsgView.requestFocus();
            }
            mInputMethodManager.toggleSoftInputFromWindow(mEditMsgView.getWindowToken(), 1, 0);
            if (mKeyboardHeight == 0) {
                mAddOthersPanelView.setVisibility(View.GONE);
            }
        } else {
            mAddOthersPanelView.setVisibility(View.VISIBLE);
            mInputMethodManager.hideSoftInputFromWindow(mEditMsgView.getWindowToken(), 0);
        }

        if (null != mEmoLayout && mEmoLayout.getVisibility() == View.VISIBLE) {
            mEmoLayout.setVisibility(View.GONE);
        }

        scrollToBottomListItem();
    }

    @OnClick(R.id.iv_show_emo)
    void showEmoClick() {
        mRecordAudioView.setVisibility(View.GONE);
        mShowKeyboardView.setVisibility(View.GONE);
        mEditMsgView.setVisibility(View.VISIBLE);
        // TODO: 2018/9/21 没有语音功能暂时隐藏
        mVoiceView.setVisibility(View.GONE);
        mShowEmoView.setVisibility(View.VISIBLE);

        if (mKeyboardHeight != 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        if (mEmoLayout.getVisibility() == View.VISIBLE) {
            if (!mEditMsgView.hasFocus()) {
                mEditMsgView.requestFocus();
            }
            mInputMethodManager.toggleSoftInputFromWindow(mEditMsgView.getWindowToken(), 1, 0);
            if (mKeyboardHeight == 0) {
                mEmoLayout.setVisibility(View.GONE);
            }
        } else {
            mEmoLayout.setVisibility(View.VISIBLE);
            mEmoGridView.setVisibility(View.VISIBLE);
            mInputMethodManager.hideSoftInputFromWindow(mEditMsgView.getWindowToken(), 0);
        }

        if (mAddOthersPanelView.getVisibility() == View.VISIBLE) {
            mAddOthersPanelView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.iv_show_keyboard)
    void showKeyboardClick() {
        mRecordAudioView.setVisibility(View.GONE);
        mShowKeyboardView.setVisibility(View.GONE);
        mEditMsgView.setVisibility(View.VISIBLE);
        // TODO: 2018/9/21 没有语音功能暂时隐藏
        mVoiceView.setVisibility(View.GONE);
        mShowEmoView.setVisibility(View.VISIBLE);

        if (mKeyboardHeight != 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        if (!mEditMsgView.hasFocus()) {
            mEditMsgView.requestFocus();
        }

        mInputMethodManager.toggleSoftInputFromWindow(mEditMsgView.getWindowToken(), 1, 0);
    }

    @OnClick(R.id.iv_voice)
    void voiceClick() {
        mInputMethodManager.hideSoftInputFromWindow(mEditMsgView.getWindowToken(), 0);
        mEditMsgView.setVisibility(View.GONE);
        mVoiceView.setVisibility(View.GONE);
        mRecordAudioView.setVisibility(View.VISIBLE);
        mShowKeyboardView.setVisibility(View.VISIBLE);
        mEmoLayout.setVisibility(View.GONE);
        mAddOthersPanelView.setVisibility(View.GONE);
        mEditMsgView.setText("");
    }

    private float y1, y2;

    @OnTouch(R.id.btn_record_voice)
    boolean recordVoiceTouch(View v, MotionEvent event) {
        scrollToBottomListItem();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (AudioPlayerHandler.getInstance().isPlaying()) {
                    AudioPlayerHandler.getInstance().stopPlayer();
                }
                y1 = event.getY();
                mRecordAudioView.setBackgroundResource(R.drawable.im_pannel_btn_voice_forward_pressed);
                mRecordAudioView.setText(getString(
                        R.string.release_to_send_voice));
                mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_01);
                mSoundVolumeView.setVisibility(View.VISIBLE);
                mSoundVolumeLayout.setBackgroundResource(R.mipmap.im_sound_volume_default_bg);
                mSoundVolumeDialog.show();
                mAudioSavePath = CommonUtil
                        .getAudioSavePath(IMLoginManager.getInstance().getLoginId());

                // 这个callback很蛋疼，发送消息从MotionEvent.ACTION_UP 判断
                mAudioRecordHandler = new AudioRecordHandler(mAudioSavePath);
                mAudioRecorderThread = new Thread(mAudioRecordHandler);
                mAudioRecordHandler.setRecording(true);
                logger.d("chat_activity#audio#audio record thread starts");
                mAudioRecorderThread.start();
                break;
            case MotionEvent.ACTION_MOVE:
                y2 = event.getY();
                if (y1 - y2 > 180) {
                    mSoundVolumeView.setVisibility(View.GONE);
                    mSoundVolumeLayout.setBackgroundResource(R.mipmap.im_sound_volume_cancel_bg);
                } else {
                    mSoundVolumeView.setVisibility(View.VISIBLE);
                    mSoundVolumeLayout.setBackgroundResource(R.mipmap.im_sound_volume_default_bg);
                }
                break;
            case MotionEvent.ACTION_UP:
                y2 = event.getY();
                if (mAudioRecordHandler.isRecording()) {
                    mAudioRecordHandler.setRecording(false);
                }
                if (mSoundVolumeDialog.isShowing()) {
                    mSoundVolumeDialog.dismiss();
                }
                mRecordAudioView.setBackgroundResource(R.mipmap.im_pannel_btn_voice_forward_normal);
                mRecordAudioView.setText(getString(
                        R.string.tip_for_voice_forward));
                if (y1 - y2 <= 180) {
                    if (mAudioRecordHandler.getRecordTime() >= 0.5) {
                        if (mAudioRecordHandler.getRecordTime() < SysConstant.MAX_SOUND_RECORD_TIME) {
                            Message msg = uiHandler.obtainMessage();
                            msg.what = HandlerConstant.HANDLER_RECORD_FINISHED;
                            msg.obj = mAudioRecordHandler.getRecordTime();
                            uiHandler.sendMessage(msg);
                        }
                    } else {
                        mSoundVolumeView.setVisibility(View.GONE);
                        mSoundVolumeLayout
                                .setBackgroundResource(R.mipmap.im_sound_volume_short_tip_bg);
                        mSoundVolumeDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            public void run() {
                                if (mSoundVolumeDialog.isShowing())
                                    mSoundVolumeDialog.dismiss();
                                this.cancel();
                            }
                        }, 700);
                    }
                }
                break;

        }
        return false;
    }

    @OnClick(R.id.tv_send_msg)
    void sendMsgClick() {
        logger.d("chat_activity#send btn clicked");

        String content = mEditMsgView.getText().toString();
        logger.d("chat_activity#chat content:%s", content);
        if (TextUtils.isEmpty(content)) {
            ViewUtil.showMessage(R.string.message_null);
            return;
        }
        TextMessage textMessage = TextMessage.buildForSend(content, mLoginUser, mPeerEntity);
        imService.getMessageManager().sendText(textMessage);
        mEditMsgView.setText("");
        pushList(textMessage);
        scrollToBottomListItem();
    }

    @OnClick(R.id.v_take_photo)
    void takePhotoClick() {
        new RxPermissions(this).requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Action1<Permission>() {
                    @Override
                    public void call(Permission permission) {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            ImageSelectorActivity.start(ChatActivity.this, SysConstant.MAX_SELECT_IMAGE_COUNT, MODE_MULTIPLE, true, true, false);
                            mEditMsgView.clearFocus();//切记清除焦点
                            scrollToBottomListItem();
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            ViewUtil.showMessage("用户拒绝了该权限");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                            ViewUtil.showMessage("权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用");
                        }
                    }
                });
    }

    @OnClick(R.id.v_take_camera)
    void takeCameraClick() {
        new RxPermissions(this).request(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            // 用户已经同意该权限
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            mTakePhotoSavePath = CommonUtil.getImageSavePath(String.valueOf(System
                                    .currentTimeMillis())
                                    + ".jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, CommonUtil.getUriForFile(ChatActivity.this, new File(mTakePhotoSavePath)));
                            startActivityForResult(intent, SysConstant.CAMERA_WITH_DATA);
                            mEditMsgView.clearFocus();//切记清除焦点
                            scrollToBottomListItem();
                        }
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mCurrentSessionKey = getIntent().getStringExtra(KEY_SESSION_KEY);
        initSoftInputMethod();
        initEmo();
        initAudioHandler();
        initAudioSensor();
        initView();
        imServiceConnector.connect(this);
        EventBus.getDefault().register(this);
        logger.d("chat_activity#register im service and eventBus");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comm_one, menu);
        menu.findItem(R.id.action).setIcon(R.mipmap.ic_more_horiz_white_36dp);
        menu.findItem(R.id.action).setVisible((mPeerEntity instanceof GroupEntity) ? true : false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action:
                ChatGroupSettingFragment.launch(this, mPeerEntity.getSessionKey());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        logger.d("chat_activity#onNewIntent:%s", this);
        super.onNewIntent(intent);
        setIntent(intent);
        mHistoryTimes = 0;
        if (intent == null) {
            return;
        }
        String newSessionKey = getIntent().getStringExtra(KEY_SESSION_KEY);
        if (newSessionKey == null) {
            return;
        }

        if (!newSessionKey.equals(mCurrentSessionKey)) {
            mCurrentSessionKey = newSessionKey;
            initData();
        }
    }

    @Override
    protected void onResume() {
        logger.d("chat_activity#onresume:%s", this);
        super.onResume();
        mHistoryTimes = 0;
        // not the first time
        if (imService != null) {
            // 处理session的未读信息
            handleUnreadMsgs();
        }
    }

    @Override
    protected void onStop() {
        logger.d("chat_activity#onStop:%s", this);
        if (null != mAdapter) {
            mAdapter.hidePopup();
        }
        AudioPlayerHandler.getInstance().clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        logger.d("chat_activity#onDestroy:%s", this);
        mHistoryTimes = 0;
        imServiceConnector.disconnect(this);
        EventBus.getDefault().unregister(this);
        mAdapter.clearItem();
//        albumList.clear();
        mSensorManager.unregisterListener(mSensorEventListener, mSensor);
        ImageMessage.clearImageMessageList();
        unregisterReceiver(mSwitchInputMethodReceiver);
        super.onDestroy();
    }

    @Subscribe(priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onEvent(PriorityEvent event) {
        switch (event.getEvent()) {
            case MSG_RECEIVED_MESSAGE: {
                MessageEntity entity = (MessageEntity) event.getObject();
                // 正式当前的会话
                if (mCurrentSessionKey.equals(entity.getSessionKey())) {
                    Message message = Message.obtain();
                    message.what = HandlerConstant.MSG_RECEIVED_MESSAGE;
                    message.obj = entity;
                    uiHandler.sendMessage(message);
                    EventBus.getDefault().cancelEventDelivery(event);
                }
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = SysConstant.MESSAGE_EVENTBUS_PRIORITY)
    public void onEventMainThread(MessageEvent event) {
        MessageEvent.Event type = event.getEvent();
        MessageEntity entity = event.getMessageEntity();
        switch (type) {
            case ACK_SEND_MESSAGE_OK: {
                onMsgAck(event.getMessageEntity());
            }
            break;

            case ACK_SEND_MESSAGE_FAILURE:
                // 失败情况下新添提醒
                ViewUtil.showMessage("请检查网络");
            case ACK_SEND_MESSAGE_TIME_OUT: {
                onMsgUnAckTimeoutOrFailure(event.getMessageEntity());
            }
            break;

            case HANDLER_IMAGE_UPLOAD_FAILD: {
                logger.d("pic#onUploadImageFaild");
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                mAdapter.updateItemState(imageMessage);
                ViewUtil.showMessage("请检查网络");
            }
            break;

            case HANDLER_IMAGE_UPLOAD_SUCCESS: {
                ImageMessage imageMessage = (ImageMessage) event.getMessageEntity();
                mAdapter.updateItemState(imageMessage);
            }
            break;

            case HISTORY_MSG_OBTAIN: {
                if (mHistoryTimes == 1) {
                    mAdapter.clearItem();
                    reqHistoryMsg();
                }
            }
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * 触发条件，imService连接成功，或者newIntent
     */
    private void initData() {
        mHistoryTimes = 0;
        mAdapter.clearItem();
        ImageMessage.clearImageMessageList();
        mLoginUser = imService.getLoginManager().getLoginInfo();
        mPeerEntity = imService.getSessionManager().findPeerEntity(mCurrentSessionKey);
        // 头像、历史消息加载、取消通知
        setTitleByUser();
        reqHistoryMsg();
        mAdapter.setImService(imService, mLoginUser);
        imService.getUnReadMsgManager().readUnreadSession(mPeerEntity.getSessionKey());
        imService.getNotificationManager().cancelSessionNotifications(mPeerEntity.getSessionKey());
    }

    private void initSoftInputMethod() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // 注册广播
        mSwitchInputMethodReceiver = new SwitchInputMethodReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED");
        registerReceiver(mSwitchInputMethodReceiver, filter);

        SystemConfigSp.getInstance().init(this);
        mCurrentInputMethod = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        mKeyboardHeight = SystemConfigSp.getInstance().getIntConfig(mCurrentInputMethod);
    }

    private void initEmo() {
        Emoparser.getInstance(ChatActivity.this);
    }

    private void initAudioHandler() {
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_RECORD_FINISHED:
                        onRecordVoiceEnd((Float) msg.obj);
                        break;

                    // 录音结束
                    case HandlerConstant.HANDLER_STOP_PLAY:
                        // 其他地方处理了
                        //adapter.stopVoicePlayAnim((String) msg.obj);
                        break;
                    case HandlerConstant.RECEIVE_MAX_VOLUME:
                        onReceiveMaxVolume((Integer) msg.obj);
                        break;

                    case HandlerConstant.RECORD_AUDIO_TOO_LONG:
                        doFinishRecordAudio();
                        break;

                    case HandlerConstant.MSG_RECEIVED_MESSAGE:
                        MessageEntity entity = (MessageEntity) msg.obj;
                        onMsgRecv(entity);
                        break;

                    default:
                        break;
                }
            }
        };
    }

    /**
     * 初始化AudioManager，用于访问控制音量和钤声模式
     */
    private void initAudioSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            try {
                if (!AudioPlayerHandler.getInstance().isPlaying()) {
                    return;
                }
                float range = event.values[0];
                if (null != mSensor && range == mSensor.getMaximumRange()) {
                    // 屏幕恢复亮度
                    AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_NORMAL, ChatActivity.this);
                } else {
                    // 屏幕变黑
                    AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_IN_CALL, ChatActivity.this);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void initView() {
        mAdapter = new ChatAdapter(this);
        mListView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int preSum = mListView.getCount();
                        MessageEntity messageEntity = mAdapter.getTopMsgEntity();
                        if (messageEntity != null) {
                            List<MessageEntity> historyMsgInfo = imService.getMessageManager().loadHistoryMsg(messageEntity, mHistoryTimes);
                            if (historyMsgInfo.size() > 0) {
                                mHistoryTimes++;
                                mAdapter.loadHistoryList(historyMsgInfo);
                            }
                        }

                        int afterSum = mListView.getCount();
                        if (afterSum > preSum) {
                            mListView.setSelection(afterSum - preSum - 1);
                        } else {
                            mListView.setSelection(afterSum - preSum);
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 200);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            mNewMsgTipView.setVisibility(View.GONE);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        RelativeLayout.LayoutParams editMsgParams = (RelativeLayout.LayoutParams) mEditMsgView.getLayoutParams();
        editMsgParams.addRule(RelativeLayout.LEFT_OF, R.id.iv_show_emo);
        editMsgParams.addRule(RelativeLayout.RIGHT_OF, R.id.iv_voice);

        initSoundVolumeDialog();

        // OTHER_PANEL_VIEW
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAddOthersPanelView.getLayoutParams();
        if (mKeyboardHeight > 0) {
            params.height = mKeyboardHeight;
            mAddOthersPanelView.setLayoutParams(params);
        }

        // EMO_LAYOUT
        RelativeLayout.LayoutParams paramEmoLayout = (RelativeLayout.LayoutParams) mEmoLayout.getLayoutParams();
        if (mKeyboardHeight > 0) {
            paramEmoLayout.height = mKeyboardHeight;
            mEmoLayout.setLayoutParams(paramEmoLayout);
        }
        mEmoGridView.setOnEmoGridViewItemClick(new EmoGridView.OnEmoGridViewItemClick() {
            @Override
            public void onItemClick(int facesPos, int viewIndex) {
                int deleteId = (++viewIndex) * (SysConstant.pageSize - 1);
                if (deleteId > Emoparser.getInstance(ChatActivity.this).getResIdList().length) {
                    deleteId = Emoparser.getInstance(ChatActivity.this).getResIdList().length;
                }
                if (deleteId == facesPos) {
                    String msgContent = mEditMsgView.getText().toString();
                    if (msgContent.isEmpty())
                        return;
                    if (msgContent.contains("["))
                        msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
                    CharSequence charSequence = Emoparser.getInstance(ChatActivity.this).emoCharsequence(msgContent);
                    mEditMsgView.setText(charSequence);
                } else {
                    int resId = Emoparser.getInstance(ChatActivity.this).getResIdList()[facesPos];
                    String pharse = Emoparser.getInstance(ChatActivity.this).getIdPhraseMap()
                            .get(resId);
                    CharSequence charSequence = Emoparser.getInstance(ChatActivity.this).emoCharsequence(pharse);
                    int startIndex = mEditMsgView.getSelectionStart();
                    Editable edit = mEditMsgView.getEditableText();
                    if (startIndex < 0 || startIndex >= edit.length()) {
                        if (null != pharse) {
                            edit.append(charSequence);
                        }
                    } else {
                        if (null != pharse) {
                            edit.insert(startIndex, pharse);
                        }
                    }
                }
                Editable edtable = mEditMsgView.getText();
                int position = edtable.length();
                Selection.setSelection(edtable, position);
            }
        });
        mEmoGridView.setAdapter();

        // LOADING
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mRootView.getGlobalVisibleRect(r);
                // 进入Activity时会布局，第一次调用onGlobalLayout，先记录开始软键盘没有弹出时底部的位置
                if (mRootBottom == Integer.MIN_VALUE) {
                    mRootBottom = r.bottom;
                    return;
                }
                // adjustResize，软键盘弹出后高度会变小
                if (r.bottom < mRootBottom) {
                    //按照键盘高度设置表情框和发送图片按钮框的高度
                    mKeyboardHeight = mRootBottom - r.bottom;
                    SystemConfigSp.getInstance().init(ChatActivity.this);
                    SystemConfigSp.getInstance().setIntConfig(mCurrentInputMethod, mKeyboardHeight);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAddOthersPanelView.getLayoutParams();
                    params.height = mKeyboardHeight;
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mEmoLayout.getLayoutParams();
                    params1.height = mKeyboardHeight;
                }
            }
        });
    }

    /**
     * 初始化音量对话框
     */
    private void initSoundVolumeDialog() {
        mSoundVolumeDialog = new Dialog(this, R.style.SoundVolumeStyle);
        mSoundVolumeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSoundVolumeDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSoundVolumeDialog.setContentView(R.layout.dialog_sound_volume);
        mSoundVolumeDialog.setCanceledOnTouchOutside(true);
        mSoundVolumeView = mSoundVolumeDialog.findViewById(R.id.iv_sound_volume);
        mSoundVolumeLayout = mSoundVolumeDialog.findViewById(R.id.ll_sound_volume);
    }

    /**
     * 设定聊天名称
     * 1. 如果是user类型， 点击触发UserProfile
     * 2. 如果是群组，检测自己是不是还在群中
     */
    private void setTitleByUser() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mPeerEntity.getMainName());
        int peerType = mPeerEntity.getType();
        switch (peerType) {
            case DBConstant.SESSION_TYPE_GROUP: {
                GroupEntity group = (GroupEntity) mPeerEntity;
                Set<Integer> members = group.getGroupMemberIds();
                if (!members.contains(mLoginUser.getPeerId())) {
                    ViewUtil.showMessage("你可能已经不在该群中了!");
                }
            }
            case DBConstant.SESSION_TYPE_SINGLE: {
                // TODO: 2018/9/4 暂时不知道做什么
            }
        }
    }

    /**
     * 1、初始化请求历史消息
     * 2、本地消息不全，也会触发
     */
    private void reqHistoryMsg() {
        mHistoryTimes++;
        List<MessageEntity> msgList = imService.getMessageManager().loadHistoryMsg(mHistoryTimes, mPeerEntity.getSessionKey(), mPeerEntity);
        pushList(msgList);
        scrollToBottomListItem();
    }

    private void pushList(MessageEntity msg) {
        logger.d("chat#pushList msgInfo:%s", msg);
        mAdapter.addItem(msg);
    }

    private void pushList(List<MessageEntity> entityList) {
        logger.d("chat#pushList list:%d", entityList.size());
        mAdapter.loadHistoryList(entityList);
    }

    /**
     * 滑动到列表底部
     */
    private void scrollToBottomListItem() {
        logger.d("chat_activity#scrollToBottomListItem");
        // TODO: 2018/9/4 why use the last one index + 2 can real scroll to the bottom
        mListView.setSelection(mAdapter.getCount() + 1);
        mNewMsgTipView.setVisibility(View.GONE);
    }

    /**
     * 录音结束后处理录音数据
     *
     * @param audioLen
     */
    private void onRecordVoiceEnd(float audioLen) {
        logger.d("chat_activity#chat#audio#onRecordVoiceEnd audioLen:%f", audioLen);
        AudioMessage audioMessage = AudioMessage.buildForSend(audioLen, mAudioSavePath, mLoginUser, mPeerEntity);
        imService.getMessageManager().sendVoice(audioMessage);
    }

    /**
     * 根据分贝值设置录音时的音量动画
     *
     * @param voiceValue
     */
    private void onReceiveMaxVolume(int voiceValue) {
        if (voiceValue < 200.0) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_01);
        } else if (voiceValue > 200.0 && voiceValue < 600) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_02);
        } else if (voiceValue > 600.0 && voiceValue < 1200) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_03);
        } else if (voiceValue > 1200.0 && voiceValue < 2400) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_04);
        } else if (voiceValue > 2400.0 && voiceValue < 10000) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_05);
        } else if (voiceValue > 10000.0 && voiceValue < 28000.0) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_06);
        } else if (voiceValue > 28000.0) {
            mSoundVolumeView.setImageResource(R.mipmap.im_sound_volume_07);
        }
    }

    private void doFinishRecordAudio() {
        try {
            if (mAudioRecordHandler.isRecording()) {
                mAudioRecordHandler.setRecording(false);
            }
            if (mSoundVolumeDialog.isShowing()) {
                mSoundVolumeDialog.dismiss();
            }

            mRecordAudioView.setBackgroundResource(R.mipmap.im_pannel_btn_voice_forward_normal);

            mAudioRecordHandler.setRecordTime(SysConstant.MAX_SOUND_RECORD_TIME);
            onRecordVoiceEnd(SysConstant.MAX_SOUND_RECORD_TIME);
        } catch (Exception e) {
        }
    }

    /**
     * 肯定是在当前的session内
     *
     * @param entity
     */
    private void onMsgRecv(MessageEntity entity) {
        logger.d("chat_activity#onMsgRecv");
        imService.getUnReadMsgManager().ackReadMsg(entity);
        logger.d("chat#start pushList");
        pushList(entity);
        if (mLastItemPosition < mAdapter.getCount()) {
            mNewMsgTipView.setVisibility(View.VISIBLE);
        } else {
            scrollToBottomListItem();
        }
    }

    private void handleUnreadMsgs() {
        // 清除未读消息
        UnreadEntity unreadEntity = imService.getUnReadMsgManager().findUnread(mPeerEntity.getSessionKey());
        if (null == unreadEntity) {
            return;
        }
        int unReadCnt = unreadEntity.getUnReadCount();
        if (unReadCnt > 0) {
            imService.getNotificationManager().cancelSessionNotifications(mPeerEntity.getSessionKey());
            mAdapter.notifyDataSetChanged();
            scrollToBottomListItem();
        }
    }

    /**
     * [备注] DB保存，与session的更新manager已经做了
     *
     * @param messageEntity
     */
    private void onMsgAck(MessageEntity messageEntity) {
        logger.d("chat_activity#onMsgAck");
        int msgId = messageEntity.getMsgId();
        logger.d("chat#onMsgAck, msgId:%d", msgId);

        // 到底采用哪种ID呐??
        long localId = messageEntity.getId();
        mAdapter.updateItemState(messageEntity);
    }

    private void onMsgUnAckTimeoutOrFailure(MessageEntity messageEntity) {
        logger.d("chat#onMsgUnAckTimeoutOrFailure, msgId:%s", messageEntity.getMsgId());
        // msgId 应该还是为0
        mAdapter.updateItemState(messageEntity);
    }

    private class SwitchInputMethodReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.INPUT_METHOD_CHANGED")) {
                mCurrentInputMethod = Settings.Secure.getString(ChatActivity.this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                SystemConfigSp.getInstance().setStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD, mCurrentInputMethod);
                int height = SystemConfigSp.getInstance().getIntConfig(mCurrentInputMethod);
                if (mKeyboardHeight != height) {
                    mKeyboardHeight = height;
                    mAddOthersPanelView.setVisibility(View.GONE);
                    mEmoLayout.setVisibility(View.GONE);
                    ChatActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    mEditMsgView.requestFocus();
                    if (mKeyboardHeight != 0 && mAddOthersPanelView.getLayoutParams().height != mKeyboardHeight) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAddOthersPanelView.getLayoutParams();
                        params.height = mKeyboardHeight;
                    }
                    if (mKeyboardHeight != 0 && mEmoLayout.getLayoutParams().height != mKeyboardHeight) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEmoLayout.getLayoutParams();
                        params.height = mKeyboardHeight;
                    }
                } else {
                    mAddOthersPanelView.setVisibility(View.VISIBLE);
                    mEmoLayout.setVisibility(View.VISIBLE);
                    ChatActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    mEditMsgView.requestFocus();
                }
            }
        }
    }

    private void handleImagePickData(List<String> list) {
        ArrayList<ImageMessage> listMsg = new ArrayList<>();
        for (String item : list) {
            ImageMessage imageMessage = ImageMessage.buildForSend(item, mLoginUser, mPeerEntity);
            listMsg.add(imageMessage);
            pushList(imageMessage);
        }
        imService.getMessageManager().sendImages(listMsg);
    }

    /**
     * @param data
     * @Description 处理拍照后的数据
     * 应该是从某个 activity回来的
     */
    private void handleTakePhotoData(Intent data) {
        ImageMessage imageMessage = ImageMessage.buildForSend(mTakePhotoSavePath, mLoginUser, mPeerEntity);
        List<ImageMessage> sendList = new ArrayList<>(1);
        sendList.add(imageMessage);
        imService.getMessageManager().sendImages(sendList);
        // 格式有些问题
        pushList(imageMessage);
        mEditMsgView.clearFocus();//消除焦点
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
            case SysConstant.CAMERA_WITH_DATA:
                handleTakePhotoData(data);
                break;
            case ImageSelectorActivity.REQUEST_IMAGE:
                ArrayList<String> images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
                if (images != null && !images.isEmpty()) {
                    handleImagePickData(images);
                }
                break;
        }
    }

    public static class ChatAdapter extends BaseAdapter {

        private Logger logger = Logger.getLogger(ChatAdapter.class);
        private ArrayList<Object> mMsgObjectList = new ArrayList<>();
        private Context mContext;

        /**
         * 依赖整体session状态的
         */
        private UserEntity loginUser;
        private IMService imService;

        public ChatAdapter(Context context) {
            mContext = context;
        }

        /**
         * 添加历史消息
         *
         * @param msg
         */
        public void addItem(MessageEntity msg) {
            int nextTime = msg.getCreated();
            if (getCount() > 0) {
                Object object = mMsgObjectList.get(getCount() - 1);
                if (object instanceof MessageEntity) {
                    int preTime = ((MessageEntity) object).getCreated();
                    boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                    if (needTime) {
                        Integer in = nextTime;
                        mMsgObjectList.add(in);
                    }
                }
            } else {
                Integer in = msg.getCreated();
                mMsgObjectList.add(in);
            }

            mMsgObjectList.add(msg);
            if (msg instanceof ImageMessage) {
                ImageMessage.addToImageMessageList((ImageMessage) msg);
            }
            notifyDataSetChanged();
        }

        public MessageEntity getTopMsgEntity() {
            if (mMsgObjectList.size() <= 0) {
                return null;
            }

            for (Object result : mMsgObjectList) {
                if (result instanceof MessageEntity) {
                    return (MessageEntity) result;
                }
            }
            return null;
        }

        /**
         * 下拉载入历史消息，从最上面开始添加
         *
         * @param historyList
         */
        public void loadHistoryList(List<MessageEntity> historyList) {
            logger.d("#chatAdapter#loadHistoryList");
            if (null == historyList || historyList.isEmpty()) {
                return;
            }

            Collections.sort(historyList, new MessageTimeComparator());
            ArrayList<Object> chatList = new ArrayList<>();
            int preTime = 0;
            int nextTime = 0;
            for (MessageEntity msg : historyList) {
                nextTime = msg.getCreated();
                boolean needTime = preTime == 0 || DateUtil.needDisplayTime(preTime, nextTime);
                if (needTime) {
                    Integer in = nextTime;
                    chatList.add(in);
                }
                preTime = nextTime;
                chatList.add(msg);
            }

            // 如果是历史消息，从头开始加
            mMsgObjectList.addAll(0, chatList);
            getImageList();
            logger.d("#chatAdapter#addItem");
            notifyDataSetChanged();
        }

        /**
         * 获取图片消息列表
         */
        private void getImageList() {
            for (int i = mMsgObjectList.size() - 1; i >= 0; --i) {
                Object item = mMsgObjectList.get(i);
                if (item instanceof ImageMessage) {
                    ImageMessage.addToImageMessageList((ImageMessage) item);
                }
            }
        }

        /**
         * 临时处理，一定要干掉
         */
        public void hidePopup() {

        }

        public void clearItem() {
            mMsgObjectList.clear();
        }

        /**
         * msgId 是消息ID
         * localId是本地的ID
         * position 是list的位置
         * <p>
         * 只更新item的状态
         * 刷新单条记录
         *
         * @param position
         * @param messageEntity
         */
        public void updateItemState(int position, MessageEntity messageEntity) {
            //更新DB
            //更新单条记录
            imService.getDbInterface().insertOrUpdateMessage(messageEntity);
            notifyDataSetChanged();
        }

        /**
         * 对于混合消息的特殊处理
         *
         * @param messageEntity
         */
        public void updateItemState(final MessageEntity messageEntity) {
            long dbId = messageEntity.getId();
            int msgId = messageEntity.getMsgId();
            int len = mMsgObjectList.size();
            for (int index = len - 1; index > 0; index--) {
                Object object = mMsgObjectList.get(index);
                if (object instanceof MessageEntity) {
                    MessageEntity entity = (MessageEntity) object;
                    if (object instanceof ImageMessage) {
                        ImageMessage.addToImageMessageList((ImageMessage) object);
                    }
                    if (entity.getId() == dbId && entity.getMsgId() == msgId) {
                        mMsgObjectList.set(index, messageEntity);
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return null == mMsgObjectList ? 0 : mMsgObjectList.size();
        }

        @Override
        public int getViewTypeCount() {
            return RenderType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            try {
                /**默认是失败类型*/
                RenderType type = RenderType.MESSAGE_TYPE_INVALID;

                Object obj = mMsgObjectList.get(position);
                if (obj instanceof Integer) {
                    type = RenderType.MESSAGE_TYPE_TIME_TITLE;
                } else if (obj instanceof MessageEntity) {
                    MessageEntity info = (MessageEntity) obj;
                    boolean isMine = info.getFromId() == loginUser.getPeerId();
                    int displayType = info.getDisplayType();
                    switch (displayType) {
                        case DBConstant.SHOW_AUDIO_TYPE:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_AUDIO
                                    : RenderType.MESSAGE_TYPE_OTHER_AUDIO;
                            break;
                        case DBConstant.SHOW_IMAGE_TYPE:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_IMAGE
                                    : RenderType.MESSAGE_TYPE_OTHER_IMAGE;
                            break;
                        case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_TEXT
                                    : RenderType.MESSAGE_TYPE_OTHER_TEXT;
                            break;
                        default:
                            break;
                    }
                }
                return type.ordinal();
            } catch (Exception e) {
                logger.e(e.getMessage());
                return RenderType.MESSAGE_TYPE_INVALID.ordinal();
            }
        }

        @Override
        public Object getItem(int position) {
            if (position >= getCount() || position < 0) {
                return null;
            }
            return mMsgObjectList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                final int typeIndex = getItemViewType(position);
                RenderType renderType = RenderType.values()[typeIndex];
                // 改用map的形式
                switch (renderType) {
                    case MESSAGE_TYPE_INVALID:
                        // 直接返回
                        logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                        break;

                    case MESSAGE_TYPE_TIME_TITLE:
                        convertView = timeBubbleRender(position, convertView, parent);
                        break;

                    case MESSAGE_TYPE_MINE_AUDIO:
                        convertView = audioMsgRender(position, convertView, parent, true);
                        break;
                    case MESSAGE_TYPE_OTHER_AUDIO:
                        convertView = audioMsgRender(position, convertView, parent, false);
                        break;
                    case MESSAGE_TYPE_MINE_IMAGE:
                        convertView = imageMsgRender(position, convertView, parent, true);
                        break;
                    case MESSAGE_TYPE_OTHER_IMAGE:
                        convertView = imageMsgRender(position, convertView, parent, false);
                        break;
                    case MESSAGE_TYPE_MINE_TEXT:
                        convertView = textMsgRender(position, convertView, parent, true);
                        break;
                    case MESSAGE_TYPE_OTHER_TEXT:
                        convertView = textMsgRender(position, convertView, parent, false);
                        break;
                }
                return convertView;
            } catch (Exception e) {
                logger.e("chat#%s", e);
                return null;
            }
        }

        private View timeBubbleRender(int position, View convertView, ViewGroup parent) {
            TimeRenderView timeRenderView;
            Integer timeBubble = (Integer) mMsgObjectList.get(position);
            if (null == convertView) {
                timeRenderView = TimeRenderView.inflater(mContext, parent);
            } else {
                // 不用再使用tag 标签了
                timeRenderView = (TimeRenderView) convertView;
            }
            timeRenderView.setTime(timeBubble);
            return timeRenderView;
        }

        /**
         * text类型的:
         * 1. 设定内容Emoparser
         * 2. 点击事件  单击跳转、 双击方法、长按pop menu、点击头像的事件 跳转
         *
         * @param position
         * @param convertView
         * @param viewGroup
         * @param isMine
         * @return
         */
        private View textMsgRender(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
            TextRenderView textRenderView;
            final TextMessage textMessage = (TextMessage) mMsgObjectList.get(position);
            UserEntity userEntity = imService.getContactManager().findContact(textMessage.getFromId());

            if (null == convertView) {
                textRenderView = TextRenderView.inflater(mContext, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
            } else {
                textRenderView = (TextRenderView) convertView;
            }

            TextView msgContentView = textRenderView.getMsgContentView();
            // 失败事件添加
            textRenderView.getMessageFailedView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
//                    popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE, true, isMine);
                    textMessage.setStatus(MessageConstant.MSG_SENDING);
                    mMsgObjectList.remove(position);
                    addItem(textMessage);
                    if (imService != null) {
                        imService.getMessageManager().resendMessage(textMessage);
                    }
                }
            });

            msgContentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 弹窗类型
//                    MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
//                    boolean bResend = textMessage.getStatus() == MessageConstant.MSG_FAILURE;
//                    popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE, bResend, isMine);
                    return true;
                }
            });

            // url路径可以设定跳转
            final String content = textMessage.getContent();
            msgContentView.setOnTouchListener(new OnDoubleClickListener() {
                @Override
                public void onClick(View view) {
                    //todo
                }

                @Override
                public void onDoubleClick(View view) {
                    PreviewTextActivity.launch(mContext, content);
                }
            });
            textRenderView.render(textMessage, userEntity, mContext);
            return textRenderView;
        }

        /**
         * 头像事件
         * mine:事件 other事件
         * 图片的状态  消息收到，没收到，图片展示成功，没有成功
         * 触发图片的事件  【长按】
         * 图片消息类型的render
         *
         * @param position
         * @param convertView
         * @param parent
         * @param isMine
         */
        private View imageMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
            ImageRenderView imageRenderView;
            final ImageMessage imageMessage = (ImageMessage) mMsgObjectList.get(position);
            UserEntity userEntity = imService.getContactManager().findContact(imageMessage.getFromId());

            // 保存在本地的path
            final String imagePath = imageMessage.getPath();
            // 消息中的image路径
            final String imageUrl = imageMessage.getUrl();

            if (null == convertView) {
                imageRenderView = ImageRenderView.inflater(mContext, parent, isMine);
            } else {
                imageRenderView = (ImageRenderView) convertView;
            }

            ImageView msgImage = imageRenderView.getMsgImage();
            final int msgId = imageMessage.getMsgId();
            // 失败事件添加
            imageRenderView.getMessageFailedView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // 重发或者重新加载
//                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
//                    popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, true, isMine);

                }
            });

            imageRenderView.setBtnImageListener(new ImageRenderView.BtnImageListener() {
                @Override
                public void onMsgSuccess() {
                    // TODO: 2018/9/3 跳转图片预览界面
                }

                @Override
                public void onMsgFailure() {
                    /**
                     * 多端同步也不会拉到本地失败的数据
                     * 只有isMine才有的状态，消息发送失败
                     * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
                     * 2. 图片上传成功，但是发送失败。 点击重新发送??
                     */
                    if (FileUtil.isSdCardAvailuable()) {
                        imageMessage.setStatus(MessageConstant.MSG_SENDING);
                        if (imService != null) {
                            imService.getMessageManager().resendMessage(imageMessage);
                        }
                        updateItemState(msgId, imageMessage);
                    } else {
                        ViewUtil.showMessage("请检查存储卡是否可用");
                    }
                }
            });

            imageRenderView.setImageLoadListener(new ImageRenderView.ImageLoadListener() {
                @Override
                public void onLoadComplete(String localPath) {
                    logger.d("chat#pic#save image ok");
                    logger.d("pic#setsavepath:%s", localPath);
                    imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                    updateItemState(imageMessage);
                }

                @Override
                public void onLoadFailed() {
                    logger.d("chat#pic#onBitmapFailed");
                    imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                    updateItemState(imageMessage);
                    logger.d("download failed");
                }
            });

            final View msgLayout = imageRenderView.getMsgLayout();
            msgImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
//                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
//                    boolean bResend = (imageMessage.getStatus() == MessageConstant.MSG_FAILURE)
//                            || (imageMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);
//                    popup.show(msgLayout, DBConstant.SHOW_IMAGE_TYPE, bResend, isMine);
                    return true;
                }
            });

            imageRenderView.render(imageMessage, userEntity, mContext);
            return imageRenderView;
        }

        private View audioMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
            AudioRenderView audioRenderView;
            final AudioMessage audioMessage = (AudioMessage) mMsgObjectList.get(position);
            UserEntity userEntity = imService.getContactManager().findContact(audioMessage.getFromId());

            if (null == convertView) {
                audioRenderView = AudioRenderView.inflater(mContext, parent, isMine);
            } else {
                audioRenderView = (AudioRenderView) convertView;
            }

            final String audioPath = audioMessage.getAudioPath();
            final View msgLayout = audioRenderView.getMsgLayout();
            if (!TextUtils.isEmpty(audioPath)) {
                // 播放的路径为空,这个消息应该如何展示
                msgLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
//                        MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
//                        boolean bResend = audioMessage.getStatus() == MessageConstant.MSG_FAILURE;
//                        popup.show(messageLayout, DBConstant.SHOW_AUDIO_TYPE, bResend, isMine);
                        return true;
                    }
                });
            }

            audioRenderView.getMessageFailedView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
//                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
//                    popup.show(messageLayout, DBConstant.SHOW_AUDIO_TYPE, true, isMine);
                }
            });

            audioRenderView.setBtnImageListener(new AudioRenderView.BtnImageListener() {
                @Override
                public void onClickUnread() {
                    logger.d("chat#audio#set audio meessage read status");
                    audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
                    imService.getDbInterface().insertOrUpdateMessage(audioMessage);
                }

                @Override
                public void onClickReaded() {

                }
            });

            audioRenderView.render(audioMessage, userEntity, mContext);
            return audioRenderView;
        }

        /**
         * init的时候需要设定
         *
         * @param imService
         * @param loginUser
         */
        public void setImService(IMService imService, UserEntity loginUser) {
            this.imService = imService;
            this.loginUser = loginUser;
        }

        public static class MessageTimeComparator implements Comparator<MessageEntity> {

            @Override
            public int compare(MessageEntity m1, MessageEntity m2) {
                if (m1.getCreated() == m2.getCreated()) {
                    return m1.getMsgId() - m2.getMsgId();
                }
                return m1.getCreated() - m2.getCreated();
            }
        }
    }
}
