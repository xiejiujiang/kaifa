package com.example.kaifa.controller;

import com.alibaba.fastjson.JSONObject;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/token")
public class TokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value="/test", method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView test(HttpServletRequest request, HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三姗");
        mav.setViewName("ptdesc");
        return mav;
    }


    // 提供给 第三方的接口，用于 调取 获取 当前 客户，当前商品的 T+的 销售策略 带出的 价格！
    @RequestMapping(value="/getsaleprice", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String getsaleprice(HttpServletRequest request, HttpServletResponse response){
        String IP = RequestIPUtils.getIpAddr(request);//47.108.88.144
        System.out.println("获取价格请求 IP ======= " + IP);
        //if("47.108.88.144".equals(IP)){
            String customer = request.getParameter("customer");
            String inventory = request.getParameter("inventory");
            String department = request.getParameter("department");
            String sign = request.getParameter("sign");
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());//当日
            if(sign != null && !sign.equals("")
                    && customer != null && !customer.equals("")
                    && inventory != null && !inventory.equals("")
                    && department != null && !department.equals("")
                    && sign.equals(Md5.md5(customer+inventory+department+today))){
                System.out.println("传入的参数： customer ====== " + customer + ",inventory ====== " + inventory + ",department ====== " + department);
                String price = tokenService.getsaleprice(customer,inventory,department);
                System.out.println("最外层 --------------- price ====== " + price);
                if(price == null || "".equals(price) || "null".equals(price)){
                    return "999999";
                }else{
                    return price;
                }
            }else{
                String price = "999999";
                return price;
            }
        /*}else{
            return "999999";
        }*/
    }


    // 免费给千佛做的，用来获取T+的 往来单位存货设置表
    @RequestMapping(value="/getpartnerinventory", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String getpartnerinventory(HttpServletRequest request, HttpServletResponse response){
        String IP = RequestIPUtils.getIpAddr(request);//47.108.88.144
        System.out.println("获取往来单位存货设置 IP ======= " + IP);
        //if("47.108.88.144".equals(IP)){
            String customer = request.getParameter("customer");//传入 all 或者 具体某个 往来编码
            String sign = request.getParameter("sign");
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());//当日
            if(sign != null && !sign.equals("")
                    && customer != null && !customer.equals("")
                    && sign.equals(Md5.md5(customer+today))){
                List<Map<String,String>> datalist = tokenService.getpartnerinventory(customer);
                return JSONObject.toJSONString(datalist);
            }else{
                return "参数错误";
            }
        /*}else{
            return "参数错误";
        }*/
    }
}