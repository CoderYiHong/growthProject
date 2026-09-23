package com.yihong.growth.controller.admin;

import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingController {

    private final SettingService settingService;

    @GetMapping
    public Result<Map<String, String>> getAll() {
        return Result.ok(settingService.getAll());
    }

    @OperationLog(value = "更新网站设置", module = "网站设置")
    @PutMapping
    public Result<?> save(@RequestBody Map<String, String> settings) {
        settingService.saveAll(settings);
        return Result.ok("保存成功");
    }
}
