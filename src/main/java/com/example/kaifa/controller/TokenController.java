package com.example.kaifa.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.kaifa.entity.PurSubchaseOrder.PuorderJsonRootBean;
import com.example.kaifa.entity.SAsubscribe.SACsubJsonRootBean;
import com.example.kaifa.service.PuOrderService;
import com.example.kaifa.service.TokenService;
import com.example.kaifa.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/token")
public class TokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PuOrderService puOrderService;

    @RequestMapping(value="/test", method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三姗");
        mav.setViewName("ptdesc");
        return mav;
    }


    @RequestMapping(value="/recode", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String recode(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("------------------- 正式OAuth回调地址 -------------------");
        String code = request.getParameter("code");
        //第一次授权后，会有这个code,立刻调用 一次 授权码换token接口 ，拿到完整的 token 相关信息，并写入数据库。
        //3月17日思考： 暂时不用接口来访问，直接在线访问后 拿到第一次的数据，并 复制 填入数据库表中接口（后续定时任务来更新）
        return code;
    }


    //T+ 的 消息订阅的接口。
    @RequestMapping(value="/ticket", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String reticket(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("------------------- 正式消息接收地址，包含 ticket，消息订阅，授权 -------------------");
        try{
            InputStreamReader reader = new InputStreamReader(request.getInputStream(),"utf-8");
            BufferedReader buffer = new BufferedReader(reader);
            String params = buffer.readLine();
            JSONObject jsonObject = JSONObject.parseObject(params);
            String encryptMsg = jsonObject.getString("encryptMsg");
            String destr = AESUtils.aesDecrypt(encryptMsg,"123456789012345x");
            // {"id":"AC1C04B100013301500B4A9B012DB2EC","appKey":"A9A9WH1i","appId":"58","msgType":"SaleDelivery_Audit","time":"1649994072443","bizContent":{"externalCode":"","voucherID":"23","voucherDate":"2022/4/15 0:00:00","voucherCode":"SA-2022-04-0011"},"orgId":"90015999132","requestId":"86231b63-f0c2-4de1-86e9-70557ba9cd62"}
            JSONObject job = JSONObject.parseObject(destr);
            String msgType = job.getString("msgType");
            //根据 不同的 msgType 处理不同 的业务。
            /*if("SaleDelivery_Audit".equals(msgType)){
                SACsubJsonRootBean jrb =  job.toJavaObject(SACsubJsonRootBean.class);//销货单审核的订阅信息DTO
            }*/

            if("PurchaseOrder_Audit".equals(msgType)){
                PuorderJsonRootBean puorder = job.toJavaObject(PuorderJsonRootBean.class);//采购订单
                Map<String,String> pras = new HashMap<String,String>();
                pras.put("code",puorder.getBizContent().getVoucherCode());
                Map<String,Object> resultMap = puOrderService.getPuOrderByDB(pras);
                //这个 resultMap 就是 从数据库 中 查询 返回的 结果。
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "{ \"result\":\"success\" }";
    }


    // 提供给 第三方的接口，用于 调取 获取 当前 客户，当前商品的 T+的 销售策略 带出的 价格！
    @RequestMapping(value="/getsaleprice", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String getsaleprice(HttpServletRequest request, HttpServletResponse response){
        String customer = request.getParameter("customer");
        String inventory = request.getParameter("inventory");
        String sign = request.getParameter("sign");
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());//当日
        if(sign != null && !sign.equals("")
                && customer != null && !customer.equals("")
                && inventory != null && !inventory.equals("")
                && sign.equals(Md5.md5(customer+inventory+today))){
            String price = tokenService.getsaleprice(customer,inventory);
            return price;
        }else{
            String price = "999999";
            return price;
        }
    }
}