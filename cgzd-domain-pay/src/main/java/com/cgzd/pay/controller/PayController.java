package com.cgzd.pay.controller;


import com.cgzd.common.Enum.CodeConsEnum;
import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.common.pojo.base.vo.BaseResponse;
import com.cgzd.common.util.CodeCons;
import com.cgzd.pay.convert.PayOrderConvert;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.Type;
import com.cgzd.pay.feign.PayFeign;
import com.cgzd.pay.service.PayServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 支付
 */
@RestController
@RequestMapping(PayFeign.PATH)
public class PayController implements PayFeign {


    @Autowired
    private PayServiceFactory payServiceFactory;

    @Autowired
    private PayOrderConvert payOrderConvert;


    /**
     * APP支付
     * @param aPayOrder 支付定单详情
     * @return 支付结果
     */
    @Override
    public BaseResponse<Object> appPay(PayOrderDTO aPayOrder) {
        try {
            return BaseResponse.success(payServiceFactory.pay(Type.APP, payOrderConvert.convert(aPayOrder)));
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

    /**
     * h5支付
     * @param aPayOrder 支付定单详情
     * @return 支付结果
     */
    @Override
    public BaseResponse<Object> h5Pay(PayOrderDTO aPayOrder) {
        try {
            return BaseResponse.success(payServiceFactory.pay(Type.H5, payOrderConvert.convert(aPayOrder)));
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

    @Override
    public BaseResponse<Object> h5PayMweb(PayOrderDTO aPayOrder) throws CgzdBaseException {
        try {
            return BaseResponse.success(payServiceFactory.pay(Type.H5_MWEB, payOrderConvert.convert(aPayOrder)));
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

    @Override
    public BaseResponse<Object> h5GetOrderId(PayOrderDTO aPayOrder) throws CgzdBaseException {
        try {
            return BaseResponse.success(payServiceFactory.h5GetOrderId(aPayOrder.getCode()));
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

    /**
     * 支付
     * @param aPayOrders 支付定单详情
     * @return 返回结果
     * @throws CgzdBaseException 自定义错误
     */
    @Override
    public BaseResponse<String> pay(List<PayOrderDTO> aPayOrders) throws CgzdBaseException {
        try {
            if (CollectionUtils.isEmpty(aPayOrders)){
                return BaseResponse.error(CodeConsEnum.ERROR_CODE.getCode(),
                        CodeConsEnum.ERROR_CODE.getMsg(),"参数不正确");
            }
            AtomicReference<String> mResult = new AtomicReference<>();
            aPayOrders.forEach(o->mResult.set(payServiceFactory.notify(payOrderConvert.convert(o))));
            return BaseResponse.success(mResult.get());
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

    @Override
    public BaseResponse<String> nativePay(PayOrderDTO aPayOrder) throws CgzdBaseException {
        try {
            Object pay = payServiceFactory.pay(Type.NATIVE, payOrderConvert.convert(aPayOrder));
            if(null != pay){
                if(aPayOrder.getMethod().equals(Method.WEI_PAY.getCode())){
                    return new BaseResponse(10000, "SUCCESS", "成功", ((HashMap) pay).get("codeUrl"));
                }else{
                    return new BaseResponse(10000, "SUCCESS", "成功", pay.toString());
                }
            }
            return BaseResponse.error(CodeCons.ERROR_CODE,"获取二维码失败");
        }catch (CgzdBaseException aE){
            return BaseResponse.error(aE);
        }
    }

}
