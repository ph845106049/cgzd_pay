package com.cgzd.pay.controller;

import com.cgzd.pay.enums.Method;
import com.cgzd.pay.service.PayServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 回调
 */
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotifyController {

    private final PayServiceFactory payServiceFactory;

    /**
     * 阿里支付回调
     * @param request 回调参数
     * @return 回调结果
     */
    @PostMapping("/alipay")
    public String aliPayNotify(HttpServletRequest request){
        return payServiceFactory.notify(Method.ALI_PAY,request);
    }

    /**
     * @Description 微信支付回调接口，接收通知成功必须通知微信已成功接收
     * @param: request
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/3 9:22
     */
    @PostMapping("/wechatPayNotify")
    public String wechatPayNotify(HttpServletRequest request){
        return payServiceFactory.notify(Method.WEI_PAY,request);
    }

    /**
     * 校付通回调
     * @param request 回调参数
     * @return 回调结果
     */
    @PostMapping("/xft")
    public String xftPayNotify(HttpServletRequest request){
        return payServiceFactory.notify(Method.XFT_PAY,request);
    }
}
