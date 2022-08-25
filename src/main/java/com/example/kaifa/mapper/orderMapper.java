package com.example.kaifa.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface orderMapper {

    List<Map<String,String>> getSalePriceList();

    //获取部门最新售价
    String getDepartmentSalePrice(Map<String,String> map);

    //获取客户最新售价
    String getCustmerSalePrice(Map<String,String> map);

    //获取客户协议价
    String getCustmerxieyiPrice(Map<String,String> map);

    //获取部门批发价
    String getDepartmentPIFAprice(Map<String,String> map);

    //获取 存货批发价
    String getInventoryPIFAprice(Map<String,String> map);

    //获取 客户最新进价加价
    String getCustomerLastprice(Map<String,String> map);

    //获取 存货最新进价加价
    String getInventoryLastprice(Map<String,String> map);

    List<Map<String,String>> getallpartnerinventory();

    List<Map<String,String>> getpartnerinventory(String customer);
}
