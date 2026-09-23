package com.yihong.growth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.*;
import com.yihong.growth.mapper.*;
import com.yihong.growth.service.SettingService;
import com.yihong.growth.service.TimetableService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台公开接口 — 不需要登录
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ProjectMapper projectMapper;
    private final ArticleMapper articleMapper;
    private final GrowthStageMapper growthStageMapper;
    private final SkillMapper skillMapper;
    private final SettingService settingService;
    private final TimetableService timetableService;

    // ===== 项目 =====
    @GetMapping("/projects")
    public Result<List<Project>> projects() {
        List<Project> list = projectMapper.selectList(
            new LambdaQueryWrapper<Project>()
                .eq(Project::getSourceType, "github")
                .eq(Project::getVisible, 1)
                .orderByAsc(Project::getSortOrder)
        );
        return Result.ok(list);
    }

    @GetMapping("/projects/{id}")
    public Result<Project> projectDetail(@PathVariable Long id, HttpServletResponse response) {
        Project p = projectMapper.selectOne(
            new LambdaQueryWrapper<Project>()
                .eq(Project::getId, id)
                .eq(Project::getSourceType, "github")
                .eq(Project::getVisible, 1));
        if (p == null) { response.setStatus(404); return Result.error(404, "项目不存在"); }
        return Result.ok(p);
    }

    // ===== 文章 =====
    @GetMapping("/articles")
    public Result<List<Article>> articles() {
        List<Article> list = articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getSourceType, "csdn")
                .eq(Article::getStatus, "published")
                .eq(Article::getVisible, 1)
                .orderByDesc(Article::getCreateTime)
        );
        return Result.ok(list);
    }

    @GetMapping("/articles/{id}")
    public Result<Article> articleDetail(@PathVariable Long id, HttpServletResponse response) {
        Article a = articleMapper.selectOne(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getId, id)
                .eq(Article::getSourceType, "csdn")
                .eq(Article::getStatus, "published")
                .eq(Article::getVisible, 1));
        if (a == null) { response.setStatus(404); return Result.error(404, "文章不存在"); }
        return Result.ok(a);
    }

    // ===== 成长轨迹 =====
    @GetMapping("/growth")
    public Result<List<GrowthStage>> growth() {
        List<GrowthStage> list = growthStageMapper.selectList(
            new LambdaQueryWrapper<GrowthStage>()
                .eq(GrowthStage::getVisible, 1)
                .orderByAsc(GrowthStage::getSortOrder)
        );
        return Result.ok(list);
    }

    // ===== 技能 =====
    @GetMapping("/skills")
    public Result<List<Skill>> skills() {
        List<Skill> list = skillMapper.selectList(
            new LambdaQueryWrapper<Skill>()
                .eq(Skill::getVisible, 1)
                .orderByAsc(Skill::getSortOrder)
        );
        return Result.ok(list);
    }

    // ===== 网站设置（公开，供前端 Header/Footer 使用） =====
    @GetMapping("/settings")
    public Result<Map<String, String>> siteSettings() {
        return Result.ok(settingService.getAll());
    }

    // ===== 课表 =====
    @GetMapping("/timetable")
    public Result<Map<String, Object>> timetable() {
        return Result.ok(timetableService.aggregate());
    }

    // ===== 仪表盘公开统计 =====
    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.ok(java.util.Map.of(
            "projectCount", projectMapper.selectCount(new LambdaQueryWrapper<Project>()
                    .eq(Project::getSourceType, "github")
                    .eq(Project::getVisible, 1)),
            "articleCount", articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                    .eq(Article::getSourceType, "csdn")
                    .eq(Article::getStatus, "published")
                    .eq(Article::getVisible, 1))
        ));
    }
}
