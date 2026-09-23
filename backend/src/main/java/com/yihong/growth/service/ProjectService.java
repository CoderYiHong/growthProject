package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yihong.growth.dto.ProjectDTO;
import com.yihong.growth.entity.Project;
import com.yihong.growth.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper mapper;

    /** 校验开始日期不晚于结束日期 */
    private void validateDates(ProjectDTO dto) {
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && !dto.getStartDate().isEmpty() && !dto.getEndDate().isEmpty()
                && dto.getStartDate().compareTo(dto.getEndDate()) > 0) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    public Project create(ProjectDTO dto) {
        validateDates(dto);
        Project p = toEntity(dto);
        p.setId(null);
        if (p.getSourceUrl() != null && p.getSourceUrl().startsWith("https://github.com/")) {
            p.setSourceType("github");
        }
        mapper.insert(p);
        return p;
    }

    public void update(Long id, ProjectDTO dto) {
        validateDates(dto);
        UpdateWrapper<Project> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        if (dto.getName() != null) uw.set("name", dto.getName());
        if (dto.getCategory() != null) uw.set("category", dto.getCategory());
        if (dto.getSummary() != null) uw.set("summary", dto.getSummary());
        if (dto.getDescription() != null) uw.set("description", dto.getDescription());
        if (dto.getTechStack() != null) uw.set("tech_stack", dto.getTechStack());
        if (dto.getStatus() != null) uw.set("status", dto.getStatus());
        if (dto.getIsRecommended() != null) uw.set("is_recommended", dto.getIsRecommended() ? 1 : 0);
        if (dto.getSortOrder() != null) uw.set("sort_order", dto.getSortOrder());
        if (dto.getRole() != null) uw.set("role", dto.getRole());
        if (dto.getDevPeriod() != null) uw.set("dev_period", dto.getDevPeriod());
        if (dto.getDemoUrl() != null) uw.set("demo_url", dto.getDemoUrl());
        if (dto.getSourceUrl() != null) uw.set("source_url", dto.getSourceUrl());
        if (dto.getCover() != null) uw.set("cover", dto.getCover());
        if (dto.getStartDate() != null) uw.set("start_date", dto.getStartDate());
        if (dto.getEndDate() != null) uw.set("end_date", dto.getEndDate());
        if (dto.getFeatures() != null) uw.set("features", dto.getFeatures());
        if (dto.getBackground() != null) uw.set("background", dto.getBackground());
        if (dto.getGoals() != null) uw.set("goals", dto.getGoals());
        if (dto.getArchitecture() != null) uw.set("architecture", dto.getArchitecture());
        if (dto.getChallenges() != null) uw.set("challenges", dto.getChallenges());
        if (dto.getResults() != null) uw.set("results", dto.getResults());
        if (dto.getReview() != null) uw.set("review", dto.getReview());
        if (dto.getVisible() != null) uw.set("visible", dto.getVisible());
        mapper.update(null, uw);
    }

    private Project toEntity(ProjectDTO dto) {
        Project p = new Project();
        p.setName(dto.getName()); p.setCategory(dto.getCategory());
        p.setSummary(dto.getSummary()); p.setDescription(dto.getDescription());
        p.setTechStack(dto.getTechStack()); p.setStatus(dto.getStatus());
        p.setIsRecommended(dto.getIsRecommended() != null && dto.getIsRecommended() ? 1 : 0);
        p.setSortOrder(dto.getSortOrder()); p.setRole(dto.getRole());
        p.setDevPeriod(dto.getDevPeriod()); p.setDemoUrl(dto.getDemoUrl());
        p.setSourceUrl(dto.getSourceUrl()); p.setCover(dto.getCover());
        p.setFeatures(dto.getFeatures()); p.setBackground(dto.getBackground());
        p.setGoals(dto.getGoals()); p.setArchitecture(dto.getArchitecture());
        p.setChallenges(dto.getChallenges()); p.setResults(dto.getResults());
        p.setReview(dto.getReview()); p.setVisible(dto.getVisible());
        return p;
    }
}
