package com.bonade.xxp.xqc_android_im.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.ui.widget.BubbleImageView;
import com.bonade.xxp.xqc_android_im.ui.widget.IMImageProgressbar;
import com.bonade.xxp.xqc_android_im.util.FileUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;

public class ImageRenderView extends BaseMsgRenderView {

    private Logger logger = Logger.getLogger(ImageRenderView.class);

    // 上层必须实现的接口
    private ImageLoadListener imageLoadListener;
    private BtnImageListener btnImageListener;

    // 可点击的view
    private View msgLayout;
    // 图片消息体
    private BubbleImageView msgImage;
    // 图片状态指示
    private IMImageProgressbar imageProgress;

    public ImageRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static ImageRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        int resource = isMine ? R.layout.item_mine_image_message : R.layout.item_other_image_message;
        ImageRenderView imageRenderView = (ImageRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        imageRenderView.setMine(isMine);
        return imageRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        msgLayout = findViewById(R.id.message_layout);
        msgImage = findViewById(R.id.message_image);
        imageProgress = findViewById(R.id.pb_image);
        imageProgress.setShowText(false);
    }

    /**
     * 控件赋值
     * <p>
     * 对于mine。 图片send_success 就是成功了直接取地址
     * 对于sending  就是正在上传
     * <p>
     * 对于other，消息一定是success，接受成功额
     * 2. 然后分析loadStatus 判断消息的展示状态
     *
     * @param messageEntity
     */
    @Override
    public void render(@NonNull MessageEntity messageEntity, UserEntity userEntity, Context context) {
        super.render(messageEntity, userEntity, context);
    }

    @Override
    public void msgFailure(final MessageEntity entity) {
        super.msgFailure(entity);
        msgImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断状态，重新发送resend
                btnImageListener.onMsgFailure();
            }
        });
        if (FileUtil.isFileExist(((ImageMessage) entity).getPath())) {
            msgImage.setImageUrl("file://" + ((ImageMessage) entity).getPath());
        } else {
            msgImage.setImageUrl(((ImageMessage) entity).getUrl());
        }
        imageProgress.hideProgress();
    }

    @Override
    public void msgStatusError(final MessageEntity entity) {
        super.msgStatusError(entity);
        imageProgress.hideProgress();
    }

    /**
     * 图片信息正在发送的过程中
     * 1.上传图片
     * 2.发送信息
     *
     * @param entity
     */
    @Override
    public void msgSendinging(MessageEntity entity) {
        super.msgSendinging(entity);
        if (isMine()) {
            if (FileUtil.isFileExist(((ImageMessage) entity).getPath())) {
                msgImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {

                    @Override
                    public void onLoadingComplete(String imageUrl, GlideDrawable loadedImage) {

                    }

                    @Override
                    public void onLoadingStarted(String imageUrl) {

                    }

                    @Override
                    public void onLoadingCanceled(String imageUrl) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUrl) {

                    }
                });
                msgImage.setImageUrl("file://" + ((ImageMessage) entity).getPath());
            } else {
                //todo 文件不存在
            }
        }
    }

    /**
     * 消息成功
     * 1.对象图片消息
     * 2.自己多端同步的消息
     * 说明imageUrl不会为空的
     *
     * @param entity
     */
    @Override
    public void msgSuccess(MessageEntity entity) {
        super.msgSuccess(entity);
        ImageMessage imageMessage = (ImageMessage) entity;
        final String imagePath = imageMessage.getPath();
        final String url = imageMessage.getUrl();
        int loadStatus = imageMessage.getLoadStatus();
        if (TextUtils.isEmpty(url)) {
            // 消息状态异常
            msgStatusError(entity);
            return;
        }

        switch (loadStatus) {
            case MessageConstant.IMAGE_UNLOAD:
                msgImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUrl, GlideDrawable loadedImage) {
                        if (imageLoadListener != null) {
                            imageLoadListener.onLoadComplete(imageUrl);
                        }
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUrl) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUrl) {
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingFailed(String imageUrl) {
                        imageProgress.hideProgress();
                        imageLoadListener.onLoadFailed();
                    }
                });

                if (isMine()) {
                    if (FileUtil.isFileExist(imagePath)) {
                        msgImage.setImageUrl("file://" + imagePath);
                    } else {
                        msgImage.setImageUrl(url);
                    }
                } else {
                    msgImage.setImageUrl(url);
                }
                break;
            case MessageConstant.IMAGE_LOADED_SUCCESS:
                msgImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {
                    @Override
                    public void onLoadingComplete(String imageUrl, GlideDrawable loadedImage) {
                        imageProgress.hideProgress();
                    }

                    @Override
                    public void onLoadingStarted(String imageUrl) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUrl) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUrl) {
                        imageProgress.showProgress();
                    }
                });

                if (isMine()) {
                    if (FileUtil.isFileExist(imagePath)) {
                        msgImage.setImageUrl("file://" + imagePath);
                    } else {
                        msgImage.setImageUrl(url);
                    }
                } else {
                    msgImage.setImageUrl(url);
                }
                msgImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (btnImageListener != null) {
                            btnImageListener.onMsgSuccess();
                        }
                    }
                });
                break;
            //todo 图像失败了，允许点击之后重新下载
            case MessageConstant.IMAGE_LOADED_FAILURE: {
//                msgStatusError(imageMessage);
//                getImageProgress().hideProgress();
                msgImage.setImageLoadingCallback(new BubbleImageView.ImageLoadingCallback() {

                    @Override
                    public void onLoadingComplete(String imageUrl, GlideDrawable loadedImage) {
                        imageProgress.hideProgress();
                        imageLoadListener.onLoadComplete(imageUrl);
                    }

                    @Override
                    public void onLoadingStarted(String imageUrl) {
                        imageProgress.showProgress();
                    }

                    @Override
                    public void onLoadingCanceled(String imageUrl) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUrl) {

                    }
                });
                msgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        msgImage.setImageUrl(url);
                    }
                });
            }
            break;

        }
    }

    public interface ImageLoadListener {

        void onLoadComplete(String path);

        // 应该把exception 返回结构放进去
        void onLoadFailed();
    }

    public void setImageLoadListener(ImageLoadListener imageLoadListener) {
        this.imageLoadListener = imageLoadListener;
    }

    public interface BtnImageListener {

        void onMsgSuccess();

        void onMsgFailure();
    }

    public void setBtnImageListener(BtnImageListener btnImageListener) {
        this.btnImageListener = btnImageListener;
    }

    public View getMsgLayout() {
        return msgLayout;
    }

    public ImageView getMsgImage() {
        return msgImage;
    }

    public IMImageProgressbar getImageProgress() {
        return imageProgress;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
}
