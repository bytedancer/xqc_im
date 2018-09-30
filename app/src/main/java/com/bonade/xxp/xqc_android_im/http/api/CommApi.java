package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.imservice.service.LoadImageService;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface CommApi {

    @Multipart
    @POST("im/upload/chatPicture")  //这里是自己post文件的地址
    Observable<BaseResponse<LoadImageService.FileUploadRespData>> uploadChatPicture(@Part MultipartBody.Part part);
}
