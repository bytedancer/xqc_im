package com.bonade.xxp.xqc_android_im.model;

public class DataUserInfo {

    private UserInfoLogin data;

    public UserInfoLogin getData() {
        return data;
    }

    public void setData(UserInfoLogin data) {
        this.data = data;
    }

    public static class UserInfoLogin {

        private UserInfo userInfo;//用户信息

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        public static class UserInfo {

            private String account;//账号
            private String changer2Status;//钱包状态：浦发二类户为SALARY，银联钱包是WALLET_CUP,例如：SALARY;WALLET_CUP
            private String email;
            private long id;
            private String identityNo;//身份证号码
            private int isActive;//是否已经激活
            private String isIdentity;//是否已经实名认证
            private int isSync;//是否已同步恩布用户：0为没有同步，1为已同步（平台新用户）
            private String mobile;//手机
            private String name;//姓名
            private String password;//密码
            private String salaryState;//是否开通二类户，true为开通，false为未开通
            private String spdbAccount1;//浦发一类户账号
            private String spdbAccount1BankId;//浦发一类户银行Id
            private String spdbAccount1BankName;//浦发一类户银行名称

            private String spdbAccount2;//浦发二类户账号
            private String spdbAccountId;//浦发AccountID
            private String spdbChangerStatus;//二类户归集状态：1转入二类户|2不转入二类户
            private String spdbOpenid;//浦发OpenId

            private String thirdCustId;//电子钱包编号
            private String userLogo;//头像路径
            private String userName;//用户名
            private String userType;//用户类型：0:运营人员、1:企业人员

            private String walletState;//电子钱包开通状态，1：已开通；0：未开通；null：未知
            private String xqcAccount;//薪起程账号

            public String getAccount() {
                return account;
            }

            public void setAccount(String account) {
                this.account = account;
            }

            public String getChanger2Status() {
                return changer2Status;
            }

            public void setChanger2Status(String changer2Status) {
                this.changer2Status = changer2Status;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getIdentityNo() {
                return identityNo;
            }

            public void setIdentityNo(String identityNo) {
                this.identityNo = identityNo;
            }

            public int getIsActive() {
                return isActive;
            }

            public void setIsActive(int isActive) {
                this.isActive = isActive;
            }

            public String getIsIdentity() {
                return isIdentity;
            }

            public void setIsIdentity(String isIdentity) {
                this.isIdentity = isIdentity;
            }

            public int getIsSync() {
                return isSync;
            }

            public void setIsSync(int isSync) {
                this.isSync = isSync;
            }

            public String getMobile() {
                return mobile;
            }

            public void setMobile(String mobile) {
                this.mobile = mobile;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getSalaryState() {
                return salaryState;
            }

            public void setSalaryState(String salaryState) {
                this.salaryState = salaryState;
            }

            public String getSpdbAccount1() {
                return spdbAccount1;
            }

            public void setSpdbAccount1(String spdbAccount1) {
                this.spdbAccount1 = spdbAccount1;
            }

            public String getSpdbAccount1BankId() {
                return spdbAccount1BankId;
            }

            public void setSpdbAccount1BankId(String spdbAccount1BankId) {
                this.spdbAccount1BankId = spdbAccount1BankId;
            }

            public String getSpdbAccount1BankName() {
                return spdbAccount1BankName;
            }

            public void setSpdbAccount1BankName(String spdbAccount1BankName) {
                this.spdbAccount1BankName = spdbAccount1BankName;
            }

            public String getSpdbAccount2() {
                return spdbAccount2;
            }

            public void setSpdbAccount2(String spdbAccount2) {
                this.spdbAccount2 = spdbAccount2;
            }

            public String getSpdbAccountId() {
                return spdbAccountId;
            }

            public void setSpdbAccountId(String spdbAccountId) {
                this.spdbAccountId = spdbAccountId;
            }

            public String getSpdbChangerStatus() {
                return spdbChangerStatus;
            }

            public void setSpdbChangerStatus(String spdbChangerStatus) {
                this.spdbChangerStatus = spdbChangerStatus;
            }

            public String getSpdbOpenid() {
                return spdbOpenid;
            }

            public void setSpdbOpenid(String spdbOpenid) {
                this.spdbOpenid = spdbOpenid;
            }

            public String getThirdCustId() {
                return thirdCustId;
            }

            public void setThirdCustId(String thirdCustId) {
                this.thirdCustId = thirdCustId;
            }

            public String getUserLogo() {
                return userLogo;
            }

            public void setUserLogo(String userLogo) {
                this.userLogo = userLogo;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            public String getUserType() {
                return userType;
            }

            public void setUserType(String userType) {
                this.userType = userType;
            }

            public String getWalletState() {
                return walletState;
            }

            public void setWalletState(String walletState) {
                this.walletState = walletState;
            }

            public String getXqcAccount() {
                return xqcAccount;
            }

            public void setXqcAccount(String xqcAccount) {
                this.xqcAccount = xqcAccount;
            }
        }
    }
}
