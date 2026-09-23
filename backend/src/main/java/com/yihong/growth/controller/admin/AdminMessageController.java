package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.PageResult;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.Message;
import com.yihong.growth.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageMapper mapper;

    private static final java.util.Set<String> ALLOWED_STATUS =
            java.util.Set.of("unread", "read", "replied");

    @GetMapping
    public Result<PageResult<Message>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        pageSize = Math.min(pageSize, 100);
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) qw.eq(Message::getStatus, status);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(Message::getName, keyword).or().like(Message::getEmail, keyword)
                    .or().like(Message::getSubject, keyword).or().like(Message::getContent, keyword));
        }
        qw.orderByDesc(Message::getCreateTime);
        Page<Message> p = mapper.selectPage(new Page<>(page, pageSize), qw);
        return Result.ok(PageResult.of(p.getTotal(), pageSize, p.getCurrent(), p.getRecords()));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!ALLOWED_STATUS.contains(status)) {
            return Result.error(400, "无效的状态值，允许: " + String.join(", ", ALLOWED_STATUS));
        }
        Message msg = new Message();
        msg.setId(id);
        msg.setStatus(status);
        mapper.updateById(msg);
        return Result.ok("更新成功");
    }

    @OperationLog(value = "删除留言", module = "留言管理")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        int affected = mapper.deleteById(id);
        return affected > 0 ? Result.ok("删除成功") : Result.error(404, "记录不存在");
    }

    /** 各状态留言数量统计，用于前台统计卡片 */
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        List<Message> all = mapper.selectList(null);
        long unread = all.stream().filter(m -> "unread".equals(m.getStatus())).count();
        long read = all.stream().filter(m -> "read".equals(m.getStatus())).count();
        long replied = all.stream().filter(m -> "replied".equals(m.getStatus())).count();
        return Result.ok(Map.of(
            "total", (long) all.size(),
            "unread", unread,
            "read", read,
            "replied", replied
        ));
    }
}
