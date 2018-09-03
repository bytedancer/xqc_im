package com.bonade.xxp.xqc_android_im.ui.widget.message;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.AudioMessage;
import com.bonade.xxp.xqc_android_im.ui.helper.AudioPlayerHandler;
import com.bonade.xxp.xqc_android_im.util.CommonUtil;
import com.bonade.xxp.xqc_android_im.util.ViewUtil;

import org.w3c.dom.Text;

import java.io.File;

public class AudioRenderView extends BaseMsgRenderView {

    // 可点击的消息体
    private View msgLayout;

    // 播放动画的view
    private View audioAnttView;

    // 指示语音是否被播放
    private View audioUnreadNotify;

    // 语音时长
    private TextView audioDuration;

    // 对外暴露的监听器，点击
    private BtnImageListener btnImageListener;

    public interface BtnImageListener {

        void  onClickUnread();
        void  onClickReaded();
    }

    public void setBtnImageListener(BtnImageListener btnImageListener){
        this.btnImageListener = btnImageListener;
    }

    public AudioRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        msgLayout = findViewById(R.id.message_layout);
        audioAnttView = findViewById(R.id.view_audio_antt);
        audioDuration = findViewById(R.id.tv_audio_duration);
        audioUnreadNotify = findViewById(R.id.view_audio_unread_notify);
    }

//    public static AudioRenderView inflater(Context context, boolean isMine) {
//        int resoure = isMine ? R.layout.item_mine_audio_message:R.layout.item_other_audio_message;
//        AudioRenderView audioRenderView = (AudioRenderView) LayoutInflater.from(context).inflate(resoure, null);
//        audioRenderView.setMine(isMine);
//        return audioRenderView;
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        msgLayout = findViewById(R.id.message_layout);
//        audioAnttView = findViewById(R.id.view_audio_antt);
//        audioDuration = findViewById(R.id.tv_audio_duration);
//        audioUnreadNotify = findViewById(R.id.view_audio_unread_notify);
//    }

    /**
     * 控件赋值
     * 是不是采用callback的形式
     *
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(@NonNull MessageEntity messageEntity, @NonNull UserEntity userEntity, Context context) {
        super.render(messageEntity, userEntity, context);
        final AudioMessage audioMessage = (AudioMessage)messageEntity;

        final String audioPath = audioMessage.getAudioPath();
        final int audioReadStatus = audioMessage.getReadStatus();

        msgLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 上层回调事件
                if (!new File(audioPath).exists()) {
                    ViewUtil.showMessage("该语音文件已丢失");
                    return;
                }

                switch (audioReadStatus) {
                    case MessageConstant.AUDIO_UNREAD:
                        if(btnImageListener != null){
                            btnImageListener.onClickUnread();
                            audioUnreadNotify.setVisibility(View.GONE);
                        }
                        break;
                    case MessageConstant.AUDIO_READED:
                        if(btnImageListener != null){
                            btnImageListener.onClickReaded();
                        }
                        break;
                }

                // 获取播放路径，播放语音
                String currentPlayPath = AudioPlayerHandler.getInstance().getCurrentPlayPath();
                if (currentPlayPath != null && AudioPlayerHandler.getInstance().isPlaying()) {
                    AudioPlayerHandler.getInstance().stopPlayer();
                    if (currentPlayPath.equals(audioPath)) {
                        // 关闭当前的
                        return;
                    }
                }

                final AnimationDrawable animationDrawable = (AnimationDrawable) audioAnttView.getBackground();
                AudioPlayerHandler.getInstance().setAudioListener(new AudioPlayerHandler.AudioListener() {
                    @Override
                    public void onStop() {
                        if(animationDrawable!=null && animationDrawable.isRunning()){
                            animationDrawable.stop();
                            animationDrawable.selectDrawable(0);
                        }
                    }
                });

                // 延迟播放
                Thread myThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                            AudioPlayerHandler.getInstance().startPlay(audioPath);
                            animationDrawable.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                myThread.start();
            }
        });

        // 针对path的设定
        if (null != audioPath) {
            int resource = isMine ? R.drawable.voice_play_mine:R.drawable.voice_play_other;
            audioAnttView.setBackgroundResource(resource);
            AnimationDrawable animationDrawable = (AnimationDrawable) audioAnttView.getBackground();

            if (null != AudioPlayerHandler.getInstance().getCurrentPlayPath()
                    && AudioPlayerHandler.getInstance().getCurrentPlayPath().equals(audioPath)) {
                animationDrawable.start();
            } else {
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                    animationDrawable.selectDrawable(0);
                }
            }

            switch (audioReadStatus) {
                case MessageConstant.AUDIO_READED:
                    audioAlreadyRead();
                    break;
                case MessageConstant.AUDIO_UNREAD:
                    audioUnread();
                    break;
            }

            int audioLength = audioMessage.getAudiolength();
            audioDuration.setText(String.valueOf(audioLength) + '"');
            // messageLayout 的长按事件绑定 在上层做掉，有时间了再迁移

            // getAudioBkSize, 在看一下
            int width = CommonUtil.getAudioBkSize(audioLength, context);
            if (width < CommonUtil.dip2px(context, 45)) {
                width = CommonUtil.dip2px(context, 45);
            }
            RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
            msgLayout.setLayoutParams(layoutParam);
            if (isMine) {
                layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.tv_audio_duration);
                RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) audioDuration.getLayoutParams();
                param.addRule(RelativeLayout.RIGHT_OF, R.id.iv_message_state_failed);
                param.addRule(RelativeLayout.RIGHT_OF, R.id.pb_loading);
            }
        }
    }

    public void startAnimation() {
        AnimationDrawable animationDrawable = (AnimationDrawable) audioAnttView.getBackground();
        animationDrawable.start();
    }

    public void stopAnimation(){
        AnimationDrawable animationDrawable = (AnimationDrawable) audioAnttView.getBackground();
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
            animationDrawable.selectDrawable(0);
        }
    }

    /**
     * unread与alreadRead的区别是什么
     */
    private void audioUnread() {
        if(isMine){
            audioUnreadNotify.setVisibility(View.GONE);
        }else{
            audioUnreadNotify.setVisibility(View.VISIBLE);
        }
    }

    private void  audioAlreadyRead(){
        audioUnreadNotify.setVisibility(View.GONE);
    }

    public View getMsgLayout() {
        return msgLayout;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

}
