package com.example.kaifa.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.kaifa.mapper.orderMapper;
import com.example.kaifa.service.PuOrderService;
import com.example.kaifa.utils.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PuOrderServiceImpl implements PuOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PuOrderServiceImpl.class);

    @Autowired
    private orderMapper orderMapper;


    @Override
    public Map<String,Object> getPuOrderByDB(Map<String, String> params) {


        return null;
    }
}
