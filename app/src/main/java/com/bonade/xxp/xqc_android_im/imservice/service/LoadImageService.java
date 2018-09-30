package com.bonade.xxp.xqc_android_im.imservice.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.SystemConfigSp;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.RetrofitManager;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.entity.ImageMessage;
import com.bonade.xxp.xqc_android_im.imservice.event.MessageEvent;
import com.bonade.xxp.xqc_android_im.ui.activity.ChatActivity;
import com.bonade.xxp.xqc_android_im.ui.helper.PhotoHelper;
import com.bonade.xxp.xqc_android_im.util.FileUtil;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.XqcIMHttpClient;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observer;
import rx.schedulers.Schedulers;

public class LoadImageService extends IntentService {

    public static void launch(Context context, ImageMessage imageMessage) {
        context.startService(new Intent(context, LoadImageService.class).putExtra(UPLOAD_IMAGE_INTENT_PARAMS, imageMessage));
    }

    private static final String UPLOAD_IMAGE_INTENT_PARAMS = "UPLOAD_IMAGE_INTENT_PARAMS";

    private static Logger logger = Logger.getLogger(LoadImageService.class);

    public LoadImageService() {
        super("LoadImageService");
    }

    public LoadImageService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final ImageMessage messageInfo = (ImageMessage) intent.getSerializableExtra(UPLOAD_IMAGE_INTENT_PARAMS);
        final File file = new File(messageInfo.getPath());
        final RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileUpload", file.getName(), requestBody);
        ApiFactory.getCommApi().uploadChatPicture(part)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseResponse<FileUploadRespData>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
                    }

                    @Override
                    public void onNext(BaseResponse<FileUploadRespData> response) {
                        if (response == null || response.getData() == null) {
                            EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
                            return;
                        }

                        FileUploadRespData fileUploadRespData = response.getData();
                        String filePath = fileUploadRespData.getFilePath();
                        if (TextUtils.isEmpty(filePath)) {
                            EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
                            return;
                        }

                        String imageUrl = filePath;
                        messageInfo.setUrl(imageUrl);
                        EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_SUCCESS));
                    }
                });

//        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                .addFormDataPart("fileUpload", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
//
//        String url = RetrofitManager.BASE_URL + "/im/upload/chatPicture";
//        Request request = new Request.Builder()
//                .url(url)
//                .post(builder.build())//传参数、文件或者混合，改一下就行请求体就行
//                .build();
//        new OkHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String str = response.body().string();
//                try {
//                    JSONObject jsonObject = new JSONObject(str);
//                    JSONObject data = jsonObject.getJSONObject("data");
//                    if (data == null) {
//                        EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
//                        return;
//                    }
//
//                    String filePath = data.getString("filePath");
//                    if (TextUtils.isEmpty(filePath)) {
//                        logger.i("upload image faild,cause by result is empty/null");
//                        EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_FAILD));
//                    } else {
//                        logger.i("upload image succcess,imag    eUrl is %s", filePath);
//                        String imageUrl = filePath;
//                        messageInfo.setUrl(imageUrl);
//                        EventBus.getDefault().post(new MessageEvent(messageInfo, MessageEvent.Event.IMAGE_UPLOAD_SUCCESS));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    public static class FileUploadRespData {
        private String fileName;
        private String filePath;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
