package com.cgzd.pay.utils;

import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.Enum.CodeConsEnum;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.pay.config.WeChatAppConfig;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.enums.SourceType;
import com.cgzd.pay.enums.Type;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname WechatPayUtil
 * @Author hongyu.guo
 * @Description 微信支付相关工具
 * @Date 2021/11/2 15:48
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
public class WechatPayUtil {

    private final WeChatAppConfig appConfig;

    private void buildMap(Map<String, String> params, PayOrder aPayOrder){
        switch (aPayOrder.getType()){
            case APP: params.put("appid", appConfig.getAppId());
                break;
            case H5: params.put("appid", appConfig.getH5AppId());
                break;
            case H5_MWEB: params.put("appid", appConfig.getH5AppId());
                break;
            case NATIVE: params.put("appid",appConfig.getNativeAppId());
                break;
            default:break;
        }
        //商户号
        params.put("mch_id", appConfig.getMchId());
        //随机字符串
        params.put("nonce_str", WXPayUtil.generateNonceStr());

        //异步回调地址
        params.put("notify_url", appConfig.getNotifyUrl());

        //终端IP(调用微信支付API的机器IP)
        params.put("spbill_create_ip", appConfig.getSpbillCreateIp());
        //交易类型
        params.put("trade_type", getWechatType(aPayOrder.getType()));
        try {
            //转为大写并获取md5加密后的签名
            String value = WXPayUtil.generateSignature(params, appConfig.getPrivateKey());
            params.put("sign", value);
        } catch (Exception e) {
            log.error("微信生成签名异常：{}", e);
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_VERIFY);
        }
    }

    private String getWechatType(Type aType){
        switch (aType){
            case APP: return "APP";
            case H5: return "JSAPI";
            case H5_MWEB: return "MWEB";
            case NATIVE: return Type.NATIVE.name();
            default:return "";
        }
    }


    public Map<String, String> orderquery(PayOrder aPayOrder){
        Map<String,String> mMap = new HashMap<>();
        mMap.put("out_trade_no",String.valueOf(aPayOrder.getOrderId()));
        switch (aPayOrder.getType()){
            case APP: mMap.put("appid", appConfig.getAppId());
                break;
            case H5: mMap.put("appid", appConfig.getH5AppId());
                break;
            case NATIVE: mMap.put("appid",appConfig.getNativeAppId());
                break;
            default:break;
        }

        //商户号
        mMap.put("mch_id", appConfig.getMchId());
        //随机字符串
        mMap.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //转为大写并获取md5加密后的签名
            String value = WXPayUtil.generateSignature(mMap, appConfig.getPrivateKey());
            mMap.put("sign", value);
        } catch (Exception e) {
            log.error("微信生成签名异常：{}", e);
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_VERIFY);
        }

        //将拼接的参数转换为xml格式
        String xml = XmlUtil.mapToXmlStr(mMap, "xml");
        log.info("微信统一查询接口参数：{}", JSONObject.toJSONString(xml));
        //请求微信统一下单接口
        String body = HttpRequest.post(appConfig.getOrderquery()).body(xml).execute().body();
        log.info("微信统一查询接口返回数据：{}", body);

        Map<String, String> map = null;
        try {
            map = WXPayUtil.xmlToMap(body);
        } catch (Exception e) {
            log.error("xml转Map失败：{}", e);
        }
        return map;
    }

    /**
     * @Description 下单操作
     * @param: params
     * @param: 交易类型
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/10/31 12:31
     */
    public Map<String, String> placeOrder(Map<String, String> params, PayOrder aPayOrder) {
        buildMap(params, aPayOrder);
        //将拼接的参数转换为xml格式
        String xml = XmlUtil.mapToXmlStr(params, "xml");
        log.info("微信统一下单接口参数：{}", JSONObject.toJSONString(xml));
        //请求微信统一下单接口
        String body = HttpRequest.post(appConfig.getPayUrl()).body(xml).execute().body();
        log.info("微信统一下单接口返回数据：{}", body);

        Map<String, String> map = null;
        try {
            map = WXPayUtil.xmlToMap(body);
        } catch (Exception e) {
            log.error("xml转Map失败：{}", e);
        }
        return map;
    }


    /**
     * @Description 微信支付回调结果参数解析
     * @param: request
     * @return: java.util.Map<java.lang.String, java.lang.String>
     * @Author: hongyu.guo
     * @Date: 2021/11/1 16:38
     */
    public static Map<String, String> payCallBack(HttpServletRequest request) {
        // 读取参数
        try {
            InputStream inputStream;
            StringBuffer sb = new StringBuffer();
            inputStream = request.getInputStream();
            String readLine;
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((readLine = in.readLine()) != null) {
                sb.append(readLine);
            }
            in.close();
            inputStream.close();
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(sb.toString());
            log.info("微信支付回调参数：{}", JSONObject.toJSONString(xmlToMap));
            return xmlToMap;
        } catch (Exception e) {
            log.error("微信支付回调结果参数解析失败：{}", e);
            return null;
        }
    }

    /**
     * @param returnMsg 返回信息
     * @Description 微信支付回调后返回给微信的参数
     * @param: returnCode SUCCESS/FAIL 两种
     * @param: 给微信返回的信息
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/1 17:06
     */
    @SneakyThrows
    public String responseWechatPay(String returnCode, String returnMsg) {
        HashMap<String, String> returnData = new HashMap<>();
        returnData.put("return_code", returnCode);
        returnData.put("return_msg", returnMsg);
        return XmlUtil.mapToXmlStr(returnData);
    }

    /**
     * @Description 验签
     * @param: map
     * @return: java.lang.Boolean
     * @Author: hongyu.guo
     * @Date: 2021/11/1 14:44
     */
    public Boolean verifySign(Map<String, String> map) {
        if (Objects.isNull(map)) {
            log.info("签名为空：{}", map);
            return false;
        }
        log.info("服务器返回的签名:{}", map.get("sign"));
        //验签操作
        boolean sign = Boolean.FALSE;
        try {
            sign = WXPayUtil.isSignatureValid(map, appConfig.getPrivateKey());
        } catch (Exception e) {
            log.error("微信验签异常：{}", e);
        }
        return sign;
    }


    /**
     * @Description 日期转换成localDateTime
     * @param: time
     * @return: java.lang.String
     * @Author: hongyu.guo
     * @Date: 2021/11/3 18:05
     */
    public static String getDateTime(String time) {
        //格式转换成 yyyy-MM-dd HH:mm:ss
        String format = String.format("%s-%s-%s %s:%s:%s", time.substring(0, 4), time.substring(4, 6)
                , time.substring(6, 8), time.substring(8, 10), time.substring(10, 12), time.substring(12, 14));
        return format;
    }

    public Map<String, String> h5PayMweb(PayOrder aPayOrder) {
        Map<String,String> mMap = new HashMap<>();
        mMap.put("out_trade_no",String.valueOf(aPayOrder.getOrderId()));
        //公众账号ID
        mMap.put("appid", appConfig.getH5AppId());
        //商户号
        mMap.put("mch_id", appConfig.getMchId());
        //随机字符串
        mMap.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //转为大写并获取md5加密后的签名
            String value = WXPayUtil.generateSignature(mMap, appConfig.getPrivateKey());
            mMap.put("sign", value);
        } catch (Exception e) {
            log.error("微信生成签名异常：{}", e);
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_VERIFY);
        }
        //商品描述
        mMap.put("body", aPayOrder.getDescription());

        //商户订单号
        mMap.put("out_trade_no", String.valueOf(aPayOrder.getOrderId()));

        //总金额
        mMap.put("total_fee", String.valueOf(aPayOrder.getAmount()));

        //将拼接的参数转换为xml格式
        String xml = XmlUtil.mapToXmlStr(mMap, "xml");
        log.info("微信统一查询接口参数：{}", JSONObject.toJSONString(xml));
        //请求微信统一下单接口
        String body = HttpRequest.post(appConfig.getOrderquery()).body(xml).execute().body();
        log.info("微信统一查询接口返回数据：{}", body);

        Map<String, String> map = null;
        try {
            map = WXPayUtil.xmlToMap(body);
        } catch (Exception e) {
            log.error("xml转Map失败：{}", e);
        }
        return map;
    }
}
