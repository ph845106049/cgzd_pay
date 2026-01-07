package com.cgzd.pay.dto;


import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.SourceType;
import com.cgzd.pay.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 支付订单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayOrder {

    /**
     * 订单ID
     */
    private Long orderId;
    /**
     * 订单描述
     */
    private String description;
    /**
     * 金额 (分)
     */
    private Long amount;
    /**
     * 支付类型
     */
    private Type type;

    /**
     * 微信支付 jsapi所用
     */
    private String code;
    /**
     * 支付方式
     */
    private Method method;
    /**
     * 商品ID native支付使用，max=String(32)
     */
    private String goodId;
    /**
     * 扩展属性
     */
    private Map<String, Object> property;
    /**
     * 账户余额
     */
    private Long accountBalance;
    /**
     * 积分余额
     */
    private Long accountPoint;
    private Integer sourceType;
}
