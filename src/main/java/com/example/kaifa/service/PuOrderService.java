package com.example.kaifa.service;


import java.util.Map;

public interface PuOrderService {

    //查询采购订单的详情
    //PuOrderJsonRootBean getPuOrderByAPI(Map<String,String> params);

    //从数据库中 获取 此采购订单的必要信息
    Map<String,Object> getPuOrderByDB(Map<String,String> params);

}
