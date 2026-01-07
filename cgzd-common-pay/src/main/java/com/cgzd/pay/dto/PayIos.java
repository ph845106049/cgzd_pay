package com.cgzd.pay.dto;

import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: PayIos
 * @description: TODO
 * @date 2021/12/23 19:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayIos {

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
    private Type payType;

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
     * 支付渠道方：1-课程支付，2-代理商支付
     */
    private Integer sourceType;
    /**
     * 购买凭证
     */
    private String receipt;

    /**
     * base64校验体
     */
    private String payload;
    /**
     * base64校验体
     */
    private String transactionId;

    /**
     * 用户id
     */
    private Long userId;
}