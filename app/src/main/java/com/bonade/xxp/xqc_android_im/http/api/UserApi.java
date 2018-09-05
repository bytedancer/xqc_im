package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.model.DataBindUserToken;
import com.bonade.xxp.xqc_android_im.model.DataUserInfo;

import retrofit2.http.Headers;
import rx.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserApi {

    /******************************* 薪起程接口 ************************/
    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @Headers("channel:MOBILE")
    @FormUrlEncoded
    @POST("http://192.168.0.251/api/system/serviceuser/basic/app/v3/nolog/loginByPassword")
    Observable<DataBindUserToken> login(@Field("username") String username,
                                        @Field("password") String password);

    /**
     * 获取用户信息
     * @param accessToken
     * @param loginInit
     * @return
     */
    @FormUrlEncoded
    @POST("http://192.168.0.251/api/system/serviceuser/search/app/v2/query/getUserInfoForAppLogin")
    Observable<DataUserInfo> getUserInfoForAppLogin(@Header("access_token") String accessToken,
                                                    @Field("loginInit") String loginInit);

    /******************************* IM接口 ************************/

    /**
     * 获取用户信息接口
     * @param targetId
     * @param userId
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imUserInfo/getUserInfo")
    Observable<BaseResponse<UserEntity>> getUserInfo(@Field("userId")int userId, @Field("targetId")int targetId);


}
