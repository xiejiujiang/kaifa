package com.example.kaifa.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

public class Des {

    //des加密
    public static String desEncrypt(String key, String data) {
        byte[] retVal = null;
        try {
            byte[] key_byte  = key.getBytes("UTF-8");
            byte[] data_byte = data.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key_byte);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key_byte);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            retVal = cipher.doFinal(data_byte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(retVal);
    }

    //des解密
    public static String desDecrypt(String key, String data) {
        byte[] retVal = null;
        try {
            byte[] key_byte  = key.getBytes("UTF-8");
            byte[] data_byte = Base64.getDecoder().decode(data.getBytes("UTF-8"));
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key_byte);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key_byte);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            retVal = cipher.doFinal(data_byte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(retVal);
    }


    public static void main(String[] args) {
        String qty = "-4";
        if(Integer.valueOf(qty) < 0){
            qty = "" + (0-Integer.valueOf(qty));
        }
        System.out.println(qty);
    }
}