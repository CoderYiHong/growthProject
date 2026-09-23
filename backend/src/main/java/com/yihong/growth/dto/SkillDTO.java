package com.yihong.growth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SkillDTO {
    @NotBlank(message = "技能名称不能为空")
    @Size(max = 50, message = "名称不超过50字符")
    private String name;

    @NotBlank(message = "分类不能为空")
    @Size(max = 30, message = "分类不超过30字符")
    private String category;

    @Size(max = 20, message = "等级不超过20字符")
    private String level;

    @Min(value = 0, message = "百分比不低于0")
    @Max(value = 100, message = "百分比不超过100")
    private Integer percentage;

    @Min(value = 0, message = "排序不小于0")
    private Integer sortOrder;

    private Integer visible;
}
