package com.cgzd.pay.consumer;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.pay.dao.PayMapper;
import com.cgzd.pay.dto.CallBackDTO;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: HebiMqConsumer
 * @description: 和币扣款成功后修改订单状态
 * @date 2022/3/30 17:17
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${topic.hebiTopic}",consumerGroup = "${topic.hebiGroup}")
public class HebiMqConsumer implements RocketMQListener<String> {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${topic.updateOrderTopic}")
    private String updateOrderTopic;

    @Resource
    private PayMapper payMapper;

    @Override
    public void onMessage(String msg) {
        log.info("和币扣款成功后接收到的MQ ==========================MQ==========================================> : {}", msg);
        CallBackDTO callBackDTO = JSONObject.parseObject(msg, CallBackDTO.class);
        List<Pay> pay = payMapper.getPay(callBackDTO.getMainId(),5);
        if (CollectionUtils.isEmpty(pay)){
            Pay aPay = new Pay();
            aPay.setOrderId(callBackDTO.getMainId());
            if (2==callBackDTO.getPayStatus()) {
                aPay.setStatus(Status.SUCCESS);
            }else {
                aPay.setStatus(Status.FAIL);
            }
            aPay.setTimeExpire(LocalDateTime.now().plusMinutes(30));
            payMapper.save(aPay);
        }
        callBackDTO.setOrderStatus(3);
        callBackDTO.setPayChannel(Method.HEMEIBEI.getCode())
                .setPayTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        log.info("和币支付成功通知订单==================> : {}", JSONObject.toJSONString(callBackDTO));
        //和币支付成功，通知MQ更新订单状态
        rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));
    }
}