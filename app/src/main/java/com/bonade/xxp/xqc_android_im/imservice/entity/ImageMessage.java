package com.bonade.xxp.xqc_android_im.imservice.entity;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.PeerEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.config.DBConstant;
import com.bonade.xxp.xqc_android_im.config.MessageConstant;
import com.bonade.xxp.xqc_android_im.imservice.support.SequenceNumberMaker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class ImageMessage extends MessageEntity implements Serializable {

    /**
     * 本地保存的path
     */
    private String path = "";

    /**
     * 图片的网络地址
     */
    private String url = "";

    private int loadStatus;

    // 存储图片信息
    private static HashMap<Long, ImageMessage> imageMessageMap = new HashMap<>();
    private static ArrayList<ImageMessage> imageList = null;

    /**
     * 添加一条图片消息
     */
    public static synchronized void addToImageMessageList(ImageMessage msg) {
        try {
            if (msg != null && msg.getId() != null) {
                imageMessageMap.put(msg.getId(),msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取图片列表
     * @return
     */
    public static ArrayList<ImageMessage> getImageMessageList() {
        imageList = new ArrayList<>();
        Iterator it = imageMessageMap.keySet().iterator();
        while (it.hasNext()) {
            imageList.add(imageMessageMap.get(it.next()));
        }

        Collections.sort(imageList, new Comparator<ImageMessage>(){
            public int compare(ImageMessage image1, ImageMessage image2) {
                Integer a =  image1.getUpdated();
                Integer b = image2.getUpdated();
                if(a.equals(b))
                {
                    return image2.getId().compareTo(image1.getId());
                }
                // 升序
                //return a.compareTo(b);
                // 降序
                return b.compareTo(a);
            }
        });
        return imageList;
    }

    /**
     * 清除图片列表
     */
    public static synchronized void clearImageMessageList(){
        imageMessageMap.clear();
    }

    public ImageMessage(){
        msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
    }

    /**
     * 消息拆分得时候需要
     * @param entity
     */
    private ImageMessage(MessageEntity entity) {
        // 父类id
        id =  entity.getId();
        msgId  = entity.getMsgId();
        fromId = entity.getFromId();
        toId   = entity.getToId();
        sessionKey = entity.getSessionKey();
        content=entity.getContent();
        msgType=entity.getMsgType();
        displayType=entity.getDisplayType();
        status = entity.getStatus();
        created = entity.getCreated();
        updated = entity.getUpdated();
    }

    public static ImageMessage parseFromNet(MessageEntity msg) throws JSONException {
        String content = msg.getContent();

        // 判断开头与结尾
        if (content.startsWith(MessageConstant.IMAGE_MSG_START)
                && content.endsWith(MessageConstant.IMAGE_MSG_END)) {
            // image message todo 字符串处理下
            ImageMessage imageMessage = new ImageMessage(msg);
            imageMessage.setDisplayType(DBConstant.SHOW_IMAGE_TYPE);
            String imageUrl = content.substring(MessageConstant.IMAGE_MSG_START.length());
            imageUrl = imageUrl.substring(0, imageUrl.indexOf(MessageConstant.IMAGE_MSG_END));

            // 抽离出来，或者用gson
            JSONObject extraContent = new JSONObject();
            extraContent.put("path","");
            extraContent.put("url",imageUrl);
            extraContent.put("loadStatus", MessageConstant.IMAGE_UNLOAD);
            String imageContent = extraContent.toString();
            imageMessage.setContent(imageContent);

            imageMessage.setUrl(imageUrl.isEmpty() ? null : imageUrl);
            imageMessage.setContent(content);
            imageMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
            imageMessage.setStatus(MessageConstant.MSG_SUCCESS);
            return imageMessage;
        } else {
            throw new RuntimeException("no image type,cause by [start,end] is wrong!");
        }
    }

    public static ImageMessage parseFromDB(MessageEntity msg) {
        if (msg.getDisplayType() != DBConstant.SHOW_IMAGE_TYPE) {
            throw new RuntimeException("#ImageMessage# parseFromDB,not SHOW_IMAGE_TYPE");
        }

        ImageMessage imageMessage = new ImageMessage(msg);
        String originContent = msg.getContent();
        JSONObject extraContent;
        try {
            extraContent = new JSONObject(originContent);
            imageMessage.setPath(extraContent.getString("path"));
            imageMessage.setUrl(extraContent.getString("url"));
            int loadStatus = extraContent.getInt("loadStatus");

            //todo temp solution
            if(loadStatus == MessageConstant.IMAGE_LOADING){
                loadStatus = MessageConstant.IMAGE_UNLOAD;
            }

            imageMessage.setLoadStatus(loadStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imageMessage;
    }

    /**
     * 消息页面，发送图片消息
     * @return
     */
    public static ImageMessage buildForSend(String imagePath, UserEntity fromUser, PeerEntity peerEntity){
        ImageMessage imageMessage = new ImageMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        imageMessage.setFromId(fromUser.getPeerId());
        imageMessage.setToId(peerEntity.getPeerId());
        imageMessage.setUpdated(nowTime);
        imageMessage.setCreated(nowTime);
        imageMessage.setDisplayType(DBConstant.SHOW_IMAGE_TYPE);
        if (new File(imagePath).exists()) {
            imageMessage.setPath(imagePath);
        } else {
            imageMessage.setPath(null);
        }

        int peerType = peerEntity.getType();
        int msgType = peerType == DBConstant.SESSION_TYPE_GROUP ? DBConstant.MSG_TYPE_GROUP_TEXT
                : DBConstant.MSG_TYPE_SINGLE_TEXT;
        imageMessage.setMsgType(msgType);

        imageMessage.setStatus(MessageConstant.MSG_SENDING);
        imageMessage.setLoadStatus(MessageConstant.IMAGE_UNLOAD);
        imageMessage.buildSessionKey(true);
        return imageMessage;
    }

    /**
     * Not-null value.
     */
    @Override
    public String getContent() {
        JSONObject extraContent = new JSONObject();
        try {
            extraContent.put("path",path);
            extraContent.put("url",url);
            extraContent.put("loadStatus",loadStatus);
            String imageContent = extraContent.toString();
            return imageContent;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getSendContent() {
        // 发送的时候非常关键
        return MessageConstant.IMAGE_MSG_START
                + url + MessageConstant.IMAGE_MSG_END;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}
