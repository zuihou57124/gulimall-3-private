package com.project.gulimallcart.constant;

/**
 * @author qcw
 */
public class AuthConst {

    public static String SMS_CODE_PREFIX = "sms:code";

    public static String LOGIN_USER = "user";

    public enum SmsEnum{

        SMS_SEND_NOTIME(10001,"发送频率过高"),
        SMS_SEND_PHONENUM_NULL(10002,"请输入手机号");

        int code;

        String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        SmsEnum(int code, String msg) {

            this.code = code;
            this.msg = msg;

        }
    }

}
