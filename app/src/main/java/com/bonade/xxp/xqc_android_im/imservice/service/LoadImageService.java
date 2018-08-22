package com.bonade.xxp.xqc_android_im.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.sp.SystemConfigSp;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.imservice.event.MessageEvent;
import com.bonade.xxp.xqc_android_im.ui.helper.PhotoHelper;
import com.bonade.xxp.xqc_android_im.util.FileUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.XqcIMHttpClient;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

public class LoadImageService extends IntentService {

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadImageService(){
        super("LoadImageService");
    }

    public LoadImageService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ImageMessage messageInfo = (ImageMessage)intent.getSerializableExtra(SysConstant.UPLOAD_IMAGE_INTENT_PARAMS);
        String result = null;
        Bitmap bitmap;
        try {
            File file= new File(messageInfo.getPath());
            if (file.exists() && FileUtil.getExtensionName(messageInfo.getPath()).toLowerCase().equals(".gif")) {
                XqcIMHttpClient httpClient = new XqcIMHttpClient();
                SystemConfigSp.getInstance().init(getApplicationContext());
                result = httpClient.uploadImage3(SystemConfigSp.getInstance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
            } else {
                bitmap = PhotoHelper.revitionImage(messageInfo.getPath());
                if (null != bitmap) {
                    XqcIMHttpClient httpClient = new XqcIMHttpClient();
                    byte[] bytes = PhotoHelper.getBytes(bitmap);
                    result = httpClient.uploadImage3(SystemConfigSp.getInstance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), bytes, messageInfo.getPath());
                }
            }

            if (TextUtils.isEmpty(result)) {
                logger.i("upload image faild,cause by result is empty/null");
                EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
            } else {
                logger.i("upload image succcess,imag    eUrl is %s",result);
                String imageUrl = result;
                messageInfo.setUrl(imageUrl);
                EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_SUCCESS));
            }
        } catch (IOException e) {
            logger.e(e.getMessage());
        }
    }
}
