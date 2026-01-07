package com.cgzd.pay.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.Enum.CodeConsEnum;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.feign.agency.feign.AgentFeign;
import com.cgzd.feign.order.feign.AppOrderFeign;
import com.cgzd.pay.convert.PayOrderConvert;
import com.cgzd.pay.dao.PayMapper;
import com.cgzd.pay.dto.CallBackDTO;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.enums.Type;
import com.cgzd.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 支付抽象类
 */
@Slf4j
public abstract class AbstractPayService implements IPayService {

    @Resource
    private PayOrderConvert payOrderConvert;
    @Resource
    private PayMapper payMapper;

    @Resource
    private AgentFeign agentFeign;

    @Resource
    private AppOrderFeign appOrderFeign;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Value("${topic.updateOrderTopic}")
    private String updateOrderTopic;

    /**
     * @Description 校验的字段
     * @param: aPayOrder
     * @return: void
     * @Author: hongyu.guo
     * @Date: 2021/11/5 11:16
     */
    protected void basicCheck(PayOrder aPayOrder) throws CgzdBaseException {
        if (ObjectUtils.isEmpty(aPayOrder.getOrderId())){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_01);
        }
        if (ObjectUtils.isEmpty(aPayOrder.getDescription())){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_02);
        }
        if (ObjectUtils.isEmpty(aPayOrder.getAmount()) || aPayOrder.getAmount() <1){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_03);
        }
    }

    protected List<Pay> getPayForOrderId(Long aId){
        return payMapper.getPayForOrderId(aId);
    }

    protected List<Pay> getPay(Long aOrderId, Method aMethod){
        return payMapper.getPay(aOrderId,aMethod.getCode());
    }

    @Override
    public void notify(Pay aPay) {
        if (aPay.getPayType() == Type.NATIVE){
            BaseResponse<?> mCallback = agentFeign.callback(aPay.getOrderId());
        }
    }

    @Override
    public PayOrder check(PayOrder aPayOrder) throws CgzdBaseException {
        basicCheck(aPayOrder);
        return aPayOrder;
    }
    @Override
    public PayOrder checkNoMinZero(PayOrder aPayOrder) throws CgzdBaseException {
        if (ObjectUtils.isEmpty(aPayOrder.getOrderId())){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_01);
        }
        if (ObjectUtils.isEmpty(aPayOrder.getDescription())){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_02);
        }
        if (ObjectUtils.isEmpty(aPayOrder.getAmount()) || aPayOrder.getAmount() <0){
            throw new CgzdBaseException(CodeConsEnum.PAY_FIELD_03);
        }
        return aPayOrder;
    }

    @Override
    public boolean savePay(PayOrder aPayOrder) {
        //如果有单号的订单则认为重复提交，直接返回支付信息即可
        List<Pay> mPay = getPay(aPayOrder.getOrderId(), aPayOrder.getMethod());
        if (!CollectionUtils.isEmpty(mPay)){
            return true;
        }
        //转换类型，同时设置支付状态为待支付，并且更新默认失效时间为 当前时间加上30分钟。
        return save(payOrderConvert.convert(aPayOrder)
                .setStatus(Status.PAYMENT)
                .setTimeExpire(LocalDateTime.now().plusMinutes(30)));
    }
    @Override
    public boolean savePayZero(PayOrder aPayOrder) {
        //如果有单号的订单则认为重复提交，直接返回支付信息即可
        List<Pay> mPay = getPay(aPayOrder.getOrderId(), aPayOrder.getMethod());
        if (!CollectionUtils.isEmpty(mPay)){
            return true;
        }
        //转换类型，0元支付时，直接将订单状态设置成支付成功
        save(payOrderConvert.convert(aPayOrder)
                .setStatus(Status.SUCCESS)
                .setTimeExpire(LocalDateTime.now()));

        //支付成功，更新订单状态
        CallBackDTO callBackDTO = new CallBackDTO();
        callBackDTO.setMainId(aPayOrder.getOrderId())
                .setOrderStatus(3)
                .setPayAmount(Long.valueOf(aPayOrder.getAmount()))
                .setPayChannel(Method.NO_PAY_CHANNEL.getCode())
                .setPayStatus(3)
                .setPayTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));

        log.info("0元支付成功，通知MQ更新订单状态");
        //支付成功，通知MQ更新订单状态
        rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));
        return true;
    }

    @Override
    public boolean save(Pay aPay) {
        boolean save = payMapper.save(aPay) > 0;
        log.info("保存支付订单结果：{}",save);
        return save;
    }

    @Override
    public String sysCallback(PayOrder aPayOrder) {
        return null;
    }
}
