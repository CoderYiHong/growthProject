package com.yihong.growth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * Excel 导入课表请求 — 全量替换
 * entries 中 courseIndex 指向 courses 数组下标，而非数据库 id
 */
@Data
public class ImportTimetableRequest {

    @Valid
    @Size(max = 100, message = "课程数量超出限制")
    private List<CourseItem> courses;

    @Valid
    @Size(max = 500, message = "排课数量超出限制")
    private List<EntryItem> entries;

    @Data
    public static class CourseItem {
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

        @Min(value = 1, message = "颜色编号不正确")
        @Max(value = 8, message = "颜色编号不正确")
        private Integer color;
    }

    @Data
    public static class EntryItem {
        @NotNull(message = "排课缺少课程引用")
        @Min(value = 0, message = "课程引用不正确")
        private Integer courseIndex;

        @NotNull(message = "请选择星期")
        @Min(value = 1, message = "星期不正确")
        @Max(value = 7, message = "星期不正确")
        private Integer dayOfWeek;

        @NotNull(message = "请选择节次")
        @Min(value = 0, message = "节次不正确")
        @Max(value = 3, message = "节次不正确")
        private Integer slot;

        @NotNull(message = "请填写起始周")
        @Min(value = 1, message = "起始周不正确")
        private Integer startWeek;

        @NotNull(message = "请填写结束周")
        @Min(value = 1, message = "结束周不正确")
        @Max(value = 99, message = "结束周不正确")
        private Integer endWeek;

        @Size(max = 200, message = "地点不超过200字符")
        private String room;
    }
}
