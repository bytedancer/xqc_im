package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.model.Group;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface GroupApi {

    /**
     * 获取用户信息接口
     * @param userId
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imGroupUser/groups")
    Observable<BaseResponse<List<GroupEntity>>> getAllGroups(@Field("userId")int userId);
}
