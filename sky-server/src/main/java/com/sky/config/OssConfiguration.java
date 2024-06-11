package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    //由容器进行管理，启动就进行注入
    @Bean
    //仅创建一个实例对象
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties properties){
        log.info("开始创建阿里文件上传对象:{}",properties);
        return new AliOssUtil(properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName());
    }
}
