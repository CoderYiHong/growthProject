package com.yihong.growth.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.dto.SkillDTO;
import com.yihong.growth.entity.Skill;
import com.yihong.growth.mapper.SkillMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/skills")
@RequiredArgsConstructor
public class AdminSkillController {

    private final SkillMapper mapper;

    @GetMapping
    public Result<List<Skill>> list() {
        return Result.ok(mapper.selectList(new LambdaQueryWrapper<Skill>().orderByAsc(Skill::getSortOrder)));
    }

    @OperationLog(value = "新增技能", module = "技能管理")
    @PostMapping
    public Result<?> create(@Valid @RequestBody SkillDTO dto) {
        Skill s = new Skill();
        s.setName(dto.getName()); s.setCategory(dto.getCategory());
        s.setLevel(dto.getLevel()); s.setPercentage(dto.getPercentage());
        s.setSortOrder(dto.getSortOrder()); s.setVisible(dto.getVisible());
        s.setId(null);
        mapper.insert(s);
        return Result.ok("新增成功");
    }

    @OperationLog(value = "更新技能", module = "技能管理")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody SkillDTO dto) {
        Skill s = new Skill();
        s.setId(id);
        // 部分更新：只更新 DTO 中传入了的字段
        if (dto.getName() != null) s.setName(dto.getName());
        if (dto.getCategory() != null) s.setCategory(dto.getCategory());
        if (dto.getLevel() != null) s.setLevel(dto.getLevel());
        if (dto.getPercentage() != null) s.setPercentage(dto.getPercentage());
        if (dto.getSortOrder() != null) s.setSortOrder(dto.getSortOrder());
        if (dto.getVisible() != null) s.setVisible(dto.getVisible());
        int affected = mapper.updateById(s);
        return affected > 0 ? Result.ok("更新成功") : Result.error(404, "记录不存在");
    }

    @OperationLog(value = "删除技能", module = "技能管理")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        int affected = mapper.deleteById(id);
        return affected > 0 ? Result.ok("删除成功") : Result.error(404, "记录不存在");
    }
}
