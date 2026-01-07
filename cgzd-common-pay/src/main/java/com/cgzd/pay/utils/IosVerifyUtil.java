package com.cgzd.pay.utils;


import javax.net.ssl.*;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

/**
 * 苹果IAP内购验证工具类
 *
 * 官网:https://developer.apple.com/documentation/storekit/in-app_purchase
 * 参考：https://blog.csdn.net/lbd_123/article/details/87276204
 * @author xiaoqiang
 * @date 2020-05-11 14:37
 */
public class IosVerifyUtil {
    private static class TrustAnyTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * 苹果服务器验证
     *
     * @param receipt
     *            账单
     * @url 要验证的地址
     * @return null 或返回结果 沙盒 https://sandbox.itunes.apple.com/verifyReceipt
     *
     *  21000App Store无法读取你提供的JSON数据
     *  21002 订单数据不符合格式
     *  21003 订单无法被验证
     *  21004 你提供的共享密钥和账户的共享密钥不一致
     *  21005 订单服务器当前不可用
     *  21006 订单是有效的，但订阅服务已经过期。当收到这个信息时，解码后的收据信息也包含在返回内容中
     *  21007 订单信息是测试用（sandbox），但却被发送到产品环境中验证
     *  21008 订单信息是产品环境中使用，但却被发送到测试环境中验证
     */
    public static String buyAppVerify(String receipt,int type) {
        String url_sandbox = "https://sandbox.itunes.apple.com/verifyReceipt";
        String url_verify = "https://buy.itunes.apple.com/verifyReceipt";
        // 环境判断 线上/开发环境用不同的请求链接
        String url = "";
        if(type==0){
            // 沙盒测试
            url = url_sandbox;
        }else{
            // 线上测试
            url = url_verify;
        }

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            URL console = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestProperty("Proxy-Connection", "Keep-Alive");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            BufferedOutputStream hurlBufOus = new BufferedOutputStream(conn.getOutputStream());
            // 拼成固定的格式传给平台
            String str = String.format(Locale.CHINA, "{\"receipt-data\":\"" + receipt + "\"}");
            hurlBufOus.write(str.getBytes());
            hurlBufOus.flush();

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (Exception ex) {
            System.out.println("苹果服务器异常");
            ex.printStackTrace();
        }
        return null;
    }

}
