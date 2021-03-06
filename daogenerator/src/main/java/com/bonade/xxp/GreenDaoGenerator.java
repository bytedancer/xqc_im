package com.bonade.xxp;


import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Index;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;


/**
 * 其中UserEntity、 GroupEntity 继承PeerEntity
 * 由于UserEntity、 GroupEntity是自动生成，PeerEntity会有重复字段，所以每次生成之后要处理下成员变量。
 * PeerEntity成员变量名与子类统一。
 */
public class GreenDaoGenerator {

    private static String entityPath = "com.bonade.xxp.xqc_android_im.DB.entity";

    public static void main(String[] args) throws Exception {
        int dbVersion = 1;
        Schema schema = new Schema(dbVersion, "com.bonade.xxp.xqc_android_im.DB.dao");

        schema.enableKeepSectionsByDefault();
        addUserInfo(schema);
//        addDepartment(schema);
        addGroupInfo(schema);
        addMessage(schema);
        addSessionInfo(schema);

        String path = "E:/AndroidProjects/xqc_android_im/app/src/main/java";
        new DaoGenerator().generateAll(schema, path);
    }

    private static void addDepartment(Schema schema){
        Entity department = schema.addEntity("DepartmentEntity");
        department.setTableName("Department");
        department.setClassNameDao("DepartmentDao");
        department.setJavaPackage(entityPath);

        department.addIdProperty().autoincrement();
        department.addIntProperty("departId").unique().notNull().index();
        department.addStringProperty("departName").unique().notNull().index();
        department.addIntProperty("priority").notNull();
        department.addIntProperty("status").notNull();

        department.addIntProperty("created").notNull();
        department.addIntProperty("updated").notNull();

        department.setHasKeepSections(true);
    }

    private static void addUserInfo(Schema schema) {
        Entity userInfo = schema.addEntity("UserEntity");
        userInfo.setTableName("UserInfo");
        userInfo.setClassNameDao("UserDao");
        userInfo.setJavaPackage(entityPath);

        userInfo.addLongProperty("cid").primaryKey().autoincrement();
        userInfo.addIntProperty("peerId").unique().notNull().index();
        userInfo.addStringProperty("mainName");
        userInfo.addStringProperty("avatar");

        userInfo.addIntProperty("companyId");
        userInfo.addStringProperty("companyName");
        userInfo.addStringProperty("jobName");
        userInfo.addStringProperty("deptName");
        userInfo.addStringProperty("mobile");
        userInfo.addIntProperty("isFriend");
        userInfo.addStringProperty("email");
        userInfo.addStringProperty("userName");
        userInfo.addIntProperty("status");
        userInfo.addIntProperty("created");
        userInfo.addIntProperty("updated");

        userInfo.setHasKeepSections(true);
    }

    private static void addGroupInfo(Schema schema) {
        Entity groupInfo = schema.addEntity("GroupEntity");
        groupInfo.setTableName("GroupInfo");
        groupInfo.setClassNameDao("GroupDao");
        groupInfo.setJavaPackage(entityPath);

        groupInfo.addLongProperty("cid").primaryKey().autoincrement();
        groupInfo.addIntProperty("peerId").unique().notNull();
        groupInfo.addIntProperty("groupType");
        groupInfo.addStringProperty("mainName");
        groupInfo.addStringProperty("avatar");
        groupInfo.addIntProperty("creatorId");
        groupInfo.addIntProperty("userCount");

        groupInfo.addStringProperty("userIds");
        groupInfo.addIntProperty("version");
        groupInfo.addIntProperty("status");
        groupInfo.addIntProperty("created");
        groupInfo.addIntProperty("updated");
    }

    private static void addMessage(Schema schema){
        Entity message = schema.addEntity("MessageEntity");
        message.setTableName("Message");
        message.setClassNameDao("MessageDao");
        message.setJavaPackage(entityPath);

        message.implementsSerializable();
        message.addIdProperty().autoincrement();
        Property msgProId = message.addIntProperty("msgId").notNull().getProperty();
        message.addIntProperty("fromId");
        message.addIntProperty("toId").notNull();
        // 是不是需要添加一个sessionkey标示一下，登陆的用户在前面
        Property sessionPro  = message.addStringProperty("sessionKey").notNull().getProperty();
        message.addStringProperty("content").notNull();
        message.addIntProperty("msgType").notNull();
        message.addIntProperty("displayType").notNull();

        message.addIntProperty("status").index();
        message.addIntProperty("created").index();
        message.addIntProperty("updated");

        Index index = new Index();
        index.addProperty(msgProId);
        index.addProperty(sessionPro);
        index.makeUnique();
        message.addIndex(index);

        message.setHasKeepSections(true);
    }

    private static void addSessionInfo(Schema schema){
        Entity sessionInfo = schema.addEntity("SessionEntity");
        sessionInfo.setTableName("Session");
        sessionInfo.setClassNameDao("SessionDao");
        sessionInfo.setJavaPackage(entityPath);

        //point to userId/groupId need sessionType 区分
        sessionInfo.addIdProperty().autoincrement();
        sessionInfo.addStringProperty("sessionKey").unique().notNull(); //.unique()
        sessionInfo.addIntProperty("peerId");
        sessionInfo.addIntProperty("peerType");
        sessionInfo.addIntProperty("latestMsgType");
        sessionInfo.addIntProperty("latestMsgId");
        sessionInfo.addStringProperty("latestMsgData");
        sessionInfo.addIntProperty("talkId");
        sessionInfo.addIntProperty("created");
        sessionInfo.addIntProperty("updated");
        sessionInfo.setHasKeepSections(true);
    }
}
