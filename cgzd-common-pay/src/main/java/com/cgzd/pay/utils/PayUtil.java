package com.cgzd.pay.utils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>支付工具类<p/>
 * @author yangjp
 *
 */
@Slf4j
public class PayUtil {

    public static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    public static final String TRADE_FINISHED = "TRADE_FINISHED";
    public static final String TRADE_CLOSED = "TRADE_CLOSED";
    public static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
    public static final String TRADE_STATUS = "trade_status";

    /**
     * 配置支付宝支付客户端
     *
     * @param gatewayUrl 网关地址
     * @param appId AppId
     * @param privateKey 私钥
     * @param format 格式化
     * @param charset 编码格式
     * @param alipayPublicKey 公匙
     * @param signType 加密类型
     * @return 阿里支付
     */
    public static AlipayClient alipayClient(String gatewayUrl,
                                            String appId,
                                            String privateKey,
                                            String format,
                                            String charset,
                                            String alipayPublicKey,
                                            String signType) {
        if (StringUtils.isBlank(gatewayUrl) || StringUtils.isBlank(appId) || StringUtils.isBlank(privateKey) ||
                StringUtils.isBlank(format) || StringUtils.isBlank(charset) || StringUtils.isBlank(alipayPublicKey) || StringUtils.isBlank(signType)) {

            throw new RuntimeException("配置支付宝支付客户端失败, 存在空参数!");
        }
        return setAliPayClient(gatewayUrl, appId, privateKey, format, charset, alipayPublicKey, signType, null, null);
    }

    /**
     * 配置支付宝支付客户端公共方法
     *
     * @param gatewayUrl 网关地址
     * @param appId AppId
     * @param privateKey 私钥
     * @param format 格式化
     * @param charset 编码格式
     * @param alipayPublicKey 公匙
     * @param signType 加密类型
     * @param encryptKey 加密key
     * @param encryptType 加密类型
     * @return 支付宝，支付客户端
     */
    public static AlipayClient setAliPayClient(String gatewayUrl, String appId, String privateKey, String format, String charset, String alipayPublicKey, String signType, String encryptKey, String encryptType) {
        return new DefaultAlipayClient(gatewayUrl, appId, privateKey, format, charset, alipayPublicKey, signType, encryptKey, encryptType);
    }

    public static Map<String,String> getAlipayCallback(HttpServletRequest request){
        Map<String,String> mParams = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            mParams.put(name, valueStr);
        }
        return mParams;
    }
}
