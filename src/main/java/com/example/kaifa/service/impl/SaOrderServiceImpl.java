package com.example.kaifa.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.kaifa.entity.saentity.JsonRootBean;
import com.example.kaifa.mapper.orderMapper;
import com.example.kaifa.service.SaOrderService;
import com.example.kaifa.utils.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SaOrderServiceImpl implements SaOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicServiceImpl.class);

    @Autowired
    private orderMapper orderMapper;

    @Override
    public JsonRootBean getSaOrder(Map<String, String> params) {
        JsonRootBean jrb = new JsonRootBean();
        String code = params.get("code");//单据编号
        String token = orderMapper.getTokenByAppKey(params.get("AppKey"));
        try {
            String json = "{param:{voucherCode:\"" + code + "\"}}"; //{param:{}} 查所有
            String result = HttpClient.HttpPost("/tplus/api/v2/SaleDeliveryOpenApi/GetVoucherDTO",
                    json,
                    params.get("AppKey"),
                    params.get("AppSecret"),
                    token);
            JSONObject job = JSONObject.parseObject(result);
            jrb = job.toJavaObject(JsonRootBean.class);
            if(jrb  == null || jrb.getCode() == null || jrb.getCode().contains("999")){//说明请求畅捷通失败，就再来一次
                String result2 = HttpClient.HttpPost("/tplus/api/v2/SaleDeliveryOpenApi/GetVoucherDTO",
                        json,
                        params.get("AppKey"),
                        params.get("AppSecret"),
                        token);
                JSONObject job2 = JSONObject.parseObject(result2);
                jrb = job2.toJavaObject(JsonRootBean.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jrb;
    }

    @Override
    public String unAuditSaOrder(Map<String, String> params){
        String voucherCode = params.get("params");
        String appKey = params.get("appKey");
        String AppSecret = params.get("AppSecret");
        String access_token = orderMapper.getTokenByAppKey(appKey);

        String result = "";
        try{
            String auditjson = "{\n" +
                    "  \"param\": {\n" +
                    "    \"voucherCode\": \""+voucherCode+"\"\n" +
                    "  }\n" +
                    "}";
            String auditResult = HttpClient.HttpPost("/tplus/api/v2/SaleDeliveryOpenApi/UnAudit",
                    auditjson,
                    appKey,
                    AppSecret,
                    access_token);
            JSONObject unauditjob = JSONObject.parseObject(auditResult);
            if(unauditjob.getString("code").contains("999")){//如果弃审失败，就再弃审一次 试试
                auditResult = HttpClient.HttpPost("/tplus/api/v2/SaleDeliveryOpenApi/UnAudit",
                        auditjson,
                        appKey,
                        AppSecret,
                        access_token);
            }
            LOGGER.info("-------------- 单号： " + voucherCode + "的弃审结果是：" + auditResult);
            result = auditResult;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

}
