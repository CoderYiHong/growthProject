package com.yihong.growth.controller;

import com.yihong.growth.common.Result;
import com.yihong.growth.dto.LoginDTO;
import com.yihong.growth.service.AuthService;
import com.yihong.growth.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request,
                           HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        if (!rateLimitService.tryAcquire(ip)) {
            response.setStatus(429);
            return Result.error(429, "登录尝试过于频繁，请 1 分钟后再试");
        }
        // AuthenticationException → 全局处理器 → HTTP 401
        return Result.ok("登录成功", authService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            authService.logout(auth.substring(7));
        }
        return Result.ok();
    }
}
