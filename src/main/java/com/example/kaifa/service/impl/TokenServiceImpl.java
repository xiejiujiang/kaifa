package com.example.kaifa.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.kaifa.mapper.orderMapper;
import com.example.kaifa.service.TokenService;
import com.example.kaifa.utils.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Autowired
    private orderMapper orderMapper;


    @Override
    public String getsaleprice(String customer, String inventory) {
        String price = "";
        List<Map<String,String>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
        if(pricelist != null && pricelist.size() != 0){//name,isInUse,priorityLevel
            for(Map<String,String> map : pricelist){
                String name = map.get("name");
                //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                price = getSaNamePrice(customer,inventory,name);
                if(price == null || "".equals(price)){
                    continue;
                }else{
                    return price;
                }
            }
            return price;
        }else{
            return "999999";
        }
    }

    @Override
    public String getSaNamePrice(String customer, String inventory, String name) {
        //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
        String price = "";
        switch (name){
            case "部门最新售价":

                break;
            case "客户最新售价":

                break;
            case "客户协议价":

                break;
        }
        return price;
    }
}
