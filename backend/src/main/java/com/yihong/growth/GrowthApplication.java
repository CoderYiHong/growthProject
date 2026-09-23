package com.yihong.growth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.yihong.growth.mapper")
public class GrowthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrowthApplication.class, args);
    }
}
