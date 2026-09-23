package com.yihong.growth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI growthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("YiHong 个人成长作品集 API")
                        .description("前台展示 + 后台管理 REST API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("YiHong")
                                .email("coderyihong@gmail.com")));
    }
}
