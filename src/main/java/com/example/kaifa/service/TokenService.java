package com.example.kaifa.service;

public interface TokenService {

    public String refreshToken();

    public String getsaleprice(String customer,String inventory);

    public String getSaNamePrice(String customer,String inventory,String name);
}
