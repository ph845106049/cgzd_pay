package com.cgzd.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.account.dto.SysAccountUserBalanceDTO;
import com.cgzd.common.message.center.pojo.dto.ConsultingMessageRecordDTO;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.common.util.CodeCons;
import com.cgzd.feign.account.feign.AccountFeign;
import com.cgzd.feign.message.center.feign.ConsultingMessageFeign;
import com.cgzd.feign.order.feign.OrderFeign;
import com.cgzd.order.common.dto.CheckOrderDto;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.service.PayServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: PayServiceV1Impl
 * @description: TODO
 * @date 2022/3/30 16:06
 */
@Slf4j
@Service
public class PayServiceV1Impl implements PayServiceV1 {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private AccountFeign accountFeign;

    @Autowired
    private ConsultingMessageFeign messageFeign;

    @Override
    public BaseResponse<Boolean> hebi(PayOrderDTO payOrderDTO) {
        try{
            messageFeign.insert(new ConsultingMessageRecordDTO());
        }catch (Exception e){

        }
        CheckOrderDto checkOrderDto = new CheckOrderDto();
        BeanUtils.copyProperties(payOrderDTO,checkOrderDto);
        checkOrderDto.setMainId(payOrderDTO.getOrderId());
        //校验订单，订单id和金额必传
        BaseResponse<Boolean> result = orderFeign.checkOrder(checkOrderDto);
        log.info("和币支付校验订单结果：{}", JSONObject.toJSONString(result));
        if (Objects.nonNull(result) && result.getCode() == CodeCons.SUCCESS_CODE && result.getData()) {
            SysAccountUserBalanceDTO balanceDTO = new SysAccountUserBalanceDTO();
            balanceDTO.setOrderId(Long.valueOf(payOrderDTO.getOrderId()));
            balanceDTO.setUserId(payOrderDTO.getUserId());
            balanceDTO.setPayAmount(payOrderDTO.getAmount());
            BaseResponse<Boolean> booleanResult = accountFeign.balanceConsume(balanceDTO);
            log.info("和币扣款结果：{}", JSONObject.toJSONString(booleanResult));
            if (Objects.nonNull(booleanResult) && booleanResult.getCode() == CodeCons.SUCCESS_CODE) {
                return BaseResponse.success(booleanResult.getData());
            }
        }
        return BaseResponse.error(CodeCons.ERROR_CODE,"和币支付失败!");
    }
}