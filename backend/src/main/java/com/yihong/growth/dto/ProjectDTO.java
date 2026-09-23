package com.yihong.growth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProjectDTO {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "名称不超过100字符")
    private String name;

    @NotBlank(message = "分类不能为空")
    @Size(max = 30, message = "分类不超过30字符")
    private String category;

    @Size(max = 500, message = "简介不超过500字符")
    private String summary;

    private String description;
    private String techStack;

    @Size(max = 20, message = "状态值过长")
    private String status;

    private Boolean isRecommended;

    @Min(value = 0, message = "排序不小于0")
    private Integer sortOrder;

    @Size(max = 50, message = "角色不超过50字符")
    private String role;

    @Size(max = 50, message = "开发周期不超过50字符")
    private String devPeriod;

    @Size(max = 255, message = "演示链接过长")
    @Pattern(regexp = "^(https?://.*)?$", message = "演示链接格式不正确")
    private String demoUrl;

    @Size(max = 255, message = "源码链接过长")
    @Pattern(regexp = "^(https?://.*)?$", message = "源码链接格式不正确")
    private String sourceUrl;

    @Size(max = 255, message = "封面链接过长")
    private String cover;

    private String startDate;
    private String endDate;

    private String features;
    private String background;
    private String goals;
    private String architecture;
    private String challenges;
    private String results;
    private String review;

    @Min(value = 0, message = "可见性值无效")
    @Max(value = 1, message = "可见性值无效")
    private Integer visible;
}
