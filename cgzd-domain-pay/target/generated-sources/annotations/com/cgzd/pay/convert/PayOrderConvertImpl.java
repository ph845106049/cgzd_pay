package com.cgzd.pay.convert;

import com.cgzd.pay.dto.PayIos;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.entity.Pay;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-05T16:13:24+0800",
    comments = "version: 1.4.1.Final, compiler: javac, environment: Java 1.8.0_311 (Oracle Corporation)"
)
@Component
public class PayOrderConvertImpl implements PayOrderConvert {

    @Override
    public PayOrder convert(PayOrderDTO aDTO) {
        if ( aDTO == null ) {
            return null;
        }

        PayOrder payOrder = new PayOrder();

        if ( aDTO.getOrderId() != null ) {
            payOrder.setOrderId( Long.parseLong( aDTO.getOrderId() ) );
        }
        payOrder.setDescription( aDTO.getDescription() );
        payOrder.setAmount( aDTO.getAmount() );
        payOrder.setCode( aDTO.getCode() );
        if ( aDTO.getMethod() != null ) {
            payOrder.setMethod( customConveter( aDTO.getMethod().intValue() ) );
        }
        payOrder.setGoodId( aDTO.getGoodId() );
        Map<String, Object> map = aDTO.getProperty();
        if ( map != null ) {
            payOrder.setProperty( new HashMap<String, Object>( map ) );
        }
        payOrder.setAccountBalance( aDTO.getAccountBalance() );
        payOrder.setAccountPoint( aDTO.getAccountPoint() );
        payOrder.setSourceType( aDTO.getSourceType() );

        return payOrder;
    }

    @Override
    public PayOrder ios(PayIos PayIos) {
        if ( PayIos == null ) {
            return null;
        }

        PayOrder payOrder = new PayOrder();

        payOrder.setOrderId( PayIos.getOrderId() );
        payOrder.setDescription( PayIos.getDescription() );
        payOrder.setAmount( PayIos.getAmount() );
        payOrder.setCode( PayIos.getCode() );
        payOrder.setMethod( PayIos.getMethod() );
        payOrder.setGoodId( PayIos.getGoodId() );
        Map<String, Object> map = PayIos.getProperty();
        if ( map != null ) {
            payOrder.setProperty( new HashMap<String, Object>( map ) );
        }
        payOrder.setSourceType( PayIos.getSourceType() );

        return payOrder;
    }

    @Override
    public Pay convert(PayOrder aPayOrder) {
        if ( aPayOrder == null ) {
            return null;
        }

        Pay pay = new Pay();

        pay.setOrderDescription( aPayOrder.getDescription() );
        pay.setPayType( aPayOrder.getType() );
        pay.setPayMethod( aPayOrder.getMethod() );
        pay.setOrderId( aPayOrder.getOrderId() );
        pay.setAmount( aPayOrder.getAmount() );
        Map<String, Object> map = aPayOrder.getProperty();
        if ( map != null ) {
            pay.setProperty( new HashMap<String, Object>( map ) );
        }
        pay.setSourceType( aPayOrder.getSourceType() );

        return pay;
    }
}
