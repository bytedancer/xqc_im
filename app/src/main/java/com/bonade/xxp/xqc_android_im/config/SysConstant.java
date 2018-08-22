package com.bonade.xxp.xqc_android_im.config;

public interface SysConstant {

    /**
     * 协议头相关
     */
    int PROTOCOL_HEADER_LENGTH = 16;// 默认消息头的长度
    int PROTOCOL_VERSION = 1;
    int PROTOCOL_FLAG = 0;
    char PROTOCOL_ERROR = '0';
    char PROTOCOL_RESERVED = '0';

    /**
     * 读取磁盘上文件， 分支判断其类型
     */
    public static final int FILE_SAVE_TYPE_IMAGE = 0X00013;
    public static final int FILE_SAVE_TYPE_AUDIO = 0X00014;

    /**
     * 配置的全局key
     */
    String SETTING_GLOBAL = "Global";
    public static final String UPLOAD_IMAGE_INTENT_PARAMS = "com.bonade.xxp.upload.image.intent";

    /**
     * event 优先级
     */
    int SERVICE_EVENTBUS_PRIORITY = 10;
    int MESSAGE_EVENTBUS_PRIORITY = 100;

    /**
     * message 每次拉取的条数
     */
    public static final int MSG_CNT_PER_PAGE = 18;
}
