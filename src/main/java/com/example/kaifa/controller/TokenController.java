package com.example.kaifa.controller;

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
        String customer = request.getParameter("customer");
        String inventory = request.getParameter("inventory");
        String department = request.getParameter("department");
        String sign = request.getParameter("sign");
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());//当日
        if(sign != null && !sign.equals("")
                && customer != null && !customer.equals("")
                && inventory != null && !inventory.equals("")
                && sign.equals(Md5.md5(customer+inventory+department+today))){
            String price = tokenService.getsaleprice(customer,inventory,department);
            return price;
        }else{
            String price = "999999";
            return price;
        }
    }
}