package com.yihong.growth.controller.admin;

import com.yihong.growth.annotation.OperationLog;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.*;
import com.yihong.growth.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/study-plans")
@RequiredArgsConstructor
public class AdminStudyPlanController {

    private final StudyPlanService service;

    // ===== 计划 =====

    @GetMapping
    public Result<List<StudyPlan>> listPlans() {
        return Result.ok(service.listPlans());
    }

    @OperationLog(value = "新增学习计划", module = "学习计划")
    @PostMapping
    public Result<StudyPlan> create(@RequestBody StudyPlan plan) {
        return Result.ok("创建成功", service.createPlan(plan));
    }

    @OperationLog(value = "更新学习计划", module = "学习计划")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody StudyPlan plan) {
        service.updatePlan(id, plan);
        return Result.ok("更新成功");
    }

    @OperationLog(value = "删除学习计划", module = "学习计划")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        service.deletePlan(id);
        return Result.ok("删除成功");
    }

    // ===== 系列 =====

    @GetMapping("/{planId}/groups")
    public Result<List<PlanGroup>> listGroups(@PathVariable Long planId) {
        return Result.ok(service.listGroups(planId));
    }

    @PostMapping("/{planId}/groups")
    public Result<PlanGroup> createGroup(@PathVariable Long planId, @RequestBody PlanGroup group) {
        group.setPlanId(planId);
        return Result.ok("创建成功", service.createGroup(group));
    }

    @PutMapping("/{planId}/groups/{groupId}")
    public Result<?> updateGroup(@PathVariable Long planId, @PathVariable Long groupId, @RequestBody PlanGroup group) {
        group.setPlanId(planId);
        service.updateGroup(groupId, group);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{planId}/groups/{groupId}")
    public Result<?> deleteGroup(@PathVariable Long planId, @PathVariable Long groupId) {
        service.deleteGroup(groupId);
        return Result.ok("删除成功");
    }

    // ===== 子任务 =====

    @GetMapping("/{planId}/items")
    public Result<List<PlanItem>> listItems(@PathVariable Long planId,
                                             @RequestParam(required = false) Long groupId) {
        return Result.ok(service.listItems(planId, groupId));
    }

    @PostMapping("/{planId}/items")
    public Result<PlanItem> createItem(@PathVariable Long planId, @RequestBody PlanItem item) {
        item.setPlanId(planId);
        return Result.ok("创建成功", service.createItem(item));
    }

    @PutMapping("/{planId}/items/{itemId}")
    public Result<?> updateItem(@PathVariable Long planId, @PathVariable Long itemId, @RequestBody PlanItem item) {
        item.setPlanId(planId);
        service.updateItem(itemId, item);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{planId}/items/{itemId}")
    public Result<?> deleteItem(@PathVariable Long planId, @PathVariable Long itemId) {
        service.deleteItem(itemId);
        return Result.ok("删除成功");
    }

    // ===== 打卡 =====

    @GetMapping("/{planId}/checkins")
    public Result<List<CheckinRecord>> listCheckins(@PathVariable Long planId) {
        return Result.ok(service.listCheckins(planId));
    }

    @PostMapping("/{planId}/checkins")
    public Result<CheckinRecord> createCheckin(@PathVariable Long planId, @RequestBody CheckinRecord record) {
        record.setPlanId(planId);
        return Result.ok("打卡成功", service.createCheckin(record));
    }

    // ===== 批量导入 =====

    @PostMapping("/import")
    public Result<Map<String, Object>> importPlans(@RequestBody List<StudyPlan> plans) {
        return Result.ok("导入完成", service.importPlans(plans));
    }
}
