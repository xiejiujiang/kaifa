package com.example.kaifa.service;

import com.example.kaifa.entity.saentity.JsonRootBean;
import java.util.List;
import java.util.Map;

public interface BasicService {

    //这个里面 封装了 所有  T+ 基础类的 接口API 调用方法

    //创建项目
    public String createXM(Map<String,String> params);

    //查询项目
    public String getXM(Map<String,String> params);

    //创建项目分类
    public String createXMClass(Map<String,String> params);

    //查询项目分类
    public String getXMClass(Map<String,String> params);

    //查询仓库
    public String getWarehouse(Map<String,String> params);

    //创建仓库
    public String createWarehouse(Map<String,String> params);

    //创建凭证
    public String createPZ(Map<String,String> params);

    //根据 单据编号 返回 附件 ID
    public List<Map<String,String>> getfjidByCode(String code);

}