package com.cgzd.pay.service;

import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.SourceType;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付抽象类
 * @author gaoyi
 */
public interface IPayService {

    /**
     * 支付
     * @param aPayOrder 支付订单
     * @return 返回订单的结果
     */
    Object pay(PayOrder aPayOrder) throws CgzdBaseException;

    /**
     * 校验参数
     * @param aPayOrder 支付订单
     * @return 是否校验成功
     * @throws CgzdBaseException 自定义错误
     */
    PayOrder check(PayOrder aPayOrder) throws CgzdBaseException;

    /**
     * 校验参数-支付金额不能小于0
     * @param aPayOrder 支付订单
     * @return 是否校验成功
     * @throws CgzdBaseException 自定义错误
     */
    PayOrder checkNoMinZero(PayOrder aPayOrder) throws CgzdBaseException;

    /**
     * 保存发起的支付定单
     * @param aPayOrder 发起的支付定单详情
     * @return 是否保存成功
     */
    boolean savePay(PayOrder aPayOrder);

    /**
     * 保存发起的支付定单(0元支付时使用)
     * @param aPayOrder 发起的支付定单详情
     * @return 是否保存成功
     */
    boolean savePayZero(PayOrder aPayOrder);

    /**
     * 保存订单信息
     * @param aPay 订单
     * @return 是否保存成功
     */
    boolean save(Pay aPay);

    /**
     * 支付回调
     * @param request 回调参数
     * @return 回调结果
     */
    String callback(HttpServletRequest request);

    /**
     * 线下支付回调
     * @param aPayOrder 回调订单详情
     * @return 返回结果
     */
    String sysCallback(PayOrder aPayOrder);

    /**
     * 通知订单修改状态
     * @param aPay 订单详情
     */
    void notify(Pay aPay);


}
