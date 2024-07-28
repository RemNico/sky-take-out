package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid=#{openId}")
    User getOpenId(String openId);

    void insert(User user);

    @Select("select * from user WHERE id=#{userId}")
    User getById(Long userId);

    Integer getSum(Map map);

    Integer getUserToday(Map map);
}
