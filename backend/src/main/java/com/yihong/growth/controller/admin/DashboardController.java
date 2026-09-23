package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.Article;
import com.yihong.growth.entity.Project;
import com.yihong.growth.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectMapper projectMapper;
    private final ArticleMapper articleMapper;
    private final MessageMapper messageMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        // 统计文章总阅读量作为访问量参考（比硬编码数字更有意义）
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getSourceType, "csdn"));
        long totalViews = articles.stream().mapToLong(a -> a.getViews() == null ? 0L : a.getViews()).sum();

        return Result.ok(Map.of(
            "projectCount", projectMapper.selectCount(
                    new LambdaQueryWrapper<Project>().eq(Project::getSourceType, "github")),
            "articleCount", articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>().eq(Article::getSourceType, "csdn")),
            "messageCount", messageMapper.selectCount(null),
            "totalVisits", totalViews
        ));
    }

    /**
     * 访问趋势 — 最近 12 个月的文章发布量
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend() {
        List<Article> articles = articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getSourceType, "csdn")
                .eq(Article::getStatus, "published")
                .orderByAsc(Article::getCreateTime)
        );

        // 按月聚合文章发布量
        Map<String, Long> monthlyCount = articles.stream()
            .filter(a -> a.getCreateTime() != null)
            .collect(Collectors.groupingBy(
                a -> a.getCreateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                TreeMap::new,
                Collectors.counting()
            ));

        // 最近 12 个月
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("M月");

        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String key = month.format(fmt);
            long articlesCount = monthlyCount.getOrDefault(key, 0L);

            result.add(Map.of(
                "date", month.format(labelFmt),
                "articles", articlesCount,
                "visits", 0
            ));
        }

        return Result.ok(result);
    }

    /**
     * 内容分类统计 — 各分类下的文章数量
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        List<Article> articles = articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                    .eq(Article::getSourceType, "csdn")
                    .eq(Article::getStatus, "published")
        );

        Map<String, Long> categoryCount = articles.stream()
            .filter(a -> a.getCategory() != null && !a.getCategory().isEmpty())
            .collect(Collectors.groupingBy(
                Article::getCategory,
                Collectors.counting()
            ));

        if (categoryCount.isEmpty()) {
            return Result.ok(List.of(
                Map.of("name", "工具", "value", 0),
                Map.of("name", "后端", "value", 0),
                Map.of("name", "前端", "value", 0),
                Map.of("name", "AI", "value", 0)
            ));
        }

        List<Map<String, Object>> result = categoryCount.entrySet().stream()
            .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue()))
            .collect(Collectors.toList());

        return Result.ok(result);
    }
}
