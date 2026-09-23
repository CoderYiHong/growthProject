package com.yihong.growth.aspect;

import com.yihong.growth.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Parameter;
import java.time.LocalDateTime;

/**
 * 操作日志切面 — 自动记录标注了 @OperationLog 的管理操作
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper logMapper;

    @Around("@annotation(com.yihong.growth.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        com.yihong.growth.annotation.OperationLog anno =
                signature.getMethod().getAnnotation(com.yihong.growth.annotation.OperationLog.class);

        String action = anno.value();
        String module = anno.module();
        String userName = getCurrentUser();
        String ip = getClientIp();
        String targetId = extractId(joinPoint, signature);

        com.yihong.growth.entity.OperationLog log = new com.yihong.growth.entity.OperationLog();
        log.setUserName(userName);
        log.setAction(action);
        log.setModule(module);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());

        try {
            Object result = joinPoint.proceed();
            log.setStatus("success");
            log.setDetail(buildDetail(module, action, targetId, null));
            return result;
        } catch (Throwable e) {
            log.setStatus("failed");
            log.setDetail(buildDetail(module, action, targetId, e.getMessage()));
            throw e;
        } finally {
            try {
                logMapper.insert(log);
            } catch (Exception ignored) {
                // 日志记录失败不影响业务
            }
        }
    }

    private String buildDetail(String module, String action, String targetId, String error) {
        StringBuilder sb = new StringBuilder();
        sb.append(module).append(" - ").append(action);
        if (targetId != null && !targetId.isEmpty()) {
            sb.append(" [ID=").append(targetId).append("]");
        }
        if (error != null && !error.isEmpty()) {
            sb.append(" 失败: ").append(error);
        }
        return sb.toString();
    }

    /** 从方法参数中提取实体 ID */
    private String extractId(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        Object[] args = joinPoint.getArgs();
        Parameter[] params = signature.getMethod().getParameters();
        for (int i = 0; i < params.length; i++) {
            String name = params[i].getName();
            if ("id".equals(name) && args[i] instanceof Long) {
                return args[i].toString();
            }
        }
        // 兜底：从 @PathVariable 注解中读取
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(org.springframework.web.bind.annotation.PathVariable.class)
                    && args[i] instanceof Long) {
                return args[i].toString();
            }
        }
        return null;
    }

    private String getCurrentUser() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String auth = request.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    return "admin";
                }
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
                if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
                return ip;
            }
        } catch (Exception ignored) {}
        return "";
    }
}
