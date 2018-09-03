package com.bonade.xxp.xqc_android_im.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.AudioMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.imservice.entity.TextMessage;
import com.bonade.xxp.xqc_android_im.imservice.service.IMService;
import com.bonade.xxp.xqc_android_im.ui.base.BaseActivity;
import com.bonade.xxp.xqc_android_im.ui.helper.AudioRecordHandler;
import com.bonade.xxp.xqc_android_im.ui.helper.listener.OnDoubleClickListener;
import com.bonade.xxp.xqc_android_im.ui.widget.CustomEditView;
import com.bonade.xxp.xqc_android_im.ui.widget.EmoGridView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.AudioRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.ImageRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.RenderType;
import com.bonade.xxp.xqc_android_im.ui.widget.message.TextRenderView;
import com.bonade.xxp.xqc_android_im.ui.widget.message.TimeRenderView;
import com.bonade.xxp.xqc_android_im.util.DateUtil;
import com.bonade.xxp.xqc_android_im.util.FileUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

/**
 * 聊天界面
 */
public class ChatActivity extends BaseActivity {

    public static void launch(Activity from, String sessionKey) {
        Intent intent = new Intent(from, ChatActivity.class);
        intent.putExtra(SESSION_KEY, sessionKey);
        from.startActivity(intent);
    }

    public static final String SESSION_KEY = "SESSION_KEY";
    // 处理语音
    private static Handler uiHandler = null;

    @BindView(R.id.xrefreshview)
    SmartRefreshLayout xRefreshView;

    @BindView(R.id.rv_msg)
    RecyclerView mRecyclerView;

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
    private TextView mNewMsgTipView;

    private String mAudioSavePath;
    private InputMethodManager mInputMethodManager;
    private AudioRecordHandler mAudioRecordHandler;


    private String mSessionKey;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void setupViews(Bundle savedInstanceState) {
        mSessionKey = getIntent().getStringExtra(SESSION_KEY);
    }

    public static class ChatAdapter extends BaseQuickAdapter<Object, BaseViewHolder> {

        private Logger logger = Logger.getLogger(ChatAdapter.class);

        private Context mContext;

        /**
         * 依赖整体session状态的
         */
        private UserEntity loginUser;
        private IMService imService;

        public ChatAdapter(Context context, @Nullable List<Object> data) {
            super(data);
            mContext = context;

            setMultiTypeDelegate(new MultiTypeDelegate<Object>() {

                @Override
                protected int getItemType(Object object) {
                    try {
                        // 默认是失败类型
                        RenderType type = RenderType.MESSAGE_TYPE_INVALID;
                        if (object instanceof Integer) {
                            type = RenderType.MESSAGE_TYPE_TIME_TITLE;
                        } else if (object instanceof MessageEntity) {
                            MessageEntity info = (MessageEntity) object;
                            boolean isMine = info.getFromId() == loginUser.getPeerId();
                            switch (info.getDisplayType()) {
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
            });

            getMultiTypeDelegate()
                    .registerItemType(RenderType.MESSAGE_TYPE_INVALID.ordinal(), 0)
                    .registerItemType(RenderType.MESSAGE_TYPE_MINE_TEXT.ordinal(), R.layout.item_mine_text_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_MINE_IMAGE.ordinal(), R.layout.item_mine_image_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_MINE_AUDIO.ordinal(), R.layout.item_mine_audio_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_OTHER_TEXT.ordinal(), R.layout.item_other_text_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_OTHER_IMAGE.ordinal(), R.layout.item_other_image_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_OTHER_AUDIO.ordinal(), R.layout.item_other_audio_message)
                    .registerItemType(RenderType.MESSAGE_TYPE_TIME_TITLE.ordinal(), R.layout.item_message_title_time);
        }

        @Override
        protected void convert(BaseViewHolder helper, Object item) {
            try {
                final int typeIndex = helper.getItemViewType();
                RenderType renderType = RenderType.values()[typeIndex];
                View rootView = helper.getView(R.id.root_layout);
                switch (renderType) {
                    case MESSAGE_TYPE_INVALID:
                        // 直接返回
                        logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                        break;
                    case MESSAGE_TYPE_TIME_TITLE:
                        timeBubbleRender(rootView, helper, item);
                        break;
                    case MESSAGE_TYPE_MINE_TEXT:
                        textMsgRender(rootView, helper, item, true);
                        break;
                    case MESSAGE_TYPE_MINE_IMAGE:
                        imageMsgRender(rootView, helper, item, true);
                        break;
                    case MESSAGE_TYPE_MINE_AUDIO:
                        audioMsgRender(rootView, helper, item, true);
                        break;
                    case MESSAGE_TYPE_OTHER_TEXT:
                        textMsgRender(rootView, helper, item, false);
                        break;
                    case MESSAGE_TYPE_OTHER_IMAGE:
                        imageMsgRender(rootView, helper, item, false);
                        break;
                    case MESSAGE_TYPE_OTHER_AUDIO:
                        audioMsgRender(rootView, helper, item, false);
                        break;

                }
            } catch (Exception e) {
                logger.e("chat#%s", e);
            }
        }

        private void timeBubbleRender(View rootView, BaseViewHolder helper, Object item) {
            TimeRenderView timeRenderView = (TimeRenderView) rootView;
            Integer timeBubble = (Integer) item;
            timeRenderView.setTime(timeBubble);
        }

        /**
         * text类型的:
         * 1. 设定内容Emoparser
         * 2. 点击事件  单击跳转、 双击方法、长按pop menu、点击头像的事件 跳转
         * @param rootView
         * @param helper
         * @param item
         * @param isMine
         */
        private void textMsgRender(View rootView, BaseViewHolder helper, Object item, final boolean isMine) {
            TextRenderView textRenderView = (TextRenderView) rootView;
            TextMessage textMessage = (TextMessage) item;
            UserEntity userEntity = imService.getContactManager().findContact(textMessage.getFromId());
            TextView msgContentView = textRenderView.getMsgContentView();

            // 失败事件添加
            textRenderView.getMessageFailedView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
//                    popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE, true, isMine);
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
            textRenderView.setMine(isMine);
            textRenderView.render(textMessage, userEntity, mContext);
        }

        /**
         * 头像事件
         * mine:事件 other事件
         * 图片的状态  消息收到，没收到，图片展示成功，没有成功
         * 触发图片的事件  【长按】
         * 图片消息类型的render
         * @param rootView
         * @param helper
         * @param item
         * @param isMine
         */
        private void imageMsgRender(View rootView, BaseViewHolder helper, Object item, final boolean isMine) {
            ImageRenderView imageRenderView = (ImageRenderView) rootView;
            final ImageMessage imageMessage = (ImageMessage) item;
            UserEntity userEntity = imService.getContactManager().findContact(imageMessage.getFromId());

            // 保存在本地的path
            final String imagePath = imageMessage.getPath();
            // 消息中的image路径
            final String imageUrl = imageMessage.getUrl();

            // 失败事件添加
            imageRenderView.getMessageFailedView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // 重发或者重新加载
//                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
//                    popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, true, isMine);
                }
            });

            ImageView msgImage = imageRenderView.getMsgImage();
            final int msgId = imageMessage.getMsgId();
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

            imageRenderView.setMine(isMine);
            imageRenderView.render(imageMessage, userEntity, mContext);
        }

        private void audioMsgRender(View rootView, BaseViewHolder helper, Object item, final boolean isMine) {
            AudioRenderView audioRenderView = (AudioRenderView) rootView;
            final AudioMessage audioMessage = (AudioMessage) item;
            UserEntity userEntity = imService.getContactManager().findContact(audioMessage.getFromId());

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

            audioRenderView.setMine(isMine);
            audioRenderView.render(audioMessage, userEntity, mContext);
        }

        /**
         * init的时候需要设定
         * @param imService
         * @param loginUser
         */
        public void setImService(IMService imService, UserEntity loginUser) {
            this.imService = imService;
            this.loginUser = loginUser;
        }

        /**
         * 添加历史消息
         * @param msg
         */
        public void addItem(MessageEntity msg) {
            int nextTime = msg.getCreated();
            if (getItemCount() > 0) {
                Object object = getData().get(getItemCount() - 1);
                if (object instanceof MessageEntity) {
                    int preTime = ((MessageEntity) object).getCreated();
                    boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                    if (needTime) {
                        Integer in = nextTime;
                        getData().add(in);
                    }
                }
            } else {
                Integer in = msg.getCreated();
                getData().add(in);
            }

            getData().add(msg);
            if (msg instanceof ImageMessage) {
                ImageMessage.addToImageMessageList((ImageMessage) msg);
            }
            logger.d("#chatAdapter#addItem");
            notifyDataSetChanged();
        }

        public MessageEntity getTopMsgEntity() {
            if (getData().size() <= 0) {
                return null;
            }

            for (Object result : getData()) {
                if (result instanceof MessageEntity) {
                    return (MessageEntity) result;
                }
            }

            return null;
        }

        /**
         * 下拉载入历史消息，从最上面开始添加
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
                boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                if (needTime) {
                    Integer in = nextTime;
                    chatList.add(in);
                }
                preTime = nextTime;
                chatList.add(msg);
            }

            // 如果是历史消息，从头开始加
            getData().addAll(0, chatList);
            getImageList();
            logger.d("#chatAdapter#addItem");
            notifyDataSetChanged();
        }

        /**
         * 获取图片消息列表
         */
        private void getImageList() {
            for (int i = getData().size() - 1; i >= 0; --i) {
                Object item = getData().get(i);
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
            getData().clear();
        }

        /**
         * msgId 是消息ID
         * localId是本地的ID
         * position 是list的位置
         *
         * 只更新item的状态
         * 刷新单条记录
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
         * @param messageEntity
         */
        public void updateItemState(final MessageEntity messageEntity) {
            long dbId = messageEntity.getId();
            int msgId = messageEntity.getMsgId();
            int len = getData().size();
            for (int index = len - 1; index > 0; index--) {
                Object object = getData().get(index);
                if (object instanceof MessageEntity) {
                    MessageEntity entity = (MessageEntity) object;
                    if (object instanceof ImageMessage) {
                        ImageMessage.addToImageMessageList((ImageMessage) object);
                    }
                    if (entity.getId() == dbId && entity.getMsgId() == msgId) {
                        getData().set(index, messageEntity);
                        break;
                    }
                }
            }
            notifyDataSetChanged();
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
