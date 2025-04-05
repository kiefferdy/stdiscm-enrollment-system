package com.stdiscm.grade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan(basePackages = {"com.stdiscm.common.model", "com.stdiscm.grade.model"})
public class GradeServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GradeServiceApplication.class, args);
    }
}
