package com.bonade.xxp.xqc_android_im.util;

import android.util.Base64;

import java.security.PublicKey;

public class XqcPwdEncryptUtils {

    private static String md5Key = "xqc1254548787244";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFbuyHjN4+18OjRSyzOUli1Ic0\n" +
            "tD/ZkfoDeHHTD3S6v/sVnfczpafqz7fNXUFCyO7mJlM58IAHoHGvrjhCXs5E29yS\n" +
            "I62Qk5tC9g1ofrDcp9DLS3HsBZVfwqVlgtTPV64IUSMckP9ZPradAoyUY5oGXJhk\n" +
            "HP+MAEaHaIYaSlgmiQIDAQAB";


    public static String loginPwdEncrypt(String pwd) {
        try {
            String pwdMd5 = MD5Utils.md5(pwd + md5Key);
            String string = pwdMd5 + ";;;qqq" + FormatUtil.dateFormatAll(System.currentTimeMillis()) + ";;;qqq" + pwd;
            byte[] data = (string).getBytes();
            PublicKey publicKey = RSAUtils.getPublicKey(PUBLIC_KEY);
            byte[] encodedData = RSAUtils.encryptData(data, publicKey);
            String str = Base64.encodeToString(encodedData, Base64.DEFAULT);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
