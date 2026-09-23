package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.PageResult;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.ArticleDTO;
import com.yihong.growth.entity.Article;
import com.yihong.growth.mapper.ArticleMapper;
import com.yihong.growth.service.CsdnArticleSyncService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleMapper articleMapper;
    private final CsdnArticleSyncService csdnArticleSyncService;

    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer visible) {
        pageSize = Math.min(pageSize, 100);
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<>();
        qw.eq(Article::getSourceType, "csdn");
        if (keyword != null && !keyword.isEmpty()) qw.like(Article::getTitle, keyword);
        if (category != null && !category.isEmpty()) qw.eq(Article::getCategory, category);
        if (status != null && !status.isEmpty()) qw.eq(Article::getStatus, status);
        if (visible != null) qw.eq(Article::getVisible, visible);
        qw.orderByDesc(Article::getCreateTime);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, pageSize), qw);
        return Result.ok(PageResult.of(p.getTotal(), pageSize, p.getCurrent(), p.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id, HttpServletResponse response) {
        Article a = articleMapper.selectById(id);
        if (a == null) { response.setStatus(404); return Result.error(404, "文章不存在"); }
        return Result.ok(a);
    }

    @OperationLog(value = "新增文章", module = "文章管理")
    @PostMapping
    public Result<?> create(@Valid @RequestBody ArticleDTO dto) {
        Article a = new Article();
        BeanUtils.copyProperties(dto, a);
        a.setId(null);
        if (a.getSourceUrl() != null && a.getSourceUrl().startsWith("https://blog.csdn.net/")) {
            a.setSourceType("csdn");
        }
        articleMapper.insert(a);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新文章", module = "文章管理")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ArticleDTO dto) {
        // 部分更新：只更新 DTO 中传入了的字段
        Article a = new Article();
        a.setId(id);
        if (dto.getTitle() != null) a.setTitle(dto.getTitle());
        if (dto.getSummary() != null) a.setSummary(dto.getSummary());
        if (dto.getCategory() != null) a.setCategory(dto.getCategory());
        if (dto.getCover() != null) a.setCover(dto.getCover());
        if (dto.getTags() != null) a.setTags(dto.getTags());
        if (dto.getContent() != null) a.setContent(dto.getContent());
        if (dto.getStatus() != null) a.setStatus(dto.getStatus());
        if (dto.getIsRecommended() != null) a.setIsRecommended(dto.getIsRecommended() ? 1 : 0);
        if (dto.getIsHot() != null) a.setIsHot(dto.getIsHot() ? 1 : 0);
        if (dto.getVisible() != null) a.setVisible(dto.getVisible());
        if (dto.getAuthor() != null) a.setAuthor(dto.getAuthor());
        if (dto.getSourceUrl() != null) a.setSourceUrl(dto.getSourceUrl());
        int affected = articleMapper.updateById(a);
        return affected > 0 ? Result.ok("更新成功") : Result.error(404, "文章不存在");
    }

    @OperationLog(value = "同步 CSDN 文章", module = "文章管理")
    @PostMapping("/sync/csdn")
    public Result<CsdnArticleSyncService.SyncResult> syncCsdn() {
        return Result.ok("CSDN 同步完成", csdnArticleSyncService.sync());
    }

    @OperationLog(value = "删除文章", module = "文章管理")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        int affected = articleMapper.deleteById(id);
        return affected > 0 ? Result.ok("删除成功") : Result.error(404, "记录不存在");
    }
}
