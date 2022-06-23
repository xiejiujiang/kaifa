package com.example.kaifa.controller;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class HQDemo {

    //测试环境
    //public static String hqurl = "https://www.hqwg.com.cn:9993/?OAH024";
    public static String hqurl = "https://www.hqwg.com.cn/?OAH027";//正式
    public static String prvid = "05192";
    public static String tel = "18428394131";
    public static String prvkey = "syfapvrg1pkwNKIwLIpOTAOB131iHT";
    //public static String deskey = "8aue2u3q";//测试，但是 1.3,1.4,1.5,1.6  用的又是这个，我在代码中写死了的。
    public static String deskey = "jsepc1xj";//正式

    public static void main(String[] args) {
        StringBuffer json = new StringBuffer();
        //String prvid="05192";
        //String tel="18428394131";
        //String prvkey="syfapvrg1pkwNKIwLIpOTAOB131iHT";
        String ts = (System.currentTimeMillis() / 1000L)+"";

        //可以多单 同时 传，但 这里 只要 测试 一单
        json.append("{\"prvid\":\""+prvid+"\",\"tel\":\""+tel+"\",\"Request_Channel\":\"WEB\",\"method\":\"uploadOrder\",\"timestamp\":\"" + ts + "\",\"token\":\"" + md5(prvkey+prvid+tel + ts) + "\",\"datas\":[");
        Random rdm = new Random();
        //for (int i = 0; i < 1; i++) {
        StringBuilder item = new StringBuilder();
        item.append("{\"lnkshpno\":\"05192112233445561234\",\"hndno\":\"0519211223344556\",\"prvid\":\""+prvid+"\",\"dptid\":\"688\",\"mkdat\":\"20210525\",\"snddat\":\"20220408\",\"sndusr\":\"13888888888\",\"brief\":\"直送单备注\"");
        item.append(",\"items\":[");
        for (int j = 0; j < 1; j++) {//具体的商品明细
            item.append("{\"gdsid\":\"" + 18002335 + "\",\"prvgdsid\":\"10001\",\"qty\":\"9\",\"bsepkg\":\"支\",\"prvprc\":\"2\",\"prvamt\":\"" + rdm.nextFloat() + "\",\"bthno\":\"20210525\",\"vlddat\":\"30\",\"crtdat\":\"20210525\"},");
        }
        item.setLength(item.length() - 1);
        item.append("]}");
        String sign = md5(item.toString());
        item.insert(1, "\"sign\":\"" + sign + "\",");
        json.append(item.toString());
        json.append(",");
        //}
        json.setLength(json.length() - 1);
        json.append("]}");

        System.out.println("json == " + json);

        String result = request("https://www.hqwg.com.cn:9993/?OAH024", "8aue2u3q", json.toString());
        String decryptData = desDecrypt("8aue2u3q", result);
        System.out.println("请求结果："+decryptData);
    }

    public static String request(String url, String key, String postData) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("POST");
            //conn.setRequestProperty("Content-type", "application/json");

            String encrptyData = "json=" + URLEncoder.encode(desEncrypt(key, postData), "utf-8");
            byte[] bytes = encrptyData.getBytes("utf-8");
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
            reader.close();
            conn.disconnect();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String desEncrypt(String key, String data) {
        byte[] retVal = null;
        try {
            byte[] key_byte = key.getBytes("UTF-8");
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


    public static String desDecrypt(String key, String data) {
        byte[] retVal = null;
        try {
            byte[] key_byte = key.getBytes("UTF-8");
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


    public static String md5(String data) {
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
}
