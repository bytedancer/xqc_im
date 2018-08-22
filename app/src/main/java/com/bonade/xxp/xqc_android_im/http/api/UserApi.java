package com.bonade.xxp.xqc_android_im.http.api;

import io.reactivex.Observable;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserApi {

    /**
     * 获取用户信息接口
     * @param targetId
     * @param userId
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imUserInfo/getUserInfo")
    Observable<String> getUserInfo(String targetId, String userId);


}
