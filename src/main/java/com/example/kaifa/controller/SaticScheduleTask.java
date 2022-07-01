package com.example.kaifa.controller;

import com.example.kaifa.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Configuration
@EnableScheduling
@Controller
public class SaticScheduleTask {

    @Autowired
    private TokenService tokenService;

    //每天凌晨3点执行
    @Scheduled(cron = "0 0 3 * * ?")
    private void configureTasks() {
        System.err.println("-------------------- 执行静态定时任务开始: " + LocalDateTime.now() + "--------------------");
        try{
            //tokenService.refreshToken();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.err.println("-------------------- 执行静态定时任务结束: " + LocalDateTime.now() + "--------------------");
    }
}