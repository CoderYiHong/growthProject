package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.PageResult;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.ProjectDTO;
import com.yihong.growth.entity.Project;
import com.yihong.growth.mapper.ProjectMapper;
import com.yihong.growth.service.GithubProjectSyncService;
import com.yihong.growth.service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectMapper projectMapper;
    private final ProjectService projectService;
    private final GithubProjectSyncService githubProjectSyncService;

    @GetMapping
    public Result<PageResult<Project>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer visible) {
        pageSize = Math.min(pageSize, 100);
        LambdaQueryWrapper<Project> qw = new LambdaQueryWrapper<>();
        qw.eq(Project::getSourceType, "github");
        if (keyword != null && !keyword.isEmpty()) qw.like(Project::getName, keyword);
        if (category != null && !category.isEmpty()) qw.eq(Project::getCategory, category);
        if (status != null && !status.isEmpty()) qw.eq(Project::getStatus, status);
        if (visible != null) qw.eq(Project::getVisible, visible);
        qw.orderByAsc(Project::getSortOrder);
        Page<Project> p = projectMapper.selectPage(new Page<>(page, pageSize), qw);
        return Result.ok(PageResult.of(p.getTotal(), pageSize, p.getCurrent(), p.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id, HttpServletResponse response) {
        Project p = projectMapper.selectById(id);
        if (p == null) { response.setStatus(404); return Result.error(404, "项目不存在"); }
        return Result.ok(p);
    }

    @OperationLog(value = "新增项目", module = "项目管理")
    @PostMapping
    public Result<?> create(@Valid @RequestBody ProjectDTO dto) {
        projectService.create(dto);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新项目", module = "项目管理")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ProjectDTO dto) {
        projectService.update(id, dto);
        return Result.ok("更新成功");
    }

    @OperationLog(value = "同步 GitHub 项目", module = "项目管理")
    @PostMapping("/sync/github")
    public Result<GithubProjectSyncService.SyncResult> syncGithub() {
        return Result.ok("GitHub 同步完成", githubProjectSyncService.sync());
    }

    @OperationLog(value = "删除项目", module = "项目管理")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        int affected = projectMapper.deleteById(id);
        return affected > 0 ? Result.ok("删除成功") : Result.error(404, "记录不存在");
    }
}
