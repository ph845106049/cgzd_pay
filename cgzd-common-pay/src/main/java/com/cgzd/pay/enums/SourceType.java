package com.cgzd.pay.enums;

import com.cgzd.common.base.Do.BaseEnum;

/**
 * @author wangning
 * @date 2021/12/6
 * @description TODO
 */
public enum SourceType  implements BaseEnum {
    COURSE(1,"课程支付"),
    /**
     * 其它
     */
    AGENT(2,"代理商支付");

    private int code;
    private String value;


    SourceType(int aCode, String aValue) {
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
