package com.project.gulimallmember.member.constant;


public enum MemberEnum {

    CREATE_NEW(0,"待付款"),
    PAYED(1,"已付款"),
    SENDed(2,"已发货"),
    RECIEVED(3,"已签收"),
    CANELED(4,"已取消"),
    SERVICING(5,"正在售后中"),
    SERVICED(6,"售后完成");

    int code;

    String msg;

    MemberEnum(int code, String msg) {

        this.code = code;
        this.msg = msg;

    }

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
}
