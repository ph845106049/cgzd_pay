package com.cgzd.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname WeChatAppConfig
 * @Author hongyu.guo
 * @Description
 * @Date 2021/11/2 15:49
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "wechat")
public class WeChatAppConfig {

    /**
     * 微信下单地址
     */
    private String payUrl;

    /**
     * 微信定单查询接口
     */
    private String orderquery;

    /**
     * app支付应用Id
     */
    private String appId;

    /**
     * h5支付appId
     */
    private String h5AppId;

    /**
     * native支付appId
     */
    private String nativeAppId;

    /**
     * 商户号
     */
    private String mchId;
    /**
     * 终端IP
     */
    private String spbillCreateIp;
    /**
     * 异步回调地址
     */
    private String notifyUrl;
    /**
     * 密钥
     */
    private String privateKey;

    /**
     * 应用密钥AppSecret，在微信开放平台提交应用审核通过后获得(JSAPI支付所用)
     */
    private String secret;

    /**
     * 二维码宽度
     */
    private Integer qrCodeWidth;

    /**
     * 二维码高度
     */
    private Integer qrCodeHeight;

    /**
     * 代理商异步回调地址
     */
    private String agentNotifyUrl;

}
