package com.cgzd.pay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pay.ali")
public class AliPayConfig {

    // 应用归属商户UID
    private String pid;

    // APPID
    private String appId;

    // 网关
    private String gatewayUrl;

    // 收款支付宝账号，以2088开头由16位纯数字组成的字符串，一般情况下收款账号就是签约账号
    private String sellerId;

    // 商户的私钥
    private String privateKey;

    // 应用公钥
    private String publicKey;

    // 支付宝的公钥
    private String aliPublicKey;

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private String notifyUrl;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private String returnUrl;

    // 签名方式
    private String signType;

    // 字符编码格式 目前支持utf-8
    private String charset;

    // 仅支持JSON
    private String format;

    // 支付类型 ，无需修改
    private String paymentType;

    // 当面付最大查询次数
    private String maxQueryRetry;

    // 当面付最大查询间隔
    private String queryDuration;

    // 当面付最大撤销次数
    private String maxCancelRetry;

    // 当面付最大撤销撤销间隔(毫秒)
    private String cancelDuration;

    // 交易保障线程第一次调度延迟
    private String heartbeatDelay;

    // 交易保障线程第一次调度间隔(秒)
    private String heartbeatDuration;

    //二维码宽度
    private Integer qrCodeWidth;

    //二维码高度
    private Integer qrCodeHeight;

    //代理商支付回调
    private String agentNotifyUrl;

    public String getPid() {
        return pid;
    }

    public AliPayConfig setPid(String aPid) {
        pid = aPid;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public AliPayConfig setAppId(String aAppId) {
        appId = aAppId;
        return this;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public AliPayConfig setGatewayUrl(String aGatewayUrl) {
        gatewayUrl = aGatewayUrl;
        return this;
    }

    public String getSellerId() {
        return sellerId;
    }

    public AliPayConfig setSellerId(String aSellerId) {
        sellerId = aSellerId;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public AliPayConfig setPrivateKey(String aPrivateKey) {
        privateKey = aPrivateKey;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public AliPayConfig setPublicKey(String aPublicKey) {
        publicKey = aPublicKey;
        return this;
    }

    public String getAliPublicKey() {
        return aliPublicKey;
    }

    public AliPayConfig setAliPublicKey(String aAliPublicKey) {
        aliPublicKey = aAliPublicKey;
        return this;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public AliPayConfig setNotifyUrl(String aNotifyUrl) {
        notifyUrl = aNotifyUrl;
        return this;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public AliPayConfig setReturnUrl(String aReturnUrl) {
        returnUrl = aReturnUrl;
        return this;
    }

    public String getSignType() {
        return signType;
    }

    public AliPayConfig setSignType(String aSignType) {
        signType = aSignType;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public AliPayConfig setCharset(String aCharset) {
        charset = aCharset;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public AliPayConfig setFormat(String aFormat) {
        format = aFormat;
        return this;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public AliPayConfig setPaymentType(String aPaymentType) {
        paymentType = aPaymentType;
        return this;
    }

    public String getMaxQueryRetry() {
        return maxQueryRetry;
    }

    public AliPayConfig setMaxQueryRetry(String aMaxQueryRetry) {
        maxQueryRetry = aMaxQueryRetry;
        return this;
    }

    public String getQueryDuration() {
        return queryDuration;
    }

    public AliPayConfig setQueryDuration(String aQueryDuration) {
        queryDuration = aQueryDuration;
        return this;
    }

    public String getMaxCancelRetry() {
        return maxCancelRetry;
    }

    public AliPayConfig setMaxCancelRetry(String aMaxCancelRetry) {
        maxCancelRetry = aMaxCancelRetry;
        return this;
    }

    public String getCancelDuration() {
        return cancelDuration;
    }

    public AliPayConfig setCancelDuration(String aCancelDuration) {
        cancelDuration = aCancelDuration;
        return this;
    }

    public String getHeartbeatDelay() {
        return heartbeatDelay;
    }

    public AliPayConfig setHeartbeatDelay(String aHeartbeatDelay) {
        heartbeatDelay = aHeartbeatDelay;
        return this;
    }

    public String getHeartbeatDuration() {
        return heartbeatDuration;
    }

    public AliPayConfig setHeartbeatDuration(String aHeartbeatDuration) {
        heartbeatDuration = aHeartbeatDuration;
        return this;
    }

    public Integer getQrCodeWidth() {
        return qrCodeWidth;
    }

    public AliPayConfig setQrCodeWidth(Integer qrCodeWidth) {
        this.qrCodeWidth = qrCodeWidth;
        return this;
    }

    public Integer getQrCodeHeight() {
        return qrCodeHeight;
    }

    public AliPayConfig setQrCodeHeight(Integer qrCodeHeight) {
        this.qrCodeHeight = qrCodeHeight;
        return this;
    }

    public String getAgentNotifyUrl() {
        return agentNotifyUrl;
    }

    public AliPayConfig setAgentNotifyUrl(String agentNotifyUrl) {
        this.agentNotifyUrl = agentNotifyUrl;
        return this;
    }
}
