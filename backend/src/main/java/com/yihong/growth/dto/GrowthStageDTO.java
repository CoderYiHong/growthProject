package com.yihong.growth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GrowthStageDTO {
    @NotBlank(message = "阶段标题不能为空")
    @Size(max = 50, message = "标题不超过50字符")
    private String title;

    @Size(max = 50, message = "时期不超过50字符")
    private String period;

    @Size(max = 100, message = "副标题不超过100字符")
    private String subtitle;

    private String icon;
    private String description;
    private String achievements;
    private String skills;
    private String color;
    private Integer sortOrder;
    private Integer visible;
}
