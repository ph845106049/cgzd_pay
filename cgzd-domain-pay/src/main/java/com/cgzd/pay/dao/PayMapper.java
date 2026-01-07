package com.cgzd.pay.dao;

import com.cgzd.pay.entity.Pay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PayMapper {

    /**
     * 保存支付信息
     * @param aPay 支付信息
     * @return 保存条数
     */
    Integer save(Pay aPay);

    /**
     * 跟据定单 ID获取支付列表
     * @param aOrderId 订单 ID
     * @return 支付列表
     */
    List<Pay> getPayForOrderId(@Param("id") Long aOrderId);

    /**
     * 跟据订单ID与支付类型获取支付列表
     * @param aOrderId 订单 ID
     * @param aMethodCode 支付类型
     * @return 支付列表
     */
    List<Pay> getPay(@Param("id") Long aOrderId,@Param("code") int aMethodCode);

    /**
     * 根据订单号查询当前订单是否是待支付状态
     * @param outTradeNo  订单号
     * @param code        支付状态
     * @return 查询结果
     */
    int selectSysPayByOrderIdAndStatus(@Param("outTradeNo") String outTradeNo,@Param("code") int code);

    List<Pay> getPayForOrderIdAndType(@Param("id")Long orderId, @Param("code")int code);
}
