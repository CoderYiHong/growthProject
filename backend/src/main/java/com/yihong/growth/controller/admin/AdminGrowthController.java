package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.GrowthStageDTO;
import com.yihong.growth.entity.GrowthStage;
import com.yihong.growth.mapper.GrowthStageMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/growth")
@RequiredArgsConstructor
public class AdminGrowthController {

    private final GrowthStageMapper mapper;

    @GetMapping
    public Result<List<GrowthStage>> list() {
        return Result.ok(mapper.selectList(
            new LambdaQueryWrapper<GrowthStage>().orderByAsc(GrowthStage::getSortOrder)));
    }

    @OperationLog(value = "新增成长阶段", module = "成长经历")
    @PostMapping
    public Result<?> create(@Valid @RequestBody GrowthStageDTO dto) {
        GrowthStage s = new GrowthStage();
        BeanUtils.copyProperties(dto, s);
        s.setId(null);
        mapper.insert(s);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新成长阶段", module = "成长经历")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody GrowthStageDTO dto) {
        GrowthStage s = new GrowthStage();
        s.setId(id);
        // 部分更新：只更新 DTO 中传入了的字段
        if (dto.getTitle() != null) s.setTitle(dto.getTitle());
        if (dto.getPeriod() != null) s.setPeriod(dto.getPeriod());
        if (dto.getSubtitle() != null) s.setSubtitle(dto.getSubtitle());
        if (dto.getIcon() != null) s.setIcon(dto.getIcon());
        if (dto.getDescription() != null) s.setDescription(dto.getDescription());
        if (dto.getAchievements() != null) s.setAchievements(dto.getAchievements());
        if (dto.getSkills() != null) s.setSkills(dto.getSkills());
        if (dto.getColor() != null) s.setColor(dto.getColor());
        if (dto.getSortOrder() != null) s.setSortOrder(dto.getSortOrder());
        if (dto.getVisible() != null) s.setVisible(dto.getVisible());
        int affected = mapper.updateById(s);
        return affected > 0 ? Result.ok("更新成功") : Result.error(404, "记录不存在");
    }

    @OperationLog(value = "删除成长阶段", module = "成长经历")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        int affected = mapper.deleteById(id);
        return affected > 0 ? Result.ok("删除成功") : Result.error(404, "记录不存在");
    }
}
