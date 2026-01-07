package com.cgzd.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 支付定单
 * @author gaoyi
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PayOrderDTO implements Serializable {


    private static final long serialVersionUID = -4004883003590854493L;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 订单描述
     */
    private String description;
    /**
     * 订单金额(分)
     */
    private Long amount;
    /**
     * 支付方式  0 = 微信  1 = 支付宝  2 = 校付通  3 = 现金  4 = 对公转账 5 = 其它
     */
    private Integer method;
    /**
     * 其它未列举参数
     */
    private Map<String, Object> property;

    /**
     * 微信 jsapi支付所用
     */
    private String code;

    /**
     * 商品ID native支付使用，max=String(32)
     */
    private String goodId;

    /**
     * 支付渠道方：1-课程支付，2-代理商支付
     */
    private Integer sourceType;
    /**
     * 账户余额
     */
    private Long accountBalance;
    /**
     * 积分余额
     */
    private Long accountPoint;
    /**
     * 用户id
     */
    private Long userId;
}
