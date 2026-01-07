package com.cgzd.pay.service;

import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.pay.dto.PayIos;

public interface IosService {

    /**
     * @description: Ios内购
     * @author cgzd
     * @date 2022/3/30 15:14
     * @version 1.0
     */
    BaseResponse iosPay(PayIos iPayNotifyVO);
}
