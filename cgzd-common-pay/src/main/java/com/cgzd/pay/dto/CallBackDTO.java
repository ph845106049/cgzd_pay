package com.cgzd.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author wangning
 * @date 2021/12/21
 * @description TODO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CallBackDTO {
    /**
     * 主订单id（用于多项子订单统一支付）
     */
    private Long mainId;

    /**
     * 订单状态  1.审批中 2.待支付 3.支付成功 4.代付尾款 5.取消 6.审批驳回 7.退课中 8.已退课 9.退费中 10.已退费
     */
    private Integer orderStatus;

    /**
     * 支付状态  1:待支付  2:支付中 3:支付成功 4:支付失败
     */
    private Integer payStatus;

    /**
     * 支付通道  0 = 微信  1 = 支付宝  2 = 校付通  3 = 现金  4 = 对公转账 5 = 其它
     */
    private Integer payChannel;

    /**
     * 实付金额(分)
     */
    private Long payAmount;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 支付编号
     */
    private String receiptNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * IOS 订单ID
     */
    private String iosOrderId;
}
