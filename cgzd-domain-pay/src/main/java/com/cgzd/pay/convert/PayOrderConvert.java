package com.cgzd.pay.convert;

import com.cgzd.common.util.EnumUtils;
import com.cgzd.pay.dto.PayIos;
import com.cgzd.pay.dto.PayOrder;
import com.cgzd.pay.dto.PayOrderDTO;
import com.cgzd.pay.entity.Pay;
import com.cgzd.pay.enums.Method;
import com.cgzd.pay.enums.SourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayOrderConvert {

    PayOrder convert(PayOrderDTO aDTO);
    PayOrder ios(PayIos PayIos);

    @Mapping(source = "description",target = "orderDescription")
    @Mapping(source = "type",target = "payType")
    @Mapping(source = "method",target = "payMethod")
    Pay convert(PayOrder aPayOrder);

    default Method customConveter(int aCode){
        return EnumUtils.codeOf(Method.class,aCode);
    }

    default SourceType sourceTypeConveter(int aCode){
        return EnumUtils.codeOf(SourceType.class,aCode);
    }

}
