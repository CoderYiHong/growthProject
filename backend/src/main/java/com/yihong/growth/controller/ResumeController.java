package com.yihong.growth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yihong.growth.common.Result;
import com.yihong.growth.entity.Certificate;
import com.yihong.growth.entity.Project;
import com.yihong.growth.entity.Skill;
import com.yihong.growth.mapper.CertificateMapper;
import com.yihong.growth.mapper.ProjectMapper;
import com.yihong.growth.mapper.SkillMapper;
import com.yihong.growth.service.SettingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线简历公开接口 — 聚合多表数据，替代前端 mock。
 */
@RestController
@RequestMapping("/api/public/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final SettingService settingService;
    private final SkillMapper skillMapper;
    private final ProjectMapper projectMapper;
    private final CertificateMapper certificateMapper;
    private final ObjectMapper objectMapper;

    @GetMapping
    public Result<Map<String, Object>> getResume() {
        Map<String, String> settings = settingService.getAll();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("profile", buildProfile(settings));
        data.put("abilities", parseJsonList(settings.get("resume_abilities"), List.of()));
        data.put("skills", buildSkills());
        data.put("projects", buildProjects());
        data.put("certificates", buildCertificates());
        data.put("honors", parseJsonList(settings.get("resume_honors"), List.of()));
        data.put("contact", buildContact(settings));
        return Result.ok(data);
    }

    private Map<String, Object> buildProfile(Map<String, String> s) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", s.getOrDefault("profile_nickname", "YiHong"));
        profile.put("avatar", s.getOrDefault("profile_avatar", ""));
        profile.put("position", s.getOrDefault("resume_position", "全栈开发工程师"));
        profile.put("degree", s.getOrDefault("resume_degree", "本科"));
        profile.put("major", s.getOrDefault("resume_major", "数据科学与大数据技术"));
        profile.put("location", s.getOrDefault("resume_location", "中国"));
        profile.put("email", s.getOrDefault("profile_email", ""));
        profile.put("phone", s.getOrDefault("resume_phone", ""));
        profile.put("github", s.getOrDefault("resume_github", ""));
        profile.put("csdn", s.getOrDefault("resume_csdn", ""));
        profile.put("introduction", s.getOrDefault("resume_introduction", ""));
        profile.put("technicalDirection", parseJsonList(
                s.get("resume_technical_direction"),
                List.of("全栈开发", "数据分析", "人工智能应用")));
        return profile;
    }

    private List<Map<String, Object>> buildSkills() {
        List<Skill> skills = skillMapper.selectList(
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getVisible, 1)
                        .orderByAsc(Skill::getSortOrder));
        return skills.stream().map(sk -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", sk.getId());
            map.put("category", sk.getCategory());
            map.put("name", sk.getName());
            map.put("level", sk.getLevel());
            map.put("percentage", sk.getPercentage());
            map.put("sort", sk.getSortOrder());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildProjects() {
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getSourceType, "github")
                        .eq(Project::getVisible, 1)
                        .orderByAsc(Project::getSortOrder));
        return projects.stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("cover", p.getCover() != null ? p.getCover() : p.getSourceCover());
            map.put("summary", p.getSummary());
            map.put("responsibility", p.getRole());
            map.put("technologies", parseTechStack(p.getTechStack()));
            map.put("previewUrl", p.getDemoUrl() != null ? p.getDemoUrl() : "");
            map.put("repositoryUrl", p.getSourceUrl() != null ? p.getSourceUrl() : "");
            map.put("featured", p.getIsRecommended() != null && p.getIsRecommended() == 1);
            map.put("visible", true);
            map.put("sort", p.getSortOrder());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildCertificates() {
        List<Certificate> certs = certificateMapper.selectList(
                new LambdaQueryWrapper<Certificate>()
                        .eq(Certificate::getVisible, 1)
                        .orderByDesc(Certificate::getFeatured)
                        .orderByAsc(Certificate::getSortOrder));
        return certs.stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("category", c.getCategory());
            map.put("issuingOrganization", c.getIssuer());
            map.put("issueDate", c.getIssueDate());
            map.put("description", c.getDescription());
            map.put("credentialNo", c.getCredentialNo());
            map.put("imageUrl", c.getImageUrl() != null ? c.getImageUrl() : "");
            map.put("featured", c.getFeatured() != null && c.getFeatured() == 1);
            map.put("visible", true);
            map.put("sort", c.getSortOrder());
            return map;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> buildContact(Map<String, String> s) {
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("email", s.getOrDefault("profile_email", ""));
        contact.put("github", s.getOrDefault("resume_github", ""));
        contact.put("csdn", s.getOrDefault("resume_csdn", ""));
        contact.put("location", s.getOrDefault("resume_location", "中国"));
        return contact;
    }

    private List<String> parseTechStack(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(value);
        }
    }

    private <T> T parseJsonList(String json, T fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            return objectMapper.readValue(json, new TypeReference<T>() {});
        } catch (JsonProcessingException e) {
            return fallback;
        }
    }
}
