package com.yihong.growth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 跨域配置 — 仅允许配置文件中明确列出的 Origin
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.startsWith("http://") || s.startsWith("https://"))
                .collect(Collectors.toList());

        if (origins.isEmpty()) {
            log.warn("CORS: 未配置任何允许的来源，跨域请求将被拒绝");
        } else {
            log.info("CORS: 允许来源 = {}", origins);
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origins);  // 精确匹配
        config.addAllowedMethod("*");
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.setAllowCredentials(false);           // Token 认证，不需要 credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
