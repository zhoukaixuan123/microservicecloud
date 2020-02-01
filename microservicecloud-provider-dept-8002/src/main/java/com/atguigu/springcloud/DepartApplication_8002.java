package com.atguigu.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author zhoukx
 * @date 2020/1/28
 * @description
 */
@EnableEurekaClient //本服务启动后会自动注册进
@SpringBootApplication
@EnableDiscoveryClient  // 服务发现
public class DepartApplication_8002 {
    public static void main(String[] args) {
        SpringApplication.run(DepartApplication_8002.class);
    }
}
