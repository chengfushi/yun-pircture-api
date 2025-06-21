package com.chengfu.yunpictureapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author chengfu
 */
@SpringBootApplication
@MapperScan("com.chengfu.yunpictureapi.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class YunPictureApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(YunPictureApiApplication.class, args);
    }

}
