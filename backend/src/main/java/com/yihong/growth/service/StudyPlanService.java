package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.entity.*;
import com.yihong.growth.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanMapper planMapper;
    private final PlanGroupMapper groupMapper;
    private final PlanItemMapper itemMapper;
    private final CheckinRecordMapper checkinMapper;

    // ===== 计划 CRUD =====

    public List<StudyPlan> listPlans() {
        return planMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>().orderByDesc(StudyPlan::getUpdatedAt));
    }

    public StudyPlan createPlan(StudyPlan plan) {
        plan.setId(null);
        plan.setActualHours(plan.getActualHours() != null ? plan.getActualHours() : 0.0);
        plan.setProgress(plan.getProgress() != null ? plan.getProgress() : 0);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(plan);
        return plan;
    }

    public void updatePlan(Long id, StudyPlan plan) {
        plan.setId(id);
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        itemMapper.delete(new LambdaQueryWrapper<PlanItem>().eq(PlanItem::getPlanId, id));
        groupMapper.delete(new LambdaQueryWrapper<PlanGroup>().eq(PlanGroup::getPlanId, id));
        checkinMapper.delete(new LambdaQueryWrapper<CheckinRecord>().eq(CheckinRecord::getPlanId, id));
        planMapper.deleteById(id);
    }

    // ===== 系列 CRUD =====

    public List<PlanGroup> listGroups(Long planId) {
        return groupMapper.selectList(
                new LambdaQueryWrapper<PlanGroup>().eq(PlanGroup::getPlanId, planId).orderByAsc(PlanGroup::getSortOrder));
    }

    public PlanGroup createGroup(PlanGroup group) {
        group.setId(null);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.insert(group);
        return group;
    }

    public void updateGroup(Long id, PlanGroup group) {
        group.setId(id);
        group.setUpdatedAt(LocalDateTime.now());
        groupMapper.updateById(group);
    }

    @Transactional
    public void deleteGroup(Long id) {
        itemMapper.delete(new LambdaQueryWrapper<PlanItem>().eq(PlanItem::getGroupId, id));
        groupMapper.deleteById(id);
    }

    // ===== 子任务 CRUD =====

    public List<PlanItem> listItems(Long planId, Long groupId) {
        LambdaQueryWrapper<PlanItem> qw = new LambdaQueryWrapper<PlanItem>()
                .eq(PlanItem::getPlanId, planId)
                .orderByAsc(PlanItem::getSortOrder);
        if (groupId != null) qw.eq(PlanItem::getGroupId, groupId);
        return itemMapper.selectList(qw);
    }

    public PlanItem createItem(PlanItem item) {
        item.setId(null);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.insert(item);
        return item;
    }

    public void updateItem(Long id, PlanItem item) {
        item.setId(id);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
    }

    // ===== 打卡记录 =====

    public List<CheckinRecord> listCheckins(Long planId) {
        return checkinMapper.selectList(
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(planId != null, CheckinRecord::getPlanId, planId)
                        .orderByDesc(CheckinRecord::getDate));
    }

    public CheckinRecord createCheckin(CheckinRecord record) {
        record.setId(null);
        record.setCreatedAt(LocalDateTime.now());
        checkinMapper.insert(record);
        // 更新计划的实际时长
        if (record.getPlanId() != null && record.getDuration() != null && record.getDuration() > 0) {
            StudyPlan plan = planMapper.selectById(record.getPlanId());
            if (plan != null) {
                plan.setActualHours((plan.getActualHours() != null ? plan.getActualHours() : 0)
                        + record.getDuration() / 60.0);
                plan.setLastStudyTime(LocalDateTime.now());
                plan.setUpdatedAt(LocalDateTime.now());
                planMapper.updateById(plan);
            }
        }
        return record;
    }

    // ===== 批量导入 =====

    @Transactional
    public Map<String, Object> importPlans(List<StudyPlan> plans) {
        int created = 0;
        for (StudyPlan plan : plans) {
            createPlan(plan);
            created++;
        }
        return Map.of("created", created);
    }
}
