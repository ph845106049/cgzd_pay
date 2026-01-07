package com.cgzd.pay.feign;

import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.pay.fallback.PayStatusFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;

@FeignClient(value = "cgzd-pay", fallbackFactory = PayStatusFeignFallback.class)
public interface PayStatusFeign {

    
    /**
     * @description: 获取订单状态枚举Status 1：根据code获取value,不传默认根据value获取code
     * @author cgzd
     * @date 2022/4/6 10:36
     * @version 1.0
     */
    @GetMapping("/iap/status")
    BaseResponse<HashMap<String, String>> getPayStatus(@PathVariable("type") String type);
}
