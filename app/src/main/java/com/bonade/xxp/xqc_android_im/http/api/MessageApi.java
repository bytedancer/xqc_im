package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.entity.UnreadMessage;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface MessageApi {

    @FormUrlEncoded
    @POST("/im/chatRecord/app/v1/unreadRecordList")
    Observable<BaseResponse<List<UnreadMessage>>> getUnreadMsgList(@Field("userCode") int userId);


}
