package com.example.kaifa.service;

import com.example.kaifa.entity.saentity.JsonRootBean;

import java.util.Map;

public interface SaOrderService {

    //查询销货单详情接口
    public JsonRootBean getSaOrder(Map<String,String> params);

    //弃审 销货单： code
    public String unAuditSaOrder(Map<String,String> params);


}
