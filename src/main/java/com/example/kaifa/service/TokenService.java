package com.example.kaifa.service;

import java.util.List;
import java.util.Map;

public interface TokenService {

    public String getsaleprice(String customer,String inventory,String department);

    public String getSaNamePrice(String customer,String inventory,String department,String name);

    public List<Map<String,String>> getpartnerinventory(String customer);
}
