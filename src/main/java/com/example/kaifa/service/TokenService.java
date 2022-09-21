package com.example.kaifa.service;

import java.util.List;
import java.util.Map;

public interface TokenService {

    public String getsaleprice(String customer,String inventory,String department);

    public String getSaNameUnitPrice(String customer,String inventory,String department,String name,String discount,String dcodee);

    public List<Map<String,String>> getpartnerinventory(String customer);
}
