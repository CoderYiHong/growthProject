package com.yihong.growth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseDTO {

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称不超过100字符")
    private String name;

    @Size(max = 50, message = "教师姓名不超过50字符")
    private String teacher;

    @Size(max = 30, message = "职称不超过30字符")
    private String title;

    @DecimalMin(value = "0", message = "学分不能为负数")
    @DecimalMax(value = "99.99", message = "学分数值过大")
    private BigDecimal credit;

    @Min(value = 0, message = "总学时不能为负数")
    @Max(value = 1000, message = "总学时数值过大")
    private Integer totalHours;

    @Min(value = 0, message = "颜色编号不正确")
    @Max(value = 8, message = "颜色编号不正确")
    private Integer color;

    @Min(value = 0, message = "排序不能为负数")
    private Integer sortOrder;
}
