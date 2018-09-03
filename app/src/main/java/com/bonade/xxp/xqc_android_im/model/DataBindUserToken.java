package com.bonade.xxp.xqc_android_im.model;

public class DataBindUserToken {

    public DataIamToken data;

    public DataIamToken getData() {
        return data;
    }

    public void setData(DataIamToken data) {
        this.data = data;
    }

    public static class DataIamToken {

        private String access_token;
        private long expires_in;
        private String refresh_token;
        private String message;
        private String hasBindingPhone;
        private String hasPassword;
        private String scope;
        private String token_type;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(long expires_in) {
            this.expires_in = expires_in;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getHasBindingPhone() {
            return hasBindingPhone;
        }

        public void setHasBindingPhone(String hasBindingPhone) {
            this.hasBindingPhone = hasBindingPhone;
        }

        public String getHasPassword() {
            return hasPassword;
        }

        public void setHasPassword(String hasPassword) {
            this.hasPassword = hasPassword;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }
    }
}
