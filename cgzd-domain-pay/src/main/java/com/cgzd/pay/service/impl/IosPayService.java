package com.cgzd.pay.service.impl;

import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.service.PayType;

import javax.servlet.http.HttpServletRequest;

@PayType(Method.IOS)
public class IosPayService extends AbstractPayService {


    @Override
    public String pay(PayOrder aPayOrder) throws CgzdBaseException {
        return null;
    }

    @Override
    public String callback(HttpServletRequest request) {
        return null;
    }
}
