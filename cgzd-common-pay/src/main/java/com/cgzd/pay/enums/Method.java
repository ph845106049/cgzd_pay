package com.cgzd.pay.enums;

import com.cgzd.common.base.Do.BaseEnum;

/**
 *  支付方式枚举
 * @author gaoyi
 */
public enum Method implements BaseEnum {

    /**
     * 只做实现标记，没有业务实现（为了区分线上线下业务）
     */
    OFFLINE(-1,"线下支付"),
    /**
     * 微信支付
     */
    WEI_PAY(0,"微信"),
    /**
     * 支付宝支付
     */
    ALI_PAY(1,"支付宝"),
    /**
     * 校付通知付
     */
    XFT_PAY(2,"校付通"),
    /**
     * 现金支付
     */
    CASH(3,"现金"),
    /**
     * 对公转账
     */
    GIRO(4,"对公转账"),
    /**
     * 其它
     */
    OTHER(5,"其它"),

    IOS(6,"IOS"),

    /**
     * 和美贝支付
     */
    HEMEIBEI(7,"和美贝支付"),

    /**
     * 无支付渠道-用于0元支付或其他无需使用支付渠道时使用
     */
    NO_PAY_CHANNEL(99,"无支付渠道");

    private int code;
    private String value;


    Method(int aCode, String aValue) {
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
