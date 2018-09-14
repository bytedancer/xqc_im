package com.bonade.xxp.xqc_android_im.imservice.manager;

import com.bonade.xxp.xqc_android_im.DB.DBInterface;
import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.http.ApiFactory;
import com.bonade.xxp.xqc_android_im.http.base.BaseResponse;
import com.bonade.xxp.xqc_android_im.http.response.GetListEmployeeResp;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.LoginEvent;
import com.bonade.xxp.xqc_android_im.imservice.event.UserInfoEvent;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bonade.xxp.xqc_android_im.util.pinyin.PinYin;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observer;
import rx.schedulers.Schedulers;

public class IMContactManager extends IMManager {

    private Logger logger = Logger.getLogger(IMContactManager.class);
    // 单例
    private static IMContactManager instance = new IMContactManager();

    public static IMContactManager getInstance() {
        return instance;
    }

    private DBInterface dbInterface = DBInterface.getInstance();

    // 自身状态字段
    private boolean userDataReady = false;
    private Map<Integer, UserEntity> userMap = new ConcurrentHashMap<>();

    @Override
    public void doOnStart() {

    }

    /**
     * 登陆成功触发
     * auto自动登陆
     */
    public void onNormalLoginOk() {
        onLocalLoginOk();
        onLocalNetOk();
    }

    /**
     * 加载本地DB的状态
     * 不管是离线还是在线登陆，loadFromDb 要运行的
     */
    public void onLocalLoginOk() {
        logger.d("contact#loadAllUserInfo");

        List<UserEntity> userList = dbInterface.loadAllUsers();
        logger.d("contact#loadAllUserInfo dbsuccess");

        for (UserEntity userInfo : userList) {
            // todo DB的状态不包含拼音的，这个样每次都要加载啊
            PinYin.getPinYin(userInfo.getMainName(), userInfo.getPinyinElement());
            userMap.put(userInfo.getPeerId(), userInfo);
        }

        triggerEvent(UserInfoEvent.USER_INFO_OK);
    }

    /**
     * 网络链接成功，登陆之后请求
     */
    public void onLocalNetOk() {
        // 用户信息
        int updateTime = dbInterface.getUserInfoLastTime();
        reqGetAllUsers(updateTime);
    }

    private void reqGetAllUsers(int lastUpdateTime) {
        List<UserEntity> userList = dbInterface.loadAllUsers();
        List<GroupEntity> groupList = dbInterface.loadAllGroup();
        String userIds = "";
        if (userList != null && !userList.isEmpty()) {
            StringBuilder userIdSbs = new StringBuilder();
            for (UserEntity userEntity : userList) {
                userIdSbs.append(",").append(userEntity.getPeerId());
            }
            userIds = userIdSbs.substring(1).toString();
        } else {
            userIds = String.valueOf(IMLoginManager.getInstance().getLoginId());
        }

        String groupIds = "";
        if (groupList != null && !groupList.isEmpty()) {
            StringBuilder groupIdSbs = new StringBuilder();
            for (GroupEntity groupEntity : groupList) {
                groupIdSbs.append(",").append(groupEntity.getPeerId());
            }
            groupIds = groupIdSbs.substring(1).toString();
        }

        ApiFactory.getContactApi().getAllUsers(userIds, groupIds)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<BaseResponse<List<UserEntity>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResponse<List<UserEntity>> response) {
                        if (response == null || response.getData() == null) {
                            return;
                        }
                        onRepAllUsers(response.getData());
                    }
                });
    }

    private void onRepAllUsers(List<UserEntity> users) {
        if (users.isEmpty()) {
            return;
        }

        for (UserEntity userEntity : users) {
            userMap.put(userEntity.getPeerId(), userEntity);
        }

        dbInterface.batchInsertOrUpdateUser(users);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    public void addContact(UserEntity userEntity) {
        userMap.put(userEntity.getPeerId(), userEntity);
        dbInterface.insertOrUpdateUser(userEntity);
        triggerEvent(UserInfoEvent.USER_INFO_UPDATE);
    }

    public UserEntity findContact(int buddyId) {
        if (buddyId > 0 && userMap.containsKey(buddyId)) {
            return userMap.get(buddyId);
        }
        return null;
    }

    @Override
    public void reset() {
        userDataReady = false;
        userMap.clear();
    }

    public void triggerEvent(UserInfoEvent event) {
        //先更新自身的状态
        switch (event) {
            case USER_INFO_OK:
                userDataReady = true;
                break;
        }
        EventBus.getDefault().postSticky(event);
    }

    public Map<Integer, UserEntity> getUserMap() {
        return userMap;
    }

    public boolean isUserDataReady() {
        return userDataReady;
    }
}
