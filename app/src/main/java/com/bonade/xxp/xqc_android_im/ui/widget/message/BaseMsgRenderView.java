package com.bonade.xxp.xqc_android_im.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.imservice.manager.IMLoginManager;
import com.bonade.xxp.xqc_android_im.ui.activity.FriendInfoActivity;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class BaseMsgRenderView extends RelativeLayout {

    /**
     * 头像
     */
    protected ImageView avatarView;
    /**
     * 消息状态
     */
    protected ImageView messageFailedView;
    protected ProgressBar loadingView;
    protected TextView nameView;

    /**
     * 渲染的消息实体
     */
    protected MessageEntity messageEntity;
    protected ViewGroup parentView;
    protected boolean isMine;

    private RequestManager mRequestManager;
    private Transformation mTransformation;

    public BaseMsgRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRequestManager = Glide.with(context);
        mTransformation = new CropCircleTransformation(Glide.get(context).getBitmapPool());
    }

    /**
     * 渲染之后做的事情，子类会调用到这个地方吗？
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        avatarView = findViewById(R.id.iv_avatar);
        messageFailedView = findViewById(R.id.iv_message_state_failed);
        loadingView = findViewById(R.id.pb_loading);
        nameView = findViewById(R.id.tv_name);
    }

    /**
     * 消息失败绑定事件 三种不同的弹窗
     * image的load状态就是 sending状态的一个子状态
     */

    /**
     * 发送中
     */
    public void msgSendinging(MessageEntity entity) {
        messageFailedView.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.VISIBLE);
    }

    /**
     * 发送失败
     */
    public void msgFailure(MessageEntity entity){
        messageFailedView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    /**
     * 发送成功
     */
    public void msgSuccess(MessageEntity entity){
        messageFailedView.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    /**
     * 发送发生错误
     */
    public void msgStatusError(MessageEntity entity){
        messageFailedView.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param context
     */
    public void render(@NonNull MessageEntity messageEntity, UserEntity userEntity, Context context) {
        this.messageEntity = messageEntity;

        int msgStatus = messageEntity.getStatus();
        int loginId = IMLoginManager.getInstance().getLoginId();
        if (userEntity.getPeerId() == loginId) {
            mRequestManager
                    .load(CommonUtil.getUserAvatarSavePath(loginId))
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .bitmapTransform(mTransformation)
                    .into(avatarView);
        } else {
            mRequestManager
                    .load(userEntity.getAvatar())
                    .error(R.mipmap.im_default_user_avatar)
                    .placeholder(R.mipmap.im_default_user_avatar)
                    .bitmapTransform(mTransformation)
                    .into(avatarView);
        }

        // 设定姓名 应该消息都是有的
        if(!isMine){
            nameView.setText(userEntity.getMainName());
            nameView.setVisibility(View.VISIBLE);
        } else {
            // TODO: 2018/9/10 设置我的头像
        }

        // 头像的跳转暂时放在这里 todo 业务结合紧密，但是应该不会改了
        final int userId = userEntity.getPeerId();
        avatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendInfoActivity.launch(getContext(), userId);
            }
        });

        // 设定三种信息的弹框类型
        // 判定消息的状态 成功还是失败 todo 具体实现放在子类中
        switch (msgStatus) {
            case MessageConstant.MSG_FAILURE:
                msgFailure(messageEntity);
                break;
            case MessageConstant.MSG_SUCCESS:
                msgSuccess(messageEntity);
                break;
            case MessageConstant.MSG_SENDING:
                msgSendinging(messageEntity);
                break;
            default:
                msgStatusError(messageEntity);
                break;
        }

        // 如果消息还是处于loading 状态
        // 判断是否是image类型  image类型的才有 loading，其他的都GONE
        // todo 上面这些由子类做
        // 消息总体的类型有两种(我、别人)
        // 展示类型有三种(图片、文字、语音)
    }

    public ImageView getAvatarView() {
        return avatarView;
    }

    public ImageView getMessageFailedView() {
        return messageFailedView;
    }

    public ProgressBar getLoadingView() {
        return loadingView;
    }

    public TextView getNameView() {
        return nameView;
    }
}
