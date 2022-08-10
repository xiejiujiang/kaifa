package com.example.kaifa.service.impl;

import com.example.kaifa.mapper.orderMapper;
import com.example.kaifa.service.TokenService;
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
    public String getsaleprice(String customer, String inventory,String department) {
        String price = "999999";
        List<Map<String,String>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
        if(pricelist != null && pricelist.size() != 0){//name,isInUse,priorityLevel
            for(Map<String,String> map : pricelist){
                String name = map.get("name");
                //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                price = getSaNamePrice(customer,inventory,department,name);
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
    public String getSaNamePrice(String customer, String inventory,String department,String name) {
        //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
        String price = "";
        switch (name){
            case "部门最新售价":
                Map<String,String> params = new HashMap<String,String>();
                params.put("iddepartment",department);//部门id
                params.put("idinventory",inventory);
                price = orderMapper.getDepartmentSalePrice(params);
                break;
            case "客户最新售价":
                Map<String,String> param = new HashMap<String,String>();
                param.put("idcustomer",customer);
                param.put("idinventory",inventory);
                price = orderMapper.getCustmerSalePrice(param);
                break;
            case "客户协议价":
                Map<String,String> paramxy = new HashMap<String,String>();
                paramxy.put("idcustomer",customer);
                paramxy.put("idinventory",inventory);
                price = orderMapper.getCustmerxieyiPrice(paramxy);
                break;
            case "部门批发价":
                Map<String,String> paramPIFA = new HashMap<String,String>();
                paramPIFA.put("iddepartment",department);
                paramPIFA.put("idcustomer",customer);
                paramPIFA.put("idinventory",inventory);
                price = orderMapper.getDepartmentPIFAprice(paramPIFA);
                break;
            case "客户折扣":

                break;
            case "存货最新售价":

                break;
            case "存货批发价":
                Map<String,String> paramInventoryPrice = new HashMap<String,String>();
                //paramInventoryPrice.put("iddepartment",department);
                paramInventoryPrice.put("idcustomer",customer);
                paramInventoryPrice.put("idinventory",inventory);
                price = orderMapper.getInventoryPIFAprice(paramInventoryPrice);
                break;
            case "存货数量档位价格":

                break;
            case "客户最新进价加价":
                Map<String,String> paramCustomerLastPrice = new HashMap<String,String>();
                //paramCustomerLastPrice.put("iddepartment",department);
                paramCustomerLastPrice.put("idcustomer",customer);
                paramCustomerLastPrice.put("idinventory",inventory);
                price = orderMapper.getCustomerLastprice(paramCustomerLastPrice);
                break;
            case "存货最新进价加价":
                Map<String,String> paramInventoryLastPrice = new HashMap<String,String>();
                //paramInventoryLastPrice.put("iddepartment",department);
                //paramInventoryLastPrice.put("idcustomer",customer);
                paramInventoryLastPrice.put("idinventory",inventory);
                price = orderMapper.getInventoryLastprice(paramInventoryLastPrice);
                break;
        }
        return price;
    }
}