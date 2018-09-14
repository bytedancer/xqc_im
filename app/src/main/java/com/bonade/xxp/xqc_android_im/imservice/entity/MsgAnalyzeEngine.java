package com.bonade.xxp.xqc_android_im.imservice.entity;

import android.text.TextUtils;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.protobuf.IMBaseDefine;
import com.bonade.xxp.xqc_android_im.protobuf.IMMessage;
import com.bonade.xxp.xqc_android_im.protobuf.helper.ProtoBuf2JavaBean;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * historical reasons,没有充分利用msgType字段
 * 多端的富文本的考虑
 */
public class MsgAnalyzeEngine {

    public static String analyzeMessageDisplay(String content){
        String finalRes = content;
        String originContent = content;
        while (!originContent.isEmpty()) {
            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
            if (nStart < 0) {// 没有头
                break;
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
                if (nEnd < 0) {// 没有尾
                    String strSplitString = originContent;
                    break;
                } else {// 匹配到
                    String pre = originContent.substring(0, nStart);

                    originContent = subContentString.substring(nEnd
                            + MessageConstant.IMAGE_MSG_END.length());

                    if(!TextUtils.isEmpty(pre) || !TextUtils.isEmpty(originContent)){
                        finalRes = DBConstant.DISPLAY_FOR_MIX;
                    }else{
                        finalRes = DBConstant.DISPLAY_FOR_IMAGE;
                    }
                }
            }
        }
        return finalRes;
    }


    // 抽离放在同一的地方
    public static MessageEntity analyzeMessage(IMMessage.IMMsgData msgData) {
        MessageEntity messageEntity = new MessageEntity();
        int createTime = (int) (msgData.getTimestamp() / 1000);
        messageEntity.setCreated(createTime);
        messageEntity.setUpdated(createTime);
        messageEntity.setFromId(Integer.parseInt(msgData.getFromUserId()));
        messageEntity.setMsgId(Integer.parseInt(msgData.getMsgId()));
        messageEntity.setMsgType(ProtoBuf2JavaBean.getJavaMsgType(msgData.getMsgType(), msgData.getMsgContentType()));
        messageEntity.setStatus(MessageConstant.MSG_SUCCESS);
        String message = msgData.getMsgContent();
        messageEntity.setContent(message);

        // 文本信息不为空
        if(!TextUtils.isEmpty(message)){
            List<MessageEntity> msgList =  textDecode(messageEntity);
            if(msgList.size()>1){
                // 混合消息
                MixMessage mixMessage = new MixMessage(msgList);
                return mixMessage;
            }else if(msgList.size() == 0){
                // 可能解析失败 默认返回文本消息
                return TextMessage.parseFromNet(messageEntity);
            }else{
                //简单消息，返回第一个
                return msgList.get(0);
            }
        }else{
            // 如果为空
            return TextMessage.parseFromNet(messageEntity);
        }
    }


    /**
     * todo 优化字符串分析
     * @param msg
     * @return
     */
    private static List<MessageEntity> textDecode(MessageEntity msg){
        List<MessageEntity> msgList = new ArrayList<>();

        String originContent = msg.getContent();
        while (!TextUtils.isEmpty(originContent)) {
            int nStart = originContent.indexOf(MessageConstant.IMAGE_MSG_START);
            if (nStart < 0) {// 没有头
                String strSplitString = originContent;

                MessageEntity entity = addMessage(msg, strSplitString);
                if(entity!=null){
                    msgList.add(entity);
                }

                originContent = "";
            } else {
                String subContentString = originContent.substring(nStart);
                int nEnd = subContentString.indexOf(MessageConstant.IMAGE_MSG_END);
                if (nEnd < 0) {// 没有尾
                    String strSplitString = originContent;


                    MessageEntity entity = addMessage(msg,strSplitString);
                    if(entity!=null){
                        msgList.add(entity);
                    }

                    originContent = "";
                } else {// 匹配到
                    String pre = originContent.substring(0, nStart);
                    MessageEntity entity1 = addMessage(msg,pre);
                    if(entity1!=null){
                        msgList.add(entity1);
                    }

                    String matchString = subContentString.substring(0, nEnd
                            + MessageConstant.IMAGE_MSG_END.length());

                    MessageEntity entity2 = addMessage(msg,matchString);
                    if(entity2!=null){
                        msgList.add(entity2);
                    }

                    originContent = subContentString.substring(nEnd
                            + MessageConstant.IMAGE_MSG_END.length());
                }
            }
        }

        return msgList;
    }


    public static MessageEntity addMessage(MessageEntity msg,String strContent) {
        if (TextUtils.isEmpty(strContent.trim())){
            return null;
        }
        msg.setContent(strContent);

        if (strContent.startsWith(MessageConstant.IMAGE_MSG_START)
                && strContent.endsWith(MessageConstant.IMAGE_MSG_END)) {
            try {
                ImageMessage imageMessage =  ImageMessage.parseFromNet(msg);
                return imageMessage;
            } catch (JSONException e) {
                // e.printStackTrace();
                return null;
            }
        } else {
            return TextMessage.parseFromNet(msg);
        }
    }
}
