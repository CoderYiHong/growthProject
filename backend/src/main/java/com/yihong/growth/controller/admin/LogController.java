package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yihong.growth.common.PageResult;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.OperationLog;
import com.yihong.growth.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper mapper;

    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        pageSize = Math.min(pageSize, 100);
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) qw.eq(OperationLog::getModule, module);
        if (status != null && !status.isEmpty()) qw.eq(OperationLog::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(OperationLog::getAction, keyword)
                    .or().like(OperationLog::getDetail, keyword)
                    .or().like(OperationLog::getUserName, keyword)
                    .or().like(OperationLog::getIp, keyword));
        }
        qw.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> p = mapper.selectPage(new Page<>(page, pageSize), qw);
        return Result.ok(PageResult.of(p.getTotal(), pageSize, p.getCurrent(), p.getRecords()));
    }

    /** 获取所有不重复的模块名称（用于筛选下拉） */
    @GetMapping("/modules")
    public Result<List<String>> modules() {
        // 仅查询 module 列以减少数据传输
        List<OperationLog> logs = mapper.selectList(
            new LambdaQueryWrapper<OperationLog>()
                .select(OperationLog::getModule)
                .last("LIMIT 5000")
        );
        List<String> modules = logs.stream()
                .map(OperationLog::getModule)
                .filter(m -> m != null && !m.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return Result.ok(modules);
    }
}
