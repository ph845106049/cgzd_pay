package com.cgzd.pay.enums;

import com.cgzd.common.base.Do.BaseEnum;

/** 订单状态
 * @author gaoyi
 */
public enum  Status implements BaseEnum {

    /**
     * 待支付
     */
    PAYMENT(0,"待支付"),
    /**
     * 支付成功
     */
    SUCCESS(1,"支付成功"),
    /**
     * 取消支付
     */
    CANCEL(2,"取消支付"),
    /**
     * 支付失败
     */
    FAIL(3,"支付失败");

    private int code;
    private String value;


    Status(int aCode, String aValue) {
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
