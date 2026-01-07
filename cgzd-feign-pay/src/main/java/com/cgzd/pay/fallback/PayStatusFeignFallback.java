package com.cgzd.pay.fallback;

import com.cgzd.pay.feign.PayStatusFeign;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: PayStatusFeignFallback
 * @description: TODO
 * @date 2022/4/6 10:32
 */
@Slf4j
@Component
public class PayStatusFeignFallback implements FallbackFactory<PayStatusFeign> {
    @Override
    public PayStatusFeign create(Throwable cause) {
        return type -> null;
    }
}