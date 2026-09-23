package com.yihong.growth.controller.admin;

import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.service.AuthService;
import com.yihong.growth.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AuthService authService;
    private final SettingService settingService;

    /** 获取个人信息 — 从 site_setting 读取，无记录时返回默认值 */
    @GetMapping
    public Result<Map<String, Object>> getProfile() {
        Map<String, String> settings = settingService.getAll();
        return Result.ok(Map.of(
            "nickname", settings.getOrDefault("profile_nickname", "YiHong"),
            "email", settings.getOrDefault("profile_email", "yihong@example.com"),
            "bio", settings.getOrDefault("profile_bio", ""),
            "avatar", settings.getOrDefault("profile_avatar", ""),
            "role", "admin"
        ));
    }

    /** 更新个人信息 */
    @OperationLog(value = "更新个人信息", module = "个人设置")
    @PutMapping
    public Result<?> updateProfile(@RequestBody Map<String, String> body) {
        settingService.putIfPresent("profile_nickname", body.get("nickname"));
        settingService.putIfPresent("profile_email", body.get("email"));
        settingService.putIfPresent("profile_bio", body.get("bio"));
        settingService.putIfPresent("profile_avatar", body.get("avatar"));
        return Result.ok("保存成功");
    }

    /** 修改密码 */
    @OperationLog(value = "修改密码", module = "个人设置")
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> body) {
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");

        if (oldPwd == null || oldPwd.isBlank()) {
            return Result.error(400, "原密码不能为空");
        }
        if (newPwd == null || newPwd.length() < 6) {
            return Result.error(400, "新密码至少6位");
        }

        boolean success = authService.changePassword(oldPwd, newPwd);
        return success ? Result.ok("密码修改成功，请重新登录") : Result.error(400, "原密码错误");
    }
}
