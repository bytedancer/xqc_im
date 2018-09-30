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
     * 发起群聊
     *
     * @param ownerUserId
     * @param groupUsers
     * @return
     */
    @FormUrlEncoded
    @POST("im/imGroup/launchGroupChat")
    Observable<BaseResponse<GroupEntity>> launchGroupChat(@Field("ownerUserId") int ownerUserId,
                                                          @Field("groupUsers") String groupUsers);

    /**
     * 添加群成员
     *
     * @param inviterId
     * @param groupId
     * @param groupUsers
     * @return
     */
    @FormUrlEncoded
    @POST("im/imGroupUser/addGroupUser")
    Observable<BaseResponse> addGroupUser(@Field("inviterId") int inviterId,
                                          @Field("groupId") int groupId,
                                          @Field("groupUsers") String groupUsers);


    /**
     * 修改群名称
     * @param operatorId
     * @param groupId
     * @param groupName
     * @return
     */
    @FormUrlEncoded
    @POST("im/imGroup/setGroupName")
    Observable<BaseResponse> setGroupName(@Field("operatorId") int operatorId,
                                          @Field("groupId") int groupId,
                                          @Field("groupName") String groupName);

    /**
     * 获取用户信息接口
     *
     * @param userId
     * @return
     */
    @FormUrlEncoded
    @POST("im/imGroupUser/groups")
    Observable<BaseResponse<List<GroupEntity>>> getAllGroups(@Field("userId") int userId);


}
