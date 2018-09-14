package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;

import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface ContactApi {

    /**
     * 公司好友列表
     *
     * @param userId
     * @param companyId
     * @param pageNum
     * @param row
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imContacts/listEmployee")
    Observable<BaseResponse<GetListEmployeeResp>> getListEmployee(@Field("userId") int userId,
                                                                  @Field("companyId") int companyId,
                                                                  @Field("pageNum") int pageNum,
                                                                  @Field("row") int row);

    /**
     * 获取我的好友(包括我自己)
     * @param userId
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imFriend/listFriend")
    Observable<BaseResponse<List<UserEntity>>> getListFriend(@Field("userId") int userId);

    /**
     * 获取和我正在聊天的所有用户信息（包括我所在群的所有人）
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imUserInfo/synchronizeUserGroupInfo")
    Observable<BaseResponse<List<UserEntity>>> getAllUsers(@Field("userIds") String userIds, @Field("groupIds") String groupIds);
}
