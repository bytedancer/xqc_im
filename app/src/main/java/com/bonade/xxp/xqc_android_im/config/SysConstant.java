package com.bonade.xxp.xqc_android_im.config;

public interface SysConstant {

    String MSG_SERVER_IP = "192.168.12.66";
    int MSG_SERVER_PORT  = 18080;

    /**
     * 协议头相关
     */
    int PROTOCOL_HEADER_LENGTH = 16;// 默认消息头的长度
    int PROTOCOL_VERSION = 1;
    int PROTOCOL_FLAG = 0;
    char PROTOCOL_ERROR = '0';
    char PROTOCOL_RESERVED = '0';

    short PROTOCOL_FLAG_LOGIN = 1;
    short PROTOCOL_FLAG_MESSAGE  = 2;
    short PROTOCOL_FLAG_HEARTBEAT = 3;
    short PROTOCOL_FLAG_LOGIN_RESP = 4;
    short PROTOCOL_FLAG_MESSAGE_RESP = 5;
    short PROTOCOL_FLAG_HEARTBEAT_RESP = 6;


    /**
     * 读取磁盘上文件， 分支判断其类型
     */
    int FILE_SAVE_TYPE_IMAGE = 0X00013;
    int FILE_SAVE_TYPE_AUDIO = 0X00014;

    float MAX_SOUND_RECORD_TIME = 60.0f;// 单位秒
    int MAX_SELECT_IMAGE_COUNT = 6;

    /**表情使用*/
    int pageSize = 21;

    /**
     * 配置的全局key
     */
    String SETTING_GLOBAL = "Global";

    /**
     * event 优先级
     */
    int SERVICE_EVENTBUS_PRIORITY = 10;
    int MESSAGE_EVENTBUS_PRIORITY = 100;

    /**
     * message 每次拉取的条数
     */
    int MSG_CNT_PER_PAGE = 18;

    int MSG_CONTENT_TYPE_TEXT = 1;
    int MSG_CONTENT_TYPE_IMAGE = 2;
}
