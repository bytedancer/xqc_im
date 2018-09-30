package com.bonade.xxp.xqc_android_im.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bonade.xxp.xqc_android_im.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

public class BubbleImageView extends AppCompatImageView {

    /**
     * 图片相关设置
     */
    protected String imageUrl = null;
    protected boolean isAttachedOnWindow = false;

    protected ImageLoadingCallback imageLoadingCallback;
    private RequestManager requestManager;

    public BubbleImageView(Context context) {
        super(context);
        init(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        requestManager = Glide.with(context);
    }

    public void setImageLoadingCallback(ImageLoadingCallback imageLoadingCallback) {
        this.imageLoadingCallback = imageLoadingCallback;
    }

    public void setImageUrl(final String url) {
        imageUrl = url;
        if (isAttachedOnWindow) {
            if (TextUtils.isEmpty(imageUrl)) {
                setImageResource(R.mipmap.im_message_image_default);
                return;
            }

            if (imageUrl.startsWith("/")) {
                File file = new File(imageUrl);
                if (!file.exists()) {
                    setImageResource(R.mipmap.im_message_image_default);
                    return;
                }
            }
            requestManager
                    .load(imageUrl.startsWith("/") ? new File(imageUrl) : imageUrl)
                    .error(R.mipmap.im_message_image_default)
                    .placeholder(R.mipmap.im_message_image_default)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            setImageDrawable(resource);
                            if (imageLoadingCallback != null) {
                                imageLoadingCallback.onLoadingComplete(url, resource);
                            }
                        }

                        @Override
                        public void onLoadStarted(Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            if (imageLoadingCallback != null) {
                                imageLoadingCallback.onLoadingStarted(url);
                            }
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            if (imageLoadingCallback != null) {
                                imageLoadingCallback.onLoadingFailed(url);
                            }
                        }

                        @Override
                        public void onStop() {
                            super.onStop();
                            if (imageLoadingCallback != null) {
                                imageLoadingCallback.onLoadingCanceled(url);
                            }

                        }
                    });
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow = false;
    }

    public interface ImageLoadingCallback {

        void onLoadingComplete(String imageUrl, GlideDrawable loadedImage);

        void onLoadingStarted(String imageUrl);

        void onLoadingCanceled(String imageUrl);

        void onLoadingFailed(String imageUrl);
    }
}
