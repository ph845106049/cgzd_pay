package com.cgzd.pay.service;

import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.pay.dto.PayOrderDTO;

/**
 * @author cgzd
 * @version 1.0
 * @ClassName: PayServiceV1
 * @description: 支付接口
 * @date 2022/3/30 16:03
 */
public interface PayServiceV1 {

    /**
     * @description: 和币支付
     * @author cgzd
     * @date 2022/3/30 16:12
     * @version 1.0
     */
    BaseResponse<Boolean> hebi(PayOrderDTO payOrderDTO);
}