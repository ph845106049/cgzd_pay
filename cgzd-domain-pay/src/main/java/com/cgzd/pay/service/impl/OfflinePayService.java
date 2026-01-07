package com.cgzd.pay.service.impl;

import com.cgzd.common.exception.CgzdBaseException;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.SourceType;
import com.cgzd.pay.enums.Status;
import com.cgzd.pay.enums.Type;
import com.cgzd.pay.service.PayType;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 线下支付实现类
 */
@PayType(Method.OFFLINE)
public class OfflinePayService extends AbstractPayService {

    @Override
    public String pay(PayOrder aPayOrder) throws CgzdBaseException {
        return "SUCCESS";
    }

    @Override
    public boolean save(Pay aPay) {
        aPay.setStatus(Status.SUCCESS);
        aPay.setPayType(Type.SYS);
        aPay.setPayTime(LocalDateTime.now());
        if (!CollectionUtils.isEmpty(aPay.getProperty())){
            if (aPay.getProperty().containsKey("proof")){
                aPay.setProof(aPay.getProperty().get("proof").toString());
            }
            if (aPay.getProperty().containsKey("prepayId")){
                aPay.setPrepayId(aPay.getProperty().get("prepayId").toString());
            }
        }
        return super.save(aPay);
    }

    @Override
    public String callback(HttpServletRequest request) {
        return null;
    }

    @Override
    public String sysCallback(PayOrder aPayOrder) {
        super.basicCheck(aPayOrder);
        super.savePay(aPayOrder);
        return "SUCCESS";
    }
}
