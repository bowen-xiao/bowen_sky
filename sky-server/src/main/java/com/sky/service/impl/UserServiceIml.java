package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;

import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceIml implements UserService {

    private static final String WX_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    WeChatProperties weChatProperties;

    @Autowired
    UserMapper userMapper;

    @Override
    public User wxLogin(UserLoginDTO loginDTO){
        String openid = getOpenId(loginDTO.getCode());
        log.info("当前微信用户的openid : {}" ,openid);
        // openid 查询数据库中是否有用户
        User user = userMapper.getUserByOpenId(openid);
        // 没有用户，就创建新用户
        if(user == null ){
            user = User.builder()
                        .openid(openid)
                        .createTime(LocalDateTime.now())
                        .build();
            userMapper.insert(user);
        }
        // 返回用户登录后的信息
        return user;
    }

    //获取到微信openID
    private String getOpenId(String code) {
        HashMap<String, String> reqMap = new HashMap<String, String>();
        reqMap.put("appid",weChatProperties.getAppid());
        reqMap.put("secret",weChatProperties.getSecret());
        reqMap.put("js_code", code);
        reqMap.put("grant_type","authorization_code");

        // 使用httpclicent工具类向微信发出登录请求，获取到 openid
        String jsonStr = HttpClientUtil.doGet(WX_URL, reqMap);
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
