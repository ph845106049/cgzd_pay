package com.cgzd.pay.controller;

import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.Enum.dictionary.GenderTypeEnum;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.common.pojo.gateway.vo.AuthenticationResult;
import com.cgzd.pay.dto.PayIos;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.service.IosService;
import com.cgzd.pay.service.PayServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: PayControllerV1
 * @description: 支付
 * @date 2022/3/30 15:59
 */
@Slf4j
@RestController
@RequestMapping("/iap")
public class PayControllerV1 {

    @Autowired
    private PayServiceV1 payServiceV1;

    @Autowired
    private IosService iosService;

    /**
     * @description: 获取订单状态枚举Status 1：根据code获取value,不传默认根据value获取code
     * @author cgzd
     * @date 2022/4/6 10:22
     * @version 1.0
     */
    @GetMapping("/status")
    public BaseResponse<HashMap<String, String>> getPayStatus(@PathVariable("type") String type) {
        HashMap<String, String> map = new HashMap<>();
        Status[] enumConstants = Status.class.getEnumConstants();
        for (Status enumConstant : enumConstants) {
            if ("1".equals(type)) {
                map.put(String.valueOf(enumConstant.getCode()), enumConstant.getValue());
            }else {
                map.put(enumConstant.getValue(), String.valueOf(enumConstant.getCode()));
            }
        }
        return BaseResponse.success(map);
    }

    /**
     * 苹果内购校验
     *
     * @param
     * @param
     * @param
     * @return
     */
    @PostMapping("/iospay")
    public synchronized BaseResponse iosPay(@RequestBody PayIos iPayNotifyVO) {
        log.info("苹果内购校验开始，交易ID：" + iPayNotifyVO.getTransactionId() + " base64校验体：" + iPayNotifyVO.getPayload());
        log.info("苹果内购参数======" + JSONObject.toJSONString(iPayNotifyVO));
        return iosService.iosPay(iPayNotifyVO);
    }

    /**
     * @description: 和币支付
     * @author cgzd
     * @date 2022/3/30 16:11
     * @version 1.0
     */
    @PostMapping("/hebi")
    public BaseResponse<Boolean> hebi(@RequestBody PayOrderDTO payOrderDTO) {
        log.info("和币支付接收到的参数：{}", JSONObject.toJSONString(payOrderDTO));
        return payServiceV1.hebi(payOrderDTO);
    }
}