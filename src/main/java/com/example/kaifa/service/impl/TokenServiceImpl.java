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
    public String refreshToken(){
        try {
            List<Map<String,String>> orgList = orderMapper.getDBAllOrgList();
            if(orgList != null && orgList.size() != 0){
                for(Map<String,String> org : orgList){
                    Map<String,String> parma = new HashMap<String,String>();
                    parma.put("grantType","refresh_token");
                    parma.put("appKey",org.get("AppKey"));
                    parma.put("refreshToken",org.get("refresh_token"));
                    String result = HttpClient.doGeturlparams("https://openapi.chanjet.com/auth/refreshToken", parma);
                    //将返回的 result 解析出来，写回数据库！,并一定更新 最后的 更新时间 ,其实 只有 refresh_token,token,和 更新时间会变。
                    JSONObject jso = JSONObject.parseObject(result);
                    if("200".equals(jso.get("code").toString())){//调用成功，更新数据库！
                        JSONObject detail = JSONObject.parseObject(jso.get("result").toString());
                        String access_token = detail.get("access_token").toString();
                        String refresh_token = detail.get("refresh_token").toString();
                        String org_id = detail.get("org_id").toString();
                        Map<String,String> updateMap = new HashMap<String,String>();
                        updateMap.put("org_id",org_id);
                        updateMap.put("refresh_token",refresh_token);
                        updateMap.put("access_token",access_token);
                        orderMapper.updateOrgToken(updateMap);
                    }else{
                        LOGGER.error("----------------更新失败，检擦！！！---------------------- " + org.get("org_id").toString());
                    }
                }
            }
        }catch (Exception e){
            //e.printStackTrace();
            //如果出异常，就再来一次试试
            try{
                List<Map<String,String>> orgList = orderMapper.getDBAllOrgList();
                if(orgList != null && orgList.size() != 0){
                    for(Map<String,String> org : orgList){
                        Map<String,String> parma = new HashMap<String,String>();
                        parma.put("grantType","refresh_token");
                        parma.put("appKey",org.get("AppKey"));
                        parma.put("refreshToken",org.get("refresh_token"));
                        String result = HttpClient.doGeturlparams("https://openapi.chanjet.com/auth/refreshToken", parma);
                        //将返回的 result 解析出来，写回数据库！,并一定更新 最后的 更新时间 ,其实 只有 refresh_token,token,和 更新时间会变。
                        JSONObject jso = JSONObject.parseObject(result);
                        if("200".equals(jso.get("code").toString())){//调用成功，更新数据库！
                            JSONObject detail = JSONObject.parseObject(jso.get("result").toString());
                            String access_token = detail.get("access_token").toString();
                            String refresh_token = detail.get("refresh_token").toString();
                            String org_id = detail.get("org_id").toString();
                            Map<String,String> updateMap = new HashMap<String,String>();
                            updateMap.put("org_id",org_id);
                            updateMap.put("refresh_token",refresh_token);
                            updateMap.put("access_token",access_token);
                            orderMapper.updateOrgToken(updateMap);
                        }else{
                            LOGGER.error("----------------更新失败，检擦！！！---------------------- " + org.get("org_id").toString());
                        }
                    }
                }
            }catch (Exception ex){
                LOGGER.error("---------------- 再来一次都TM失败了，检查一下看看！ ---------------------- " );
            }
        }
        return "success";
    }

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
