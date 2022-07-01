package com.example.kaifa.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface orderMapper {

    //返回数据库中 XXX_code_token 所有记录的 企业信息
    public List<Map<String,String>> getDBAllOrgList();

    //调用refreshtoken 后更新数据库
    public void updateOrgToken(Map<String,String> updateMap);

    //根据appkey 获取 当前的 token
    public String getTokenByAppKey(@Param("AppKey") String AppKey);

    //根据 OrgId 获取 当前的 AppKey 和 AppSecret
    public Map<String,String> getAppKeySecretByAppKey(@Param("OrgId") String OrgId);

    //根据单据编号 或者 附件 id
    public List<Map<String,String>> getfjidByCode(@Param("code")String code);

    public List<Map<String,String>> getSalePriceList();

}
