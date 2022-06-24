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
        List<Map<String,String>> pricelist = orderMapper.getSalePriceList();
        if(pricelist != null && pricelist.size() != 0){



            return "";
        }else{
            return "999999";
        }
    }
}
