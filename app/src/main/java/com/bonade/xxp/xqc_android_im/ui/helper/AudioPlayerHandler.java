package com.bonade.xxp.xqc_android_im.ui.helper;

import android.content.Context;
import android.media.AudioManager;

import com.bonade.xxp.xqc_android_im.imservice.event.AudioEvent;
import com.bonade.xxp.xqc_android_im.imservice.support.audio.SpeexDecoder;
import com.bonade.xxp.xqc_android_im.util.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class AudioPlayerHandler {

    private String currentPlayPath = null;
    private SpeexDecoder speexdec = null;
    private Thread thread = null;

    private static AudioPlayerHandler instance = null;
    private Logger logger = Logger.getLogger(AudioPlayerHandler.class);

    public static AudioPlayerHandler getInstance() {
        if (null == instance) {
            synchronized (AudioPlayerHandler.class) {
                instance = new AudioPlayerHandler();
                EventBus.getDefault().register(instance);
            }
        }
        return instance;
    }

    private AudioPlayerHandler() {
    }

    /**
     * 语音播放的模式
     *
     * @param mode
     * @param context
     */
    public void setAudioMode(int mode, Context context) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_CALL) {
            return;
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(mode);
    }

    /**
     * messagePop调用
     *
     * @param context
     * @return
     */
    public int getAudioMode(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getMode();
    }

    public void clear() {
        if (isPlaying()) {
            stopPlayer();
        }
        EventBus.getDefault().unregister(instance);
        instance = null;
    }

    /**
     * speexdec 由于线程模型
     */
    public interface AudioListener {
        void onStop();
    }

    private AudioListener audioListener;

    public void setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    private void stopAnimation() {
        if (audioListener != null) {
            audioListener.onStop();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AudioEvent audioEvent) {
        switch (audioEvent) {
            case AUDIO_STOP_PLAY: {
                currentPlayPath = null;
                stopPlayer();
            }
            break;
        }
    }

    public void stopPlayer() {
        try {
            if (null != thread) {
                thread.interrupt();
                thread = null;
                Thread.currentThread().interrupt();
            } else {
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        } finally {
            stopAnimation();
        }
    }

    public boolean isPlaying() {
        return null != thread;
    }

    public void startPlay(String filePath) {
        this.currentPlayPath = filePath;
        try {
            speexdec = new SpeexDecoder(new File(this.currentPlayPath));
            RecordPlayThread rpt = new RecordPlayThread();
            if (null == thread)
                thread = new Thread(rpt);
            thread.start();
        } catch (Exception e) {
            // 关闭动画很多地方需要写，是不是需要重新考虑一下@yingmu
            logger.e(e.getMessage());
            stopAnimation();
        }
    }

    class RecordPlayThread extends Thread {
        public void run() {
            try {
                if (null != speexdec)
                    speexdec.decode();

            } catch (Exception e) {
                logger.e(e.getMessage());
                stopAnimation();
            }
        }
    }

    public String getCurrentPlayPath() {
        return currentPlayPath;
    }

}
