package com.bonade.xxp.xqc_android_im.config;

public interface DBConstant {

    /**
     * 性别
     * 1. 男性 2.女性
     */
    int SEX_MAILE              = 1;
    int SEX_FEMALE             = 2;

    /**
     * sessionType
     */
    int SESSION_TYPE_SINGLE    = 1;
    int SESSION_TYPE_GROUP     = 2;
    int SESSION_TYPE_ERROR     = 3;

    /**group type*/
    public final int GROUP_TYPE_NORMAL = 1;
    public final int GROUP_TYPE_TEMP   = 2;

    /**
     * msgType
     */
    int MSG_TYPE_SINGLE_TEXT      = 0x01;
    int MSG_TYPE_SINGLE_AUDIO     = 0x02;
    int MSG_TYPE_SINGLE_IMAGE     = 0x03;
    int MSG_TYPE_SINGLE_LOCATION  = 0x04;
    int MSG_TYPE_SINGLE_VIDEO     = 0x05;
    int MSG_TYPE_SINGLE_URL       = 0x06;
    int MSG_TYPE_SINGLE_FILE      = 0x07;
    int MSG_TYPE_SINGLE_EMOTION   = 0x08;
    int MSG_TYPE_GROUP_TEXT       = 0x11;
    int MSG_TYPE_GROUP_AUDIO      = 0x12;
    int MSG_TYPE_GROUP_IMAGE      = 0x13;
    int MSG_TYPE_GROUP_LOCATION   = 0x14;
    int MSG_TYPE_GROUP_VIDEO      = 0x15;
    int MSG_TYPE_GROUP_URL        = 0x16;
    int MSG_TYPE_GROUP_FILE       = 0x17;
    int MSG_TYPE_GROUP_EMOTION    = 0x18;

    /**
     * msgDisplayType
     * 保存在DB中，与服务端一致，图文混排也是一条
     * 1. 最基础的文本信息
     * 2. 纯图片信息
     * 3. 语音
     * 4. 图文混排
     */
    int SHOW_ORIGIN_TEXT_TYPE     = 1;
    int SHOW_IMAGE_TYPE           = 2;
    int SHOW_AUDIO_TYPE           = 3;
    int SHOW_MIX_TEXT             = 4;
    int SHOW_GIF_TYPE             = 5;
    int SHOW_LOCATION_TYPE        = 6;
    int SHOW_VIDEO_TYPE           = 7;
    int SHOW_URL_TYPE             = 8;
    int SHOW_FILE_TYPE            = 9;
    int SHOW_GIF_OTHER_TYPE       = 10;
    int SHOW_GIF_FILE_TYPE        = 11;

    String DISPLAY_FOR_IMAGE      = "[图片]";
    String DISPLAY_FOR_MIX        = "[图文消息]";
    String DISPLAY_FOR_AUDIO      = "[语音]";
    String DISPLAY_FOR_LOCATION   = "[位置]";
    String DISPLAY_FOR_FILE       = "[文件]";
    String DISPLAY_FOR_VIDEO      = "[视频]";
    String DISPLAY_FOR_URL        = "[链接]";
    String DISPLAY_FOR_ERROR      = "[未知消息]";
    String DISPLAY_FOR_GIF        = "[动画表情]";


    /**
     * group status
     * 1: shield  0: not shield
     */
    int GROUP_STATUS_ONLINE    = 0;
    int GROUP_STATUS_SHIELD    = 1;

    /**
     * group change Type
     */
    int  GROUP_MODIFY_TYPE_ADD = 0;
    int  GROUP_MODIFY_TYPE_DEL = 1;
}
