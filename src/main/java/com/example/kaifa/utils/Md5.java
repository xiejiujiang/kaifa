package com.example.kaifa.utils;

import com.alibaba.fastjson.JSONArray;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Md5 {

    public static String md5(String data){
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes("UTF-8"));
            byte[] digest = md.digest();
            int i;
            for (int offset = 0; offset < digest.length; offset++) {
                i = digest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    sb.append(0);
                sb.append(Integer.toHexString(i));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(String[] args) {
//http://47.108.79.26:9998/kaifa/token/getsaleprice?customer=0010004&inventory=23100&department=02&sign=8c91707751d66218c6f0b3b7727a9a0b
        //customer+inventory+department+today
       System.out.println(Md5.md5("0010004"+"23127"+"02"+"20220920"));
    }
}
