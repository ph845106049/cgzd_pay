package com.cgzd.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.common.util.JsonUtils;
import com.cgzd.feign.order.feign.AppOrderFeign;
import com.cgzd.pay.config.AliPayConfig;
import com.cgzd.pay.dto.CallBackDTO;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.service.PayType;
import com.cgzd.pay.utils.PayUtil;
import com.cgzd.pay.utils.QRCodeUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阿里实现支付类
 * @author gaoyi
 */
@PayType(Method.ALI_PAY)
@RefreshScope
public class AliPayService extends AbstractPayService {

    private final AliPayConfig aliPayConfig;

    private final String CALLBACK = "callback";
    private final String NOTIFY_ID = "notify_id";
    private final String APP_ID = "app_id";

    private AlipayClient alipayClient;
    private Lock lock = new ReentrantLock();

    private final Logger log = LoggerFactory.getLogger(AliPayService.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${topic.updateOrderTopic}")
    private String updateOrderTopic;

    @Value("${pay.ali.returnUrl}")
    private String returnUrl;

    private AlipayClient getClient(){
        if (ObjectUtils.isEmpty(alipayClient)){
            lock.lock();
            try {
                alipayClient = PayUtil.alipayClient(
                        aliPayConfig.getGatewayUrl(),
                        aliPayConfig.getAppId(),
                        aliPayConfig.getPrivateKey(),
                        aliPayConfig.getFormat(),
                        aliPayConfig.getCharset(),
                        aliPayConfig.getAliPublicKey(),
                        aliPayConfig.getSignType());
            }finally {
                lock.unlock();
            }
        }
        return alipayClient;

    }

    public AliPayService(AliPayConfig aAliPayConfig) {
        this.aliPayConfig = aAliPayConfig;
    }

    /**
     * 支付宝APP支付
     * @param aPayOrder 支付定单
     * @return 返回结果
     */
    private String app(PayOrder aPayOrder){
        log.info("-------------支付宝下单中------------");
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject(aPayOrder.getDescription());
        model.setOutTradeNo(String.valueOf(aPayOrder.getOrderId()));
        model.setTotalAmount(new BigDecimal(aPayOrder.getAmount())
                .divide(BigDecimal.valueOf(100), 2,RoundingMode.HALF_UP).toString());
        model.setTimeoutExpress("30m");
        request.setBizModel(model);
        try {
            AlipayTradeAppPayResponse response = getClient().sdkExecute(request);
            if (response.isSuccess()) {
                log.info("支付宝返回信息：{}",response.getBody());
                return response.getBody();
            } else {
                throw new CgzdBaseException(Integer.valueOf(response.getCode()),response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝支付失败：{}",e.getMessage(),e);
            throw new CgzdBaseException(Integer.valueOf(e.getErrCode()),e.getErrMsg());
        }
    }

    /**
     * 支付宝H5支付
     * @param aPayOrder 订单 信息
     * @return 返回结果
     */
    private String h5(PayOrder aPayOrder){
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject(aPayOrder.getDescription());
        model.setOutTradeNo(String.valueOf(aPayOrder.getOrderId()));
        model.setTotalAmount(new BigDecimal(aPayOrder.getAmount())
                .divide(BigDecimal.valueOf(100), 2,RoundingMode.HALF_UP).toString());
        model.setTimeoutExpress("30m");
        request.setBizModel(model);
        request.setReturnUrl(returnUrl);
        log.info("支付宝H5支付参数returnUrl--->{}",returnUrl);
        try {
            AlipayTradeWapPayResponse response = getClient().pageExecute(request);
            log.info("支付宝返回信息：{}",response.getBody());
            if (response.isSuccess()) {
                return response.getBody();
            } else {
                throw new CgzdBaseException(Integer.valueOf(response.getCode()),response.getMsg());
            }
        } catch (AlipayApiException e) {
            throw new CgzdBaseException(Integer.valueOf(e.getErrCode()),e.getErrMsg());
        }
    }

    /**
     * @description 扫码支付
     * @author wn
     * @param aPayOrder
     * @updateTime 2021/11/30
     * @return: java.lang.String
    */
    private String nativeDetail(PayOrder aPayOrder){
        log.info("-------支付宝{}下单中------",aPayOrder.getType().name());
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        //订单描述
        model.setSubject(aPayOrder.getDescription());
        //商户订单号
        model.setOutTradeNo(String.valueOf(aPayOrder.getOrderId()));
        //订单金额（单位：元）
        model.setTotalAmount(new BigDecimal(aPayOrder.getAmount())
                .divide(BigDecimal.valueOf(100), 2,RoundingMode.HALF_UP).toString());

        model.setTimeoutExpress("30m");
        request.setBizModel(model);
        try {
            AlipayTradePrecreateResponse response = getClient().execute(request);
            log.info("支付宝返回信息：{}",response.getBody());
            if (response.isSuccess()) {
                String qrCodeBase64 = QRCodeUtil.getQRCodeBase64(response.getQrCode(), aliPayConfig.getQrCodeWidth(), aliPayConfig.getQrCodeHeight());
                return qrCodeBase64;
            } else {
                throw new CgzdBaseException(Integer.valueOf(response.getCode()),response.getMsg());
            }
        } catch (AlipayApiException e) {
            throw new CgzdBaseException(Integer.valueOf(e.getErrCode()),e.getErrMsg());
        }
    }

    @Override
    public String pay(PayOrder aPayOrder) throws CgzdBaseException {
        log.info("支付宝支付请求参数如下：{}",JSONObject.toJSONString(aPayOrder));
        switch (aPayOrder.getType()){
            case APP:return app(aPayOrder);
            case H5: return h5(aPayOrder);
            case NATIVE: return nativeDetail(aPayOrder);
            default: return null;
        }
    }

    /**
     * 校验阿里支付回调合法性
     * @return 是否合法
     */
    private boolean checkParams(Pay mPay, Map<String,String> aParams){
        //如果回调的 App_ID 与本地配置不一至，认为是无效回调
        if (aParams.containsKey(APP_ID) &&
                !aliPayConfig.getAppId().equals(aParams.get(APP_ID))){
            return false;
        }
        //如果回调通知notify_id 已存在，则认为多次通知
        if(mPay.getProperty().containsKey(CALLBACK)){
            Map<String, String> mParams = (Map<String,String>) mPay.getProperty().get(CALLBACK);
            return !mParams.containsKey(NOTIFY_ID) || !mParams.get(NOTIFY_ID)
                    .equals(aParams.get(NOTIFY_ID));
        }
        return true;
    }

    @Override
    @Transactional
    public String callback(HttpServletRequest request) {
        Map<String, String> mParams = PayUtil.getAlipayCallback(request);
        log.info("支付宝回调参数如下：  {}", JsonUtils.writeString(mParams));
        try {
            if (!AlipaySignature.rsaCheckV1 (mParams,aliPayConfig.getAliPublicKey(),
                    aliPayConfig.getCharset(),
                    aliPayConfig.getSignType())){
                log.error("阿里支付回调被篡改 参数: {}", JsonUtils.writeString(mParams));
            }
        } catch (AlipayApiException aE) {
            log.error("阿里支付回调校验错误，原因",aE);
        }
        String mOrderId = mParams.get("out_trade_no");
        if (StringUtils.isEmpty(mOrderId)){
            log.error("阿里支付回调订单为空！");
        }
        List<Pay> mPay = getPayForOrderId(Long.parseLong(mOrderId));
        if (CollectionUtils.isEmpty(mPay)){
            log.error("阿里支付回调订单不存在！ 订单ID:{}",mOrderId);
            return "fail";
        }
        if(mPay.size() > 1){
            log.error("阿里支付回调订单大于1条！ 订单ID:{}",mOrderId);
        }
        Pay mPaySave = mPay.get(0);
        if (ObjectUtils.isEmpty(mPaySave.getProperty())){
            mPaySave.setProperty(new HashMap<>());
        }
        if (!checkParams(mPaySave,mParams)){
            return "success";
        }

        mPaySave.getProperty().put(CALLBACK,mParams);
        mPaySave.setPrepayId(mParams.get("trade_no"));
        if (PayUtil.TRADE_FINISHED.equals(mParams.get(PayUtil.TRADE_STATUS)) ||
            PayUtil.TRADE_SUCCESS.equals(mParams.get(PayUtil.TRADE_STATUS))){
            mPaySave.setStatus(Status.SUCCESS);
            mPaySave.setPayTime(LocalDateTime.now());
        }
        if (PayUtil.TRADE_CLOSED.equals(mParams.get(PayUtil.TRADE_STATUS))){
            mPaySave.setStatus(Status.CANCEL);
        }
        log.info("支付宝JSON数据：{}",JSONObject.toJSONString(mPaySave));
        if (save(mPaySave)){
            //更新订单表( 1:待支付  2:支付中 3:支付成功  4:关闭)
            String totalAmount = mParams.get("total_amount");
            BigDecimal multiply = new BigDecimal(totalAmount).multiply(new BigDecimal("100"));
            CallBackDTO callBackDTO = new CallBackDTO();
            callBackDTO.setMainId(Long.valueOf(mParams.get("out_trade_no")))
                    .setOrderStatus(3)
                    .setPayAmount(multiply.longValue())
                    .setPayChannel(Method.ALI_PAY.getCode())
                    .setPayStatus(3)
                    .setPayTime(mParams.get("gmt_payment"))
                    .setReceiptNum(mParams.get("trade_no"));
            //支付成功，通知MQ更新订单状态
            log.info("支付成功，通知MQ更新订单状态,参数：{}",JSONUtil.toJsonStr(callBackDTO));
            rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));
            return "success";
        }
        //更新订单表(  1:待支付  2:支付中 3:支付成功 4:支付失败）
        CallBackDTO backDTO = new CallBackDTO();
        backDTO.setMainId(Long.valueOf(mParams.get("out_trade_no")))
                .setPayStatus(4)
                .setPayTime(DateUtil.now())
                .setPayChannel(Method.ALI_PAY.getCode())
                .setReceiptNum(mParams.get("trade_no"));
        //支付失败，通知MQ更新订单状态
        log.info("支付失败，通知MQ更新订单状态，参数：{}",JSONUtil.toJsonStr(backDTO));
        rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(backDTO));
        return "failure";
    }
}
