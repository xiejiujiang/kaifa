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
        String price = "";
        List<Map<String,Object>> inventoryUnit = orderMapper.getInventoryUnitMapByCode(inventory);
        if(inventoryUnit != null && inventoryUnit.size() == 1){
            //单计量的情况下
            String dcode = inventoryUnit.get(0).get("dcode").toString();
            List<Map<String,Object>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
            if(pricelist != null && pricelist.size() != 0){//name,isInUse,priorityLevel
                //for(Map<String,String> map : pricelist){
                for(int i = 0;i < pricelist.size(); i++){
                    Map<String,Object> map = pricelist.get(i);
                    String name = map.get("name").toString();
                    String discount = map.get("discount").toString();//售价带出策略 处 设置的 加价率
                    //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                    System.out.println("第"+i+"个: --------------- name = " + name);
                    price = getSaNameUnitPrice(customer,inventory,department,name,discount,dcode);
                    System.out.println("第"+i+"个: --------------- price = " + price);
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
        }else{
            List<Map<String,Object>> pricelist = orderMapper.getSalePriceList();//获取 账套里面 的 销售带出策略的 明细, 已经按照 级别 排序了。
            for(Map<String,Object> mkk : inventoryUnit){
                String dcode = mkk.get("dcode").toString();
                if(pricelist != null && pricelist.size() != 0){//name,isInUse,priorityLevel
                    //for(Map<String,String> map : pricelist){
                    for(int i = 0;i < pricelist.size(); i++){
                        Map<String,Object> map = pricelist.get(i);
                        String name = map.get("name").toString();
                        String discount = map.get("discount").toString();//售价带出策略 处 设置的 加价率
                        //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
                        System.out.println("第"+i+"个: --------------- name = " + name);
                        price = getSaNameUnitPrice(customer,inventory,department,name,discount,dcode);
                        System.out.println("第"+i+"个: --------------- price = " + price);
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
            return "";
        }
    }


    //根据 每一个 customer inventory 和 售价带出策略的name 去 获取 对应的 价格,第一个没有就去拿 第二个，直到 拿到 有内容的 为止。
    // [{unitID:1,Code:222,Name:'桶',price:'30'},{unitID:1,Code:222,Name:'件',price:'30'}]
    // 09-21 做了修改，现在 是 每一个 商品 的 某一个 单位，对应的 价格策略 带出的 价格 是多少
    @Override
    public String getSaNameUnitPrice(String customer, String inventory,String department,String name,String discount,String dcodee) {
        List<Map<String,Object>> inventoryUnit = orderMapper.getInventoryUnitMapByCode(inventory);

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
                Map<String,String> param = new HashMap<String,String>();
                param.put("idcustomer",customer);
                param.put("idinventory",inventory);
                Map<String,Object> custmerpriceMap = orderMapper.getCustmerSalePrice(param);
                if(custmerpriceMap == null || custmerpriceMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    mm.put("price",custmerpriceMap.get("price").toString());
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(custmerpriceMap);
                    String mainUnitPirce = ""+(Float.valueOf(custmerpriceMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        String unitprice = orderMapper.getgetCustmerSalePriceByInventoryAndUnitCode(map1);
                        if(unitprice != null && !"".equals(unitprice)){
                            resultstr.put("price",unitprice);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }
                break;
            case "客户协议价":
                Map<String,String> paramxy = new HashMap<String,String>();
                paramxy.put("idcustomer",customer);
                paramxy.put("idinventory",inventory);
                Map<String,Object> custmerxieyipriceMap = orderMapper.getCustmerxieyiPrice(paramxy);
                if(custmerxieyipriceMap == null || custmerxieyipriceMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    mm.put("price",custmerxieyipriceMap.get("price").toString());
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(custmerxieyipriceMap);
                    String mainUnitPirce = ""+(Float.valueOf(custmerxieyipriceMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        String unitprice = orderMapper.getCustmerxieyiPriceByInventoryAndUnitCode(map1);
                        if(unitprice != null && !"".equals(unitprice)){
                            resultstr.put("price",unitprice);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }
                break;
            case "部门批发价":
                Map<String,String> paramPIFA = new HashMap<String,String>();
                paramPIFA.put("iddepartment",department);
                paramPIFA.put("idcustomer",customer);
                paramPIFA.put("idinventory",inventory);
                Map<String,Object> deparetpriceMap = orderMapper.getDepartmentPIFAprice(paramPIFA);
                if(deparetpriceMap == null || deparetpriceMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    mm.put("price",deparetpriceMap.get("price").toString());
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(deparetpriceMap);
                    String mainUnitPirce = ""+(Float.valueOf(deparetpriceMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        String unitprice = orderMapper.getDepartmentPIFApriceByInventoryAndUnitCode(map1);
                        if(unitprice != null && !"".equals(unitprice)){
                            resultstr.put("price",unitprice);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }
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
                Map<String,Object> inventorypfpriceMap = orderMapper.getInventoryPIFAprice(paramInventoryPrice);
                if(inventorypfpriceMap == null || inventorypfpriceMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    mm.put("price",inventorypfpriceMap.get("price").toString());
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(inventorypfpriceMap);
                    String mainUnitPirce = ""+(Float.valueOf(inventorypfpriceMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        String unitprice = orderMapper.getInventoryPIFApriceByInventoryAndUnitCode(map1);
                        if(unitprice != null && !"".equals(unitprice)){
                            resultstr.put("price",unitprice);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }
                break;
            case "存货数量档位价格":

                break;
            case "客户最新进价加价":
                Map<String,String> paramCustomerLastPrice = new HashMap<String,String>();
                //paramCustomerLastPrice.put("iddepartment",department);
                paramCustomerLastPrice.put("idcustomer",customer);
                paramCustomerLastPrice.put("idinventory",inventory);
                Map<String,Object> customerpricelastMap = orderMapper.getCustomerLastprice(paramCustomerLastPrice);
                if(customerpricelastMap == null || customerpricelastMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    mm.put("price",customerpricelastMap.get("price").toString());
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(customerpricelastMap);
                    String mainUnitPirce = ""+(Float.valueOf(customerpricelastMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        // increasePriceRate 是价格本里面的 加价率 ， discount 是售价带出策略里面的加价率
                        String increasePriceRate = customerpricelastMap.get("increasePriceRate").toString();
                        String unitprice = orderMapper.getCustomerLastpriceByInventoryAndUnitCode(map1);
                        String unitpricee = "";
                        if(increasePriceRate != null && !"".equals(increasePriceRate) && Float.valueOf(increasePriceRate) != 0f ){
                            unitpricee = (Float.valueOf(unitprice) * (1+Float.valueOf(increasePriceRate)))+"" ;
                        }else{
                            unitpricee = (Float.valueOf(unitprice) * (1+Float.valueOf(discount)))+"" ;
                        }
                        if(unitpricee != null && !"".equals(unitpricee)){
                            resultstr.put("price",unitpricee);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }
                break;
            case "存货最新进价加价":
                Map<String,String> paramInventoryLastPrice = new HashMap<String,String>();
                //paramInventoryLastPrice.put("iddepartment",department);
                //paramInventoryLastPrice.put("idcustomer",customer);
                paramInventoryLastPrice.put("idinventory",inventory);
                Map<String,Object> inventorypriceplusMap = orderMapper.getInventoryLastprice(paramInventoryLastPrice);
                if(inventorypriceplusMap == null || inventorypriceplusMap.get("price") == null){
                    return "";
                }
                if(inventoryUnit != null && inventoryUnit.size() == 1){
                    Map<String,String> mm = new HashMap<String,String>();
                    mm.put("unitID",inventoryUnit.get(0).get("id").toString());
                    mm.put("Code",inventoryUnit.get(0).get("code").toString());
                    mm.put("Name",inventoryUnit.get(0).get("name").toString());
                    String ratePirce = "" + (1+Float.valueOf(discount))*Float.valueOf(inventorypriceplusMap.get("price").toString());
                    mm.put("price",ratePirce);
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    ll.add(mm);
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
                }else{
                    // 那么 根据 priceMap 查询到的这个 存货：单位：价格   计算出 这个存货对应的主计量单位 对应的 价格！
                    String rate = orderMapper.getRateByInventoryAndUnit(inventorypriceplusMap);
                    String mainUnitPirce = ""+(Float.valueOf(inventorypriceplusMap.get("price").toString()) / Float.valueOf(rate)) ; //主计量单位对应的价格
                    List<Map<String,String>> ll = new ArrayList<Map<String,String>>();
                    for(Map<String,Object> unitMap : inventoryUnit){
                        String changeRate = unitMap.get("changeRate").toString();// 单位 转换率
                        Map<String,String> resultstr = new HashMap<String,String>();//用来存 返回 字符串的 MAP
                        resultstr.put("unitID",unitMap.get("did").toString());
                        resultstr.put("Code",unitMap.get("dcode").toString());
                        resultstr.put("Name",unitMap.get("dname").toString());

                        // 如果 此商品 此单位  在 此价格策略中 找到了 就用，没找到，才用下面这个 乘积
                        Map<String,Object> map1 = new HashMap<String,Object>();
                        map1.put("idinventory",inventory);
                        map1.put("iddepartment",department);
                        map1.put("idcustomer",customer);
                        map1.put("code",unitMap.get("dcode").toString());
                        String unitprice = orderMapper.getInventoryLastpriceByInventoryAndUnitCode(map1);
                        if(unitprice != null && !"".equals(unitprice)){
                            String sss = "" +(1+Float.valueOf(discount))*Float.valueOf(unitprice);
                            resultstr.put("price",sss);
                        }else{
                            resultstr.put("price",Float.valueOf(changeRate)*Float.valueOf(mainUnitPirce)+"");
                        }
                        ll.add(resultstr);
                    }
                    JSONArray ja = new JSONArray();
                    ja.addAll(ll);
                    price = ja.toJSONString();
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