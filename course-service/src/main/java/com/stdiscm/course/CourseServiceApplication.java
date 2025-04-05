package com.stdiscm.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EntityScan(basePackages = {"com.stdiscm.common.model", "com.stdiscm.course.model"})
public class CourseServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}
