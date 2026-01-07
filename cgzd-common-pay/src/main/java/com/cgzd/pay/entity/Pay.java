package com.cgzd.pay.entity;


import com.cgzd.common.base.Do.BaseEntity;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.enums.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付entity
 * @author gaoyi
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Pay extends BaseEntity {

    private static final long serialVersionUID = -1968301204208281096L;
    /**
     * 订单ID
     */
    private Long orderId;
    /**
     * 订单描述
     */
    private String orderDescription;
    /**
     * 失效时间
     */
    private LocalDateTime timeExpire;
    /**
     * 附加参数
     */
    private Map<String,Object> attach;
    /**
     * 订单金额(分)
     */
    private Long amount;
    /**
     * 支付类型
     */
    private Type payType;
    /**
     * 支付方式
     */
    private Method payMethod;
    /**
     * 预支付交易会话标识(支付宝，或微信返回支付单号)
     */
    private String prepayId;
    /**
     * 订单状态
     */
    private Status status;
    /**
     * 支付消息 支付消息，如失败原因，取消原因等
     */
    private String msg;
    /**
     * 凭证 ，如对公转账凭证，线下支付单号等
     */
    private String proof;
    /**
     * 其它参数
     */
    private Map<String,Object> property;

    private LocalDateTime payTime;

    /**
     * 支付渠道方：1-课程支付，2-代理商支付
     */
    private Integer sourceType;
}
