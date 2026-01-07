package com.cgzd.pay.enums;

import com.cgzd.common.base.Do.BaseEnum;

/**
 * 支付类型枚举
 * @author gaoyi
 */
public enum Type implements BaseEnum {

    /**
     * 线上支付-APP
     */
    APP(0,"APP"),

    /**
     * 线上支付- H5
     */
    H5(1,"H5"),

    /**
     * 线下支付-  SYS
     */
    SYS(2,"线下"),

    /**
     * 扫码支付
     */
    NATIVE(3,"扫码支付"),

    /**
     * IOS支付
     */
    IOS(4,"苹果支付"),

    /**
     * 0元支付
     */
    ZERO_PAY(5,"0元支付"),

    /**
     * H5非微信浏览器
     */
    H5_MWEB(6,"H5非微信浏览器");


    private int code;
    private String value;


    Type(int aCode, String aValue) {
        code = aCode;
        value = aValue;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
