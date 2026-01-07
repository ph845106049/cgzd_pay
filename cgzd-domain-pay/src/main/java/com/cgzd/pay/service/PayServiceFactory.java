package com.cgzd.pay.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cgzd.common.Enum.CodeConsEnum;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.common.util.CodeCons;
import com.cgzd.feign.order.feign.OrderFeign;
import com.cgzd.order.common.dto.CheckOrderDto;
import com.cgzd.pay.config.WeChatAppConfig;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Type;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付工厂类
 */
@Component
public class PayServiceFactory implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final Map<Method, IPayService> payMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PayServiceFactory.class);

    @Autowired
    private WeChatAppConfig appConfig;

    @Autowired
    private OrderFeign orderFeign;
    @Override
    public void afterPropertiesSet() {
        Map<String, IPayService> beanMap = applicationContext.getBeansOfType(IPayService.class);
        for (String key : beanMap.keySet()) {
            this.payMap.put(Objects.requireNonNull(AnnotationUtils.findAnnotation(beanMap.get(key)
                    .getClass(), PayType.class)).value(), beanMap.get(key));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 支付接口
     * @param aType 支付方式
     * @param aPayOrder 支付定单
     * @return 支付URL
     * @throws CgzdBaseException 自定义错误
     */
    @Transactional
    public Object pay(Type aType, PayOrder aPayOrder) throws CgzdBaseException {
        log.info("appPay 入参 aPayOrder：{}",JSONObject.toJSONString(aPayOrder));
        CheckOrderDto checkOrderDto = new CheckOrderDto();
        BeanUtils.copyProperties(aPayOrder,checkOrderDto);
        checkOrderDto.setMainId(aPayOrder.getOrderId().toString());
        //校验订单，订单id和金额必传
        BaseResponse<Boolean> result = orderFeign.checkOrder(checkOrderDto);
        log.info("订单校验入参：{}，结果：{}",JSONObject.toJSONString(checkOrderDto),JSONObject.toJSONString(result));
        if (Objects.nonNull(result) && result.getCode() == CodeCons.SUCCESS_CODE && result.getData()) {
            IPayService payService;
            if (aType == Type.SYS){
                payService = payMap.get(Method.OFFLINE);
            }else if(aPayOrder.getAmount()==0) {
                log.info("订单支付金额为0,0元支付逻辑开始处理 amount:{},orderId:{}",aPayOrder.getAmount(),aPayOrder.getOrderId());
                payService = payMap.get(aPayOrder.getMethod());
                payService.checkNoMinZero(aPayOrder);
                payService.savePayZero(aPayOrder);
                HashMap<String, Object> ruMap = Maps.newHashMap();
                ruMap.put("type",Type.ZERO_PAY);
                ruMap.put("payStatus",true);
                log.info("0元支付处理完成，ruMap:{}",ruMap);
                return ruMap;
            }else {
                payService = payMap.get(aPayOrder.getMethod());
            }
            if (ObjectUtils.isEmpty(payService)){
                log.error("获取支付方式出错，code = {}",aPayOrder.getMethod());
                throw new CgzdBaseException(CodeConsEnum.PAY_TYPE_SUPPORT);
            }
            PayOrder mPayOrder = payService.check(aPayOrder);
            mPayOrder.setType(aType);
            if(!ObjectUtils.isEmpty(mPayOrder)){
                Object mPay = payService.pay(mPayOrder);
                if (!ObjectUtils.isEmpty(mPay)){
                    if (CollectionUtils.isEmpty(mPayOrder.getProperty())){
                        mPayOrder.setProperty(new HashMap<>());
                    }
                    mPayOrder.getProperty().put("pay",mPay);
                    payService.savePay(mPayOrder);
                    return mPay;
                }
            }

        }
        return null;
    }

    /**
     * 回调方法
     * @param aMethod 支付方式
     * @param request 回调参数
     * @return 返回结果
     */
    public String notify(Method aMethod, HttpServletRequest request){
        IPayService mPayService = payMap.get(aMethod);
        if (!ObjectUtils.isEmpty(mPayService)){
            return mPayService.callback(request);
        }
        return "";
    }

    /**
     * 线下支付回调
     * @param aPayOrder 定单信息
     * @return 返回结果
     */
    public String notify(PayOrder aPayOrder){
        IPayService mPayService = payMap.get(Method.OFFLINE);
        if (!ObjectUtils.isEmpty(mPayService)){
            return mPayService.sysCallback(aPayOrder);
        }
        return "";
    }

    /**
     * @Description jsapi支付前需通过code获取openId
     * @param: code
     * @return: java.lang.Object
     * @Author: hongyu.guo
     * @Date: 2021/11/11 9:59
     */
    public Object h5GetOrderId(String code) {
        if(StringUtils.isEmpty(code)){
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_FIELD);
        }
        //请求获取openId
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=%s",
                appConfig.getH5AppId(), appConfig.getSecret(), code, "authorization_code");
        log.info("----------请求获取openId,url:{}------------", url);
        String result = HttpUtil.get(url);
        if (StringUtils.isEmpty(result)) {
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_OPENID);
        }
        log.info("-----通过code获取openId，返回参数：{}----------", result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String openid = jsonObject.getString("openid");
        if (StringUtils.isEmpty(openid)) {
            throw new CgzdBaseException(CodeConsEnum.PAY_WECHAT_PAY_OPENID);
        }
        return openid;
    }
}
