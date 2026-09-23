package com.yihong.growth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yihong.growth.common.Result;
import com.yihong.growth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ObjectMapper objectMapper;  // Spring 注入，保持全局 Jackson 配置一致

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行 OPTIONS（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            sendError(response, 401, "未登录或 Token 已过期");
            return false;
        }

        if (!authService.validateToken(auth.substring(7))) {
            sendError(response, 401, "未登录或 Token 已过期");
            return false;
        }

        return true;
    }

    private void sendError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, msg)));
    }
}
