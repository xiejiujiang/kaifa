package com.example.kaifa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.example.kaifa.mapper")
public class KaifaApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaifaApplication.class, args);
    }

}
