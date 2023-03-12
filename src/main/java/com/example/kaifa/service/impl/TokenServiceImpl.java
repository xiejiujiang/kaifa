package com.example.kaifa.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.kaifa.mapper.orderMapper;
import com.example.kaifa.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<Map<String,Object>> inventoryUnit = orderMapper.getInventoryUnitMapByCode(inventory);
        if(inventoryUnit != null && inventoryUnit.size() == 1){
            //单计量的情况下
            String dcode = inventoryUnit.get(0).get("code").toString();
            //用来存 返回 字符串的
            List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
            Map<String,String> resultstr = new HashMap<String,String>();
            resultstr.put("unitID",inventoryUnit.get(0).get("id").toString());
            resultstr.put("Code",dcode);
            resultstr.put("Name",inventoryUnit.get(0).get("name").toString());
            String price1 = "";
            List<Map<String,Object>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
            for(int i = 0;i < pricelist.size(); i++){
                Map<String,Object> map = pricelist.get(i);
                String name = map.get("name").toString();
                String discount = map.get("discount").toString();//售价带出策略 处 设置的 加价率
                //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                System.out.println("第"+i+"个: --------------- name = " + name);
                price1 = getSaNameUnitPrice(customer,inventory,department,name,discount,dcode);
                System.out.println("第"+i+"个: --------------- price = " + price1);
                if(price1 == null || "".equals(price1) || "0".equals(price1) || Float.valueOf(price1) == 0f){
                    continue;
                }else{
                    break;
                }
            }
            if(price1 == null || "".equals(price1)){
                return "";
            }
            resultstr.put("price",price1);
            ll.add(resultstr);
            JSONArray ja = new JSONArray();
            ja.addAll(ll);
            return ja.toJSONString();
        }else{
            List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
            List<Map<String,Object>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
            Float mainUnitPirce = 999999f;//预置的 主单位下 数量1 对应的价格
            for(Map<String,Object> mkk : inventoryUnit){
                Map<String,String> resultstr = new HashMap<String,String>();
                Float changeRate = Float.valueOf(mkk.get("changeRate").toString());//此单位对应的换算率
                String dcode = mkk.get("dcode").toString();
                resultstr.put("unitID",mkk.get("did").toString());
                resultstr.put("Code",dcode);
                resultstr.put("Name",mkk.get("dname").toString());
                String price2 = "";
                for(int i = 0;i < pricelist.size(); i++){
                    Map<String,Object> map = pricelist.get(i);
                    String name = map.get("name").toString();
                    String discount = map.get("discount").toString();//售价带出策略 处 设置的 加价率
                    //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                    System.out.println("第"+i+"个: --------------- name = " + name);
                    price2 = getSaNameUnitPrice(customer,inventory,department,name,discount,dcode);
                    System.out.println("第"+i+"个: --------------- price = " + price2);
                    if(price2 == null || "".equals(price2) || "0".equals(price2) || Float.valueOf(price2) == 0f){
                        continue;
                    }else{
                        mainUnitPirce = Float.valueOf(price2)/changeRate;
                        break;
                    }
                }
                resultstr.put("changeRate",""+changeRate);
                resultstr.put("price",price2);
                ll.add(resultstr);
            }
            for(int i=0;i<ll.size();i++){
                // 这个 for 是为了解决 其中某个单位没得价格的问题
                if(ll.get(i).get("price") == null || "".equals(ll.get(i).get("price").toString()) || Float.valueOf(ll.get(i).get("price").toString()) == 0f) {
                    //说明 这个 商品的这个计量单位 找不到 对应的价格！
                    if(mainUnitPirce != null && mainUnitPirce != 999999f){
                        Float aaaa = mainUnitPirce*Float.valueOf(ll.get(i).get("changeRate").toString());
                        ll.get(i).put("price","" + aaaa);
                    }
                }
                ll.get(i).remove("changeRate");
            }
            JSONArray ja = new JSONArray();
            ja.addAll(ll);
            return ja.toJSONString();
        }
    }


    //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
    // [{unitID:1,Code:222,Name:'桶',price:'30'},{unitID:1,Code:222,Name:'件',price:'30'}]
    // 09-21 做了修改，现在 是 每一个 商品 的 某一个 单位，对应的 价格策略 带出的 价格 是多少
    @Override
    public String getSaNameUnitPrice(String customer, String inventory,String department,String name,String discount,String dcodee) {
        //List<Map<String,Object>> inventoryUnit = orderMapper.getInventoryUnitMapByCode(inventory);
        String price = "";
        switch (name){
            case "部门最新售价":
                // 如果 此商品 此单位  在 此价格策略中
                Map<String,Object> map1 = new HashMap<String,Object>();
                map1.put("idinventory",inventory);
                map1.put("iddepartment",department);
                map1.put("idcustomer",customer);
                map1.put("code",dcodee);
                String unitprice1 = orderMapper.getDepartmentPriceByInventoryAndUnitCode(map1);
                price = unitprice1;
                break;
            case "客户最新售价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map2 = new HashMap<String,Object>();
                map2.put("idinventory",inventory);
                map2.put("iddepartment",department);
                map2.put("idcustomer",customer);
                map2.put("code",dcodee);
                price = orderMapper.getgetCustmerSalePriceByInventoryAndUnitCode(map2);
                break;
            case "客户协议价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map3 = new HashMap<String,Object>();
                map3.put("idinventory",inventory);
                map3.put("iddepartment",department);
                map3.put("idcustomer",customer);
                map3.put("code",dcodee);
                price = orderMapper.getCustmerxieyiPriceByInventoryAndUnitCode(map3);
                break;
            case "部门批发价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map4 = new HashMap<String,Object>();
                map4.put("idinventory",inventory);
                map4.put("iddepartment",department);
                map4.put("idcustomer",customer);
                map4.put("code",dcodee);
                price = orderMapper.getDepartmentPIFApriceByInventoryAndUnitCode(map4);
                break;
            case "客户折扣":

                break;
            case "存货最新售价":

                break;
            case "存货批发价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map5 = new HashMap<String,Object>();
                map5.put("idinventory",inventory);
                map5.put("iddepartment",department);
                map5.put("idcustomer",customer);
                map5.put("code",dcodee);
                price = orderMapper.getInventoryPIFApriceByInventoryAndUnitCode(map5);
                break;
            case "存货数量档位价格":

                break;
            case "客户最新进价加价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map6 = new HashMap<String,Object>();
                map6.put("idinventory",inventory);
                map6.put("iddepartment",department);
                map6.put("idcustomer",customer);
                map6.put("code",dcodee);
                String unitprice = orderMapper.getCustomerLastpriceByInventoryAndUnitCode(map6);
                if(unitprice != null && !unitprice.equals("")){
                    price = (Float.valueOf(unitprice) * (1+Float.valueOf(discount)))+"" ;
                }else{
                    price = "" ;
                }
                break;
            case "存货最新进价加价":
                // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                Map<String,Object> map7 = new HashMap<String,Object>();
                map7.put("idinventory",inventory);
                map7.put("iddepartment",department);
                map7.put("idcustomer",customer);
                map7.put("code",dcodee);
                String unitprice7 = orderMapper.getInventoryLastpriceByInventoryAndUnitCode(map7);
                if(unitprice7 != null && !"".equals(unitprice7)){
                    price = "" +(1+Float.valueOf(discount))*Float.valueOf(unitprice7);
                }else{
                    price = "";
                }
                break;
        }
        return price;
    }


    @Override
    public List<Map<String,String>> getpartnerinventory(String customer) {
        //往来单位编码，往来单位名称，存货编码，存货名称，规格型号，主计量单位，往来单位存货编码，主计量单位ID
        if("all".equals(customer)){
            List<Map<String,String>> datalist = orderMapper.getallpartnerinventory();
            return datalist;
        }else{
            List<Map<String,String>> datalist = orderMapper.getpartnerinventory(customer);
            return datalist;
        }
    }
}