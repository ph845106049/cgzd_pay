package com.cgzd.pay.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.Enum.CodeConsEnum;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.pay.config.WeChatAppConfig;
import com.cgzd.pay.dao.PayMapper;
import com.cgzd.pay.dto.CallBackDTO;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.enums.Type;
import com.cgzd.pay.service.PayType;
import com.cgzd.pay.utils.QRCodeUtil;
import com.cgzd.pay.utils.WechatPayUtil;
import com.github.wxpay.sdk.WXPayUtil;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 微信支付
 *
 * @author gaoyi
 */
@PayType(Method.WEI_PAY)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WeiXinPayService extends AbstractPayService {

    private final WechatPayUtil wechatPayUtil;

    private final PayMapper payMapper;

    private final WeChatAppConfig appConfig;

    private final RocketMQTemplate rocketMQTemplate;

    @Value("${topic.updateOrderTopic}")
    private String updateOrderTopic;


    /**
     * @Description 微信app下单操作
     * @param: aPayOrder
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/2 16:42
     */
    @Override
    public Object pay(PayOrder aPayOrder) throws CgzdBaseException {
        log.info("微信下单请求参数：{}",JSONObject.toJSONString(aPayOrder));
        //首先判断下是否是非微信浏览器的支付
        if (Type.H5_MWEB.getValue().equals(aPayOrder.getType().getValue())) {
            log.info("微信下单H5_MWEB请求");
            return h5PayMweb(aPayOrder);
        }
        log.info("微信下单跳跃H5请求请求");
        Object mObj = orderQuery(aPayOrder);
        log.info("微信下单orderQuery{}",JSONObject.toJSONString(mObj));
        if (!ObjectUtils.isEmpty(mObj)){
            if(aPayOrder.getType().equals(Type.NATIVE)){
                HashMap<Object, Object> resultMap = new HashMap<>();
                resultMap.put("codeUrl",mObj);
                return resultMap;
            }else{
                return mObj;
            }
        }
        switch (aPayOrder.getType()) {
            case APP:
                return app(aPayOrder);
            case H5:
                return h5(aPayOrder);
            case NATIVE:
                return nativeDetail(aPayOrder);
            default:
                return null;
        }
    }

    private Object h5PayMweb(PayOrder aPayOrder) {
        return getWeChatPayVo(aPayOrder, new HashMap<String, String>());
//        Map<String, String> mOrderquery = wechatPayUtil.h5PayMweb(aPayOrder);
    }

    private Object orderQuery(PayOrder aPayOrder){
        log.info("orderQuery校验获取到的参数{}",JSONObject.toJSONString(aPayOrder));
        Map<String, String> mOrderquery = wechatPayUtil.orderquery(aPayOrder);
        log.info("获取到的orderQuery{}",JSONObject.toJSONString(mOrderquery));
        if (mOrderquery.containsKey("result_code") && "FAIL".toLowerCase()
                .equals(mOrderquery.get("result_code").toLowerCase())){
            return null;
        }
        if (mOrderquery.containsKey("trade_state") &&
                "NOTPAY".toLowerCase().equals(mOrderquery.get("trade_state").toLowerCase())){
            List<Pay> mPays = payMapper.getPayForOrderId(aPayOrder.getOrderId());
//            List<Pay> mPays = payMapper.getPayForOrderIdAndType(aPayOrder.getOrderId(),aPayOrder.getType().getCode());
            if (!CollectionUtils.isEmpty(mPays)){
                return mPays.stream().filter(p -> !CollectionUtils.isEmpty(p.getProperty()) &&
                        p.getProperty().containsKey("pay"))
                        .map(p -> p.getProperty().get("pay")).findFirst().orElse(null);

            }
        }
        return null;
    }


    /**
     * @Description 微信支付回调接口，接收通知成功必须通知微信已成功接收
     * @param: request
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/3 9:24
     */
    @Override
    public String callback(HttpServletRequest request) {
        log.info("-------------微信支付回调业务处理--------------");
        Map<String, String> map = WechatPayUtil.payCallBack(request);
        //微信返回
        if ("SUCCESS".equals(map.get("return_code"))) {
            log.info("---------微信{}回调成功，参数信息：{}--------", map.get("trade_type"), map);
            //验签
            if (!wechatPayUtil.verifySign(map)) {
                log.error("微信验签失败");
                return wechatPayUtil.responseWechatPay("FAIL", "error");
            }
            //查询订单ID
            String outTradeNo = map.get("out_trade_no");
            List<Pay> listPay = this.getPay(Long.valueOf(outTradeNo), Method.WEI_PAY);
            if (CollectionUtils.isEmpty(listPay)) {
                log.error("微信{}支付回调失败，原因：订单不存在！ 订单ID:{}", map.get("trade_type"), outTradeNo);
                return wechatPayUtil.responseWechatPay("FAIL", "error");
            }
            //业务结果为 SUCCESS
            Pay pay = listPay.get(0);
            if ("SUCCESS".equals(map.get("result_code"))) {
                log.info("-----------微信{}支付结果为成功,参数{}------------", map.get("trade_type"), map);
                //根据订单号查询当前订单是否处理过（指的是订单状态不是待支付状态），处理过的直接给微信返回SUCCESS
                int count = this.payMapper.selectSysPayByOrderIdAndStatus(outTradeNo, Status.PAYMENT.getCode());
                if (count > 0) {
                    log.info("----当前订单{}已处理，直接给微信返回SUCCESS", outTradeNo);
                    //表示当前订单已经处理过，直接给微信返回SUCCESS
                    return wechatPayUtil.responseWechatPay("SUCCESS", "OK");
                }
                //更新订单状态为支付成功
                HashMap<String, Object> hashMap = Maps.newHashMap();
                hashMap.put("callback",map);
                pay.setPrepayId(map.get("transaction_id"))
                        .setStatus(Status.SUCCESS)
                        .setPayTime(LocalDateTime.parse(WechatPayUtil.getDateTime(map.get("time_end")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .setMsg("SUCCESS")
                        .setProperty(hashMap);
                this.payMapper.save(pay);

                //支付成功，更新订单状态
                CallBackDTO callBackDTO = new CallBackDTO();
                callBackDTO.setMainId(Long.valueOf(map.get("out_trade_no")))
                        .setOrderStatus(3)
                        .setPayAmount(Long.valueOf(map.get("total_fee")))
                        .setPayChannel(Method.WEI_PAY.getCode())
                        .setPayStatus(3)
                        .setPayTime(WechatPayUtil.getDateTime(map.get("time_end")))
                        .setReceiptNum(map.get("transaction_id"));

                log.info("{}支付成功，通知MQ更新订单状态，参数：{}",map.get("trade_type"),JSONUtil.toJsonStr(callBackDTO));
                //支付成功，通知MQ更新订单状态
                rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));

                return wechatPayUtil.responseWechatPay("SUCCESS", "OK");
            } else {
                //业务结果为 FAIL
                log.error("-----------微信{}支付结果为失败,参数{}------------", map.get("trade_type"), map);
                //更新订单状态为支付失败
                HashMap<String, Object> hashMap = Maps.newHashMap();
                hashMap.put("callback",map);
                pay.setPrepayId(map.get("transaction_id"))
                        .setStatus(Status.FAIL)
                        .setPayTime(LocalDateTime.parse(WechatPayUtil.getDateTime(map.get("time_end")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .setMsg(String.format("%s--%s", map.get("err_code"), map.get("err_code_des")))
                        .setProperty(hashMap);
                this.payMapper.save(pay);
                //更新订单表(  1:待支付  2:支付中 3:支付成功 4:支付失败）
                CallBackDTO callBackDTO = new CallBackDTO();
                callBackDTO.setMainId(Long.valueOf(map.get("out_trade_no")))
                        .setPayStatus(4)
                        .setPayTime(DateUtil.now())
                        .setPayChannel(Method.WEI_PAY.getCode())
                        .setReceiptNum(map.get("transaction_id"));
                //支付失败，通知MQ更新订单状态
                log.info("{}支付失败，通知MQ更新订单状态，参数：{}",map.get("trade_type"),JSONUtil.toJsonStr(callBackDTO));
                rocketMQTemplate.convertAndSend(updateOrderTopic, JSONUtil.toJsonStr(callBackDTO));

                return wechatPayUtil.responseWechatPay("FAIL", "error");
            }

        } else {
            log.error("---------微信回调失败-----------");
            return wechatPayUtil.responseWechatPay("FAIL", "error");
        }
    }

    /**
     * @Description app支付（下单操作）
     * @param: aPayOrder
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/2 17:20
     */
    private Object app(PayOrder aPayOrder) {
        return getWeChatPayVo(aPayOrder, new HashMap<String, String>());
    }

    /**
     * @Description 下单操作
     * @param: aPayOrder
     * @param: tradeType    交易类型
     * @return: com.cgzd.common.pojo.pay.vo.WeChatPayVo
     * @Author: hongyu.guo
     * @Date: 2021/11/4 16:08
     */
    private Object getWeChatPayVo(PayOrder aPayOrder,Map<String, String> params) {
        log.info("-----调用微信{}统一下单接口-----", aPayOrder.getType());
        //商品描述
        params.put("body", aPayOrder.getDescription());
        //商户订单号
        params.put("out_trade_no", String.valueOf(aPayOrder.getOrderId()));
        //总金额
        params.put("total_fee", String.valueOf(aPayOrder.getAmount()));

        //调用微信支付，下单操作
        Map<String, String> map = wechatPayUtil.placeOrder(params, aPayOrder);
        //以下字段在return_code为SUCCESS的时候有返回
        if (Objects.equals(map.get("return_code"), "SUCCESS") && Objects.equals(map.get("result_code"), "SUCCESS")) {
            log.info("--------微信{}支付下单成功--------", aPayOrder.getType());
            //返回给前端的数据
            HashMap<String, String> paraMap = Maps.newHashMap();
            switch (aPayOrder.getType()){
                case NATIVE:nativePay(map,paraMap);
                break;
                case APP: appPay(map, paraMap);
                break;
                case H5: jsapiPay(map, paraMap);
                break;
                case H5_MWEB: h5Mweb(map, paraMap);
                break;
                default: break;
            }

            return paraMap;

        } else {
            log.info("--------微信{}支付下单失败--------", aPayOrder.getType());
            //微信返回的错误信息
            String errCodeDes = map.get("err_code_des");
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_01, errCodeDes);
        }
    }

    private void h5Mweb(Map<String, String> map, HashMap<String, String> paraMap) {
        try {
            paraMap.putAll(map);
        } catch (Exception e) {
            log.error("微信H5非微信浏览器扫码登录：{}", e.toString());
            throw new CgzdBaseException("微信支付异常!");
        }
    }

    /**
     * @Description  app支付
     * @param: tradeType    支付类型
     * @param: map
     * @param: paraMap
     * @return: void
     * @Author: hongyu.guo
     * @Date: 2021/11/12 16:05
     */
    private void appPay(Map<String, String> map, HashMap<String, String> paraMap) {
        //应用Id
        paraMap.put("appid", map.get("appid"));
        //商户号
        paraMap.put("partnerid", map.get("mch_id"));
        //预支付交易会话标识 prepay_id，在return_code和result_code都为SUCCESS的时候有返回
        paraMap.put("prepayid", map.get("prepay_id"));
        //随机字符串
        paraMap.put("noncestr", map.get("nonce_str"));
        // 时间戳，需要转为秒
        paraMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        // 固定字段，保留，不可修改
        paraMap.put("package", "Sign=WXPay");
        try {
            //生成签名
            paraMap.put("sign", WXPayUtil.generateSignature(paraMap, appConfig.getPrivateKey()));
        } catch (Exception e) {
            log.error("微信 APP 下单成功后，加密数据失败：{}", e);
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_VERIFY);
        }
    }

    /**
     * @Description  JSAPI支付
     * @param: tradeType 支付类型
     * @param: map
     * @param: paraMap
     * @return: void
     * @Author: hongyu.guo
     * @Date: 2021/11/12 16:07
     */
    private void jsapiPay(Map<String, String> map, HashMap<String, String> paraMap) {
        //应用Id(注意这里是大写，app是小写)
        paraMap.put("appId", map.get("appid"));
        //随机字符串(注意这里是大写，app是小写)
        paraMap.put("nonceStr", map.get("nonce_str"));
        // 时间戳，需要转为秒(注意这里是大写，app是小写)
        paraMap.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        //统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=
        paraMap.put("package", "prepay_id=" + map.get("prepay_id"));
        //设置(签名方式)
        paraMap.put("signType", "MD5");
        try {
            //生成签名
            paraMap.put("paySign", WXPayUtil.generateSignature(paraMap, appConfig.getPrivateKey()));
        } catch (Exception e) {
            log.error("微信 jsapi下单成功后，加密数据失败：{}",e);
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_VERIFY);
        }
    }

    /**
     * @description 扫码支付
     * @author wn
     * @param map
     * @updateTime 2021/11/30
    */
    private void nativePay(Map<String, String> map,HashMap<String, String> paraMap) {
        try {
            //生成二维码并返回base64
            paraMap.put("codeUrl",QRCodeUtil.getQRCodeBase64(map.get("code_url"),appConfig.getQrCodeWidth(), appConfig.getQrCodeHeight()));
        } catch (Exception e) {
            log.error("微信 扫码 下单成功后，获取二维码链接失败：{}", e);
            throw new CgzdBaseException("获取二维码链接失败");
        }
    }

    /**
     * @Description h5（JSAPI）支付
     * @param: aPayOrder
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/2 17:20
     */
    private Object h5(PayOrder aPayOrder) {
        //这里的code指的是openId
        if (StringUtils.isEmpty(aPayOrder.getCode())) {
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_FIELD);
        }
        HashMap<String, String> params = Maps.newHashMap();
        params.put("openid", aPayOrder.getCode());
        return getWeChatPayVo(aPayOrder, params);
    }

    /**
     * @description 扫码支付
     * @author wn
     * @param aPayOrder
     * @updateTime 2021/11/30
     * @return: java.lang.Object
    */
    private Object nativeDetail(PayOrder aPayOrder) {
//        if (StringUtils.isEmpty(aPayOrder.getGoodId())) {
//            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_GOODID);
//        }
        HashMap<String, String> params = Maps.newHashMap();
        params.put("product_id", aPayOrder.getOrderId().toString());
        return getWeChatPayVo(aPayOrder, params);
    }
}
