package com.bonade.xxp.xqc_android_im.imservice.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;

import com.bonade.xxp.xqc_android_im.DB.entity.GroupEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.DB.sp.ConfigurationSp;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.SysConstant;
import com.bonade.xxp.xqc_android_im.imservice.entity.UnreadEntity;
import com.bonade.xxp.xqc_android_im.imservice.event.GroupEvent;

import com.bonade.xxp.xqc_android_im.imservice.event.UnreadEvent;
import com.bonade.xxp.xqc_android_im.ui.activity.ChatActivity;
import com.bonade.xxp.xqc_android_im.util.IMUIHelper;
import com.bonade.xxp.xqc_android_im.util.Logger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 伪推送; app退出之后就不会收到推送的信息
 * 通知栏新消息通知
 * a.每个session 只显示一条
 * b.每个msg 信息都显示
 * 配置依赖与 configure
 *
 */
public class IMNotificationManager extends IMManager {

    private Logger logger = Logger.getLogger(IMNotificationManager.class);
    private static IMNotificationManager instance = new IMNotificationManager();
    public static IMNotificationManager getInstance() {
        return instance;
    }
    private IMNotificationManager() {}

    private ConfigurationSp configurationSp;

    @Override
    public void doOnStart() {
        cancelAllNotifications();
    }

    public void onLoginSuccess() {
        int loginId = IMLoginManager.getInstance().getLoginId();
        configurationSp = ConfigurationSp.getInstance(context, loginId);
        if(!EventBus.getDefault().isRegistered(instance)){
            EventBus.getDefault().register(instance);
        }
    }

    @Override
    public void reset() {
        EventBus.getDefault().unregister(this);
        cancelAllNotifications();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UnreadEvent event) {
        // 收到未读消息
        if (event.getEvent() == UnreadEvent.Event.UNREAD_MSG_RECEIVED) {
            UnreadEntity unreadEntity = event.getUnreadEntity();
            handleMsgRecv(unreadEntity);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupEvent event) {
        // 屏蔽群，相关的通知全部删除
        if (event.getEvent() == GroupEvent.Event.SHIELD_GROUP_SUCCESS) {
            GroupEntity groupEntity = event.getGroupEntity();
            if (groupEntity == null) {
                return;
            }
            cancelSessionNotifications(groupEntity.getSessionKey());
        }
    }

    private void handleMsgRecv(UnreadEntity unreadEntity) {
        logger.d("notification#recv unhandled message");
        int peerId = unreadEntity.getPeerId();
        int sessionType =  unreadEntity.getSessionType();
        logger.d("notification#msg no one handled, peerId:%d, sessionType:%d", peerId, sessionType);

        // 判断是否设定了免打扰
        if (unreadEntity.isForbidden()) {
            logger.d("notification#GROUP_STATUS_SHIELD");
            return;
        }

        // 全局开关
        boolean globalOnOff = configurationSp.getConfig(SysConstant.SETTING_GLOBAL ,ConfigurationSp.ConfigDimension.NOTIFICATION);
        if (globalOnOff) {
            logger.d("notification#shouldGloballyShowNotification is false, return");
            return;
        }

        // 单独设置
        boolean singleOnOff = configurationSp.getConfig(unreadEntity.getSessionKey(),ConfigurationSp.ConfigDimension.NOTIFICATION);
        if (singleOnOff) {
            logger.d("notification#shouldShowNotificationBySession is false, return");
            return;
        }

        // 判断是否是自己的消息,不是自己的消息才显示通知
        if (IMLoginManager.getInstance().getLoginId() != peerId) {
            showNotification(unreadEntity);
        }
    }

    public void cancelAllNotifications() {
        logger.d("notification#cancelAllNotifications");
        if(null == context){
            return;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancelAll();
    }

    /**
     * 在通知栏中删除特定回话的状态
     * @param sessionKey
     */
    public void cancelSessionNotifications(String sessionKey) {
        logger.d("notification#cancelSessionNotifications");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == notificationManager) {
            return;
        }
        int notificationId = getSessionNotificationId(sessionKey);
        notificationManager.cancel(notificationId);
    }

    private void showNotification(final UnreadEntity unreadEntity) {
        int peerId = unreadEntity.getPeerId();
        int sessionType = unreadEntity.getSessionType();
        String avatarUrl;
        String title;
        String content = unreadEntity.getLatestMsgData();
        String unit = context.getString(R.string.msg_count_unit);
        int totalUnread = unreadEntity.getUnReadCount();

        if (unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
            UserEntity contact = IMContactManager.getInstance().findContact(peerId);
            if (contact != null) {
                title = contact.getMainName();
                avatarUrl = contact.getAvatar();
            } else {
                title = "User_" + peerId;
                avatarUrl = "";
            }
        } else {
            GroupEntity group = IMGroupManager.getInstance().findGroup(peerId);
            if (group != null) {
                title = group.getMainName();
                avatarUrl = group.getAvatar();
            } else {
                title = "Group_" + peerId;
                avatarUrl = "";
            }
        }

        final String ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title, content);
        final int notificationId = getSessionNotificationId(unreadEntity.getSessionKey());
        ChatActivity.launch(context, unreadEntity.getSessionKey());
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.SESSION_KEY, unreadEntity.getSessionKey());

        logger.d("notification#notification avatarUrl:%s", avatarUrl);
        final String finalTitle = title;
        Glide.with(context).load(avatarUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                logger.d("notification#icon onLoadComplete");
                showInNotificationBar(finalTitle, ticker, resource, notificationId, intent);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                logger.d("notification#icon onLoadFailed");
                // 服务器支持的格式有哪些
                // todo eric default avatar is too small, need big size(128 * 128)
                Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), IMUIHelper.getDefaultAvatarResId(unreadEntity.getSessionType()));
                showInNotificationBar(finalTitle,ticker,defaultBitmap,notificationId,intent);
            }
        });
    }

    private void showInNotificationBar(String title, String ticker, Bitmap iconBitmap, int notificationId, Intent intent) {
        logger.d("notification#showInNotificationBar title:%s ticker:%s",title,ticker);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(ticker);
//        builder.setSmallIcon(R.drawable.tt_small_icon);
        builder.setTicker(ticker);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);

        // 右下方的内容
        // builder.setContentInfo("content info");

        // vibration
        if (configurationSp.getConfig(SysConstant.SETTING_GLOBAL, ConfigurationSp.ConfigDimension.VIBRATION)) {
            // delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
            long[] vibrate = {0, 200, 250, 200};
            builder.setVibrate(vibrate);
        } else {
            logger.d("notification#setting is not using vibration");
        }

        // sound
        if (configurationSp.getConfig(SysConstant.SETTING_GLOBAL, ConfigurationSp.ConfigDimension.SOUND)) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        } else {
            logger.d("notification#setting is not using sound");
        }

        if (iconBitmap != null) {
            logger.d("notification#fetch icon from network ok");
            builder.setLargeIcon(iconBitmap);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    /**
     * come from
     * http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
     * @param str
     * @return
     */
    private long hashBKDR(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }
        return hash;
    }

    public int getSessionNotificationId(String sessionKey) {
        logger.d("notification#getSessionNotificationId sessionTag:%s", sessionKey);
        int hashedNotificationId = (int) hashBKDR(sessionKey);
        logger.d("notification#hashedNotificationId:%d", hashedNotificationId);
        return hashedNotificationId;
    }
}
