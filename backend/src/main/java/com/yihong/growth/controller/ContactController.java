package com.yihong.growth.controller;

import com.yihong.growth.common.Result;
import com.yihong.growth.dto.ContactFormDTO;
import com.yihong.growth.service.ContactService;
import com.yihong.growth.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final RateLimitService rateLimitService;

    @PostMapping
    public Result<?> send(@Valid @RequestBody ContactFormDTO dto, HttpServletRequest request,
                          HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        if (!rateLimitService.tryAcquire(ip)) {
            response.setStatus(429);
            return Result.error(429, "提交过于频繁，请稍后再试");
        }
        boolean emailNotified = contactService.sendMessage(dto, ip);
        String message = emailNotified
                ? "留言发送成功，我会通过邮箱尽快回复你！"
                : "留言已保存，邮件通知暂未启用。";
        return Result.ok(message, Map.of("emailNotified", emailNotified));
    }
}
