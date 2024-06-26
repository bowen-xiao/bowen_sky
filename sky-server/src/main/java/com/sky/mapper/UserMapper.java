package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openId}")
    User getUserByOpenId(String openId);

    void insert(User user);

    @Select("select * from user where openid = #{userId}")
    User getById(Long userId);

    Integer getByMap(HashMap map);
}
