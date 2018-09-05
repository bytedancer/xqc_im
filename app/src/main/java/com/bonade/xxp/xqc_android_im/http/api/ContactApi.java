package com.bonade.xxp.xqc_android_im.http.api;

import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface ContactApi {

    /**
     * 公司好友列表
     * @param userId
     * @param companyId
     * @param pageNum
     * @param row
     * @return
     */
    @FormUrlEncoded
    @POST("/im/imContacts/listEmployee")
    Observable<BaseResponse<GetListEmployeeResp>> getListEmployee(@Field("userId")int userId,
                                                                  @Field("companyId")int companyId,
                                                                  @Field("pageNum") int pageNum,
                                                                  @Field("row") int row);
}
