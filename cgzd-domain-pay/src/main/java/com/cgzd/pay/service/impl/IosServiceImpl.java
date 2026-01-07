package com.cgzd.pay.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.common.util.CodeCons;
import com.cgzd.feign.order.feign.OrderFeign;
import com.cgzd.order.common.dto.CheckOrderDto;
import com.cgzd.pay.dao.PayMapper;
import com.cgzd.pay.dto.CallBackDTO;
import com.cgzd.pay.dto.PayIos;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.service.IosService;
import com.cgzd.pay.utils.IosVerifyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: iosServiceImpl
 * @description: TODO
 * @date 2021/12/23 19:21
 */
@Slf4j
@Service
public class IosServiceImpl implements IosService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private OrderFeign orderFeign;

    @Resource
    private PayMapper payMapper;

    @Value("${topic.updateOrderTopic}")
    private String updateOrderTopic;

    @Value("${topic.updateAccountTopic}")
    private String updateAccountTopic;

    @Override
    public BaseResponse iosPay(PayIos iPayNotifyVO) {
        if (!Objects.isNull(iPayNotifyVO.getPayType()) && 1==iPayNotifyVO.getPayType().getCode()) {
            CheckOrderDto checkOrderDto = new CheckOrderDto();
            BeanUtils.copyProperties(iPayNotifyVO,checkOrderDto);
            checkOrderDto.setMainId(iPayNotifyVO.getOrderId().toString());
            //校验订单，订单id和金额必传
            BaseResponse<Boolean> result = orderFeign.checkOrderV2(checkOrderDto);
            log.info("订单校验入参：{}，结果：{}",JSONObject.toJSONString(checkOrderDto),JSONObject.toJSONString(result));
            if (Objects.isNull(result) && result.getCode() != CodeCons.SUCCESS_CODE && !result.getData()) {
                return null;
            }
        }
        //线上环境验证   type值  为0的时候是 沙盒环境、1为 线上环境
        CallBackDTO callBackDTO = new CallBackDTO();
        //ios订单id
        callBackDTO.setIosOrderId(iPayNotifyVO.getTransactionId());
        Pay aPay = new Pay();
        int ZERO = 0;
        int FOUR = 4;
        String verifyResult = IosVerifyUtil.buyAppVerify(iPayNotifyVO.getPayload(), ZERO);
        if (verifyResult == null) {
            return BaseResponse.error(ZERO, "苹果验证失败，返回数据为空");
        } else {
            log.info("线上，苹果平台返回JSON:" + verifyResult);
            JSONObject appleReturn = JSONObject.parseObject(verifyResult);
            String states = appleReturn.getString("status");
            //无数据则沙箱环境验证
            if ("21007".equals(states)) {
                verifyResult = IosVerifyUtil.buyAppVerify(iPayNotifyVO.getPayload(), ZERO);
                log.info("沙盒环境，苹果平台返回JSON:" + verifyResult);
                appleReturn = JSONObject.parseObject(verifyResult);
                states = appleReturn.getString("status");
            }
            log.info("苹果平台返回值：appleReturn" + appleReturn);
            // 前端所提供的收据是有效的    验证成功
            if (states.equals("0")) {
                String receipt = appleReturn.getString("receipt");
                JSONObject returnJson = JSONObject.parseObject(receipt);
                String inApp = returnJson.getString("in_app");
                List<HashMap> inApps = JSONObject.parseArray(inApp, HashMap.class);
                if (!CollectionUtils.isEmpty(inApps)) {
                    ArrayList<String> transactionIds = new ArrayList<String>();
                    for (HashMap app : inApps) {
                        transactionIds.add((String) app.get("transaction_id"));
                        iPayNotifyVO.setTransactionId((String) app.get("transaction_id"));
                    }
                    //交易列表包含当前交易，则 认为交易成功
                    if (transactionIds.contains(iPayNotifyVO.getTransactionId())) {
                        log.info("交易成功，新增并处理订单");
                        //支付成功，更新订单状态
                        iosStates(iPayNotifyVO, callBackDTO, aPay, verifyResult, 3, Status.SUCCESS);
                        log.info("苹果支付成功,通知MQ更新订单状态处理完成{}", iPayNotifyVO.getOrderId());
                        log.info("订单{},IOS支付成功!", iPayNotifyVO.getOrderId());
                        return BaseResponse.success();
                    } else {
                        iosStates(iPayNotifyVO, callBackDTO, aPay, verifyResult, FOUR, Status.FAIL);
                        log.info("交易失败,transactionIds：{},TransactionId:{}", transactionIds, iPayNotifyVO.getTransactionId());
                        return BaseResponse.error(ZERO, "当前交易不在交易列表中");
                    }
                } else {
                    iosStates(iPayNotifyVO, callBackDTO, aPay, verifyResult, FOUR, Status.FAIL);
                    log.info("获取到的交易列表为空,inApps:{}", inApps);
                    return BaseResponse.error(ZERO, "未能获取获取到交易列表");
                }
            } else {
                iosStates(iPayNotifyVO, callBackDTO, aPay, verifyResult, FOUR, Status.FAIL);
                log.info("支付失败，错误码：" + states);
                return BaseResponse.error(ZERO, "支付失败");
            }
        }
    }

    private void iosStates(PayIos iPayNotifyVO, CallBackDTO callBackDTO, Pay aPay, String verifyResult, int i, Status status) {
        callBackDTO.setMainId(Long.parseLong(iPayNotifyVO.getOrderId().toString()))
                .setOrderStatus(i)
                .setPayAmount(iPayNotifyVO.getAmount())
                .setUserId(Objects.isNull(iPayNotifyVO.getUserId())?1:iPayNotifyVO.getUserId())
                .setPayChannel(Method.IOS.getCode())
                .setPayStatus(i)
                .setPayTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        log.info("苹果支付成功，通知MQ更新订单状态{}", callBackDTO);
        //支付成功，通知MQ更新订单状态
        if (1==iPayNotifyVO.getPayType().getCode()) {
            rocketMQTemplate.convertAndSend(updateAccountTopic, JSONUtil.toJsonStr(callBackDTO));
        }else{
            rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));
        }
        aPay.setOrderId(iPayNotifyVO.getOrderId());
        aPay.setMsg(verifyResult);
        aPay.setStatus(status);
        aPay.setTimeExpire(LocalDateTime.now());
        payMapper.save(aPay);
    }
}