package com.yihong.growth.service.impl;

import com.yihong.growth.dto.LoginDTO;
import com.yihong.growth.service.AuthService;
import com.yihong.growth.service.SettingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * JWT 无状态认证实现。
 * Token 自包含签名和过期时间，后端重启不丢失登录态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SettingService settingService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password-hash}")
    private String configuredPasswordHash;

    @Value("${admin.token-secret}")
    private String tokenSecret;

    @Value("${admin.token-expire-hours:24}")
    private int tokenExpireHours;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 当前密码 Hash（支持运行时通过 changePassword 修改） */
    private volatile String currentPasswordHash;

    /** JWT 签名密钥，由 token-secret 派生 */
    private SecretKey signingKey;

    @PostConstruct
    public void validateConfiguration() {
        // 验证密码 Hash 格式
        if (configuredPasswordHash == null || configuredPasswordHash.isBlank()
                || !configuredPasswordHash.startsWith("$2a$")) {
            log.error("admin.password-hash 必须是一个有效的 BCrypt Hash（以 $2a$ 开头）。启动失败。");
            throw new IllegalStateException("admin.password-hash must be a valid BCrypt hash");
        }
        this.currentPasswordHash = configuredPasswordHash;

        // 验证 Token Secret（至少 32 字符）
        if (tokenSecret == null || tokenSecret.length() < 32) {
            log.error("admin.token-secret 长度不足（需 >= 32 字符）。启动失败。");
            throw new IllegalStateException("admin.token-secret must be at least 32 characters");
        }
        if (tokenSecret.matches("^[a-zA-Z0-9_-]+$") && tokenSecret.length() < 40) {
            log.error("admin.token-secret 强度不足：仅含字母数字且长度 < 40。启动失败。");
            throw new IllegalStateException("admin.token-secret must contain mixed character types or be >= 40 chars");
        }

        // 初始化 JWT 签名密钥（HMAC-SHA256 需要 >= 256 bits = 32 bytes）
        byte[] keyBytes = tokenSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("admin.token-secret must be at least 32 bytes for HMAC-SHA256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        log.info("认证模块初始化完成：BCrypt Hash 已加载，JWT 签名密钥已配置（HMAC-SHA256）");
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        if (!adminUsername.equals(loginDTO.getUsername())
                || !encoder.matches(loginDTO.getPassword(), currentPasswordHash)) {
            throw new AuthenticationException("账号或密码错误");
        }

        // remember 为 true 时有效期延长至 7 天
        int expireHours = loginDTO.getRemember() != null && loginDTO.getRemember()
                ? Math.max(tokenExpireHours, 168) : tokenExpireHours;

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireHours * 3_600_000L);

        String token = Jwts.builder()
                .subject(adminUsername)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();

        // 从 site_setting 读取 profile，无记录时兜底
        Map<String, String> settings = settingService.getAll();

        return Map.of(
            "token", token,
            "nickname", settings.getOrDefault("profile_nickname", "YiHong"),
            "email", settings.getOrDefault("profile_email", ""),
            "role", "admin"
        );
    }

    @Override
    public void logout(String token) {
        // JWT 无状态，无法主动失效单个 Token。
        // 前端清除 Token 后该 Token 不再被发送，服务端无需额外操作。
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT 已过期: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException
                 | SecurityException | IllegalArgumentException e) {
            log.debug("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!encoder.matches(oldPassword, currentPasswordHash)) return false;
        if (newPassword == null || newPassword.length() < 6) return false;
        this.currentPasswordHash = encoder.encode(newPassword);
        // JWT 无状态：已签发的旧 Token 在到期前仍然有效。
        // 如需立即让所有旧 Token 失效，修改 application.yml 中的 admin.token-secret 并重启。
        log.info("密码已修改。旧 JWT Token 将在各自到期后自动失效");
        return true;
    }

    /** 认证异常，全局处理器映射到 HTTP 401 */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String msg) { super(msg); }
    }
}
