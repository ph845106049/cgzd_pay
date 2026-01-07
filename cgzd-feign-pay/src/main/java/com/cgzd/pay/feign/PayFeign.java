package com.cgzd.pay.feign;


import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.pay.dto.PayIos;
import com.cgzd.pay.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "cgzd-pay",path= PayFeign.PATH,contextId = "pay")
public interface PayFeign {

    String PATH = "/pay";

    /**
     * App端支付
     * @param aPayOrder 支付定单详情
     * @return 支付URL
     * @throws CgzdBaseException 自定义错误
     */
    @PostMapping("/app")
    BaseResponse<Object> appPay(@RequestBody PayOrderDTO aPayOrder) throws CgzdBaseException;

    /**
     * h5端支付
     * @param aPayOrder 支付定单详情
     * @return 支付URL
     * @throws CgzdBaseException 自定义错误
     */
    @PostMapping("/h5")
    BaseResponse<Object> h5Pay(@RequestBody PayOrderDTO aPayOrder) throws CgzdBaseException;

    @PostMapping("/h5mweb")
    BaseResponse<Object> h5PayMweb(@RequestBody PayOrderDTO aPayOrder) throws CgzdBaseException;

    /**
     * @Description h5支付前获取openId(用于授权)
     * @param: aPayOrder
     * @return: com.cgzd.common.pojo.base.vo.BaseResponse<java.lang.Object>
     * @Author: hongyu.guo
     * @Date: 2021/11/11 9:41
     */
    @PostMapping("/h5GetOrderId")
    BaseResponse<Object> h5GetOrderId(@RequestBody PayOrderDTO aPayOrder) throws CgzdBaseException;

    /**
     * 后端离线支付
     * @param aPayOrders 支付定单详情
     * @return 支付结果
     * @throws CgzdBaseException 自定义错误
     */
    @PostMapping("/sys")
    BaseResponse<String> pay(@RequestBody List<PayOrderDTO> aPayOrders) throws CgzdBaseException;

    /**
     * native支付
     * @param aPayOrder 支付定单详情
     * @return 支付URL
     * @throws CgzdBaseException 自定义错误
     */
    @PostMapping("/native")
    BaseResponse<String> nativePay(@RequestBody PayOrderDTO aPayOrder) throws CgzdBaseException;

}
