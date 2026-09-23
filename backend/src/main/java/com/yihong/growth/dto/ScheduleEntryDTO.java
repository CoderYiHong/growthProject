package com.yihong.growth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ScheduleEntryDTO {

    @NotNull(message = "请选择课程")
    private Long courseId;

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

    @Min(value = 0, message = "排序不能为负数")
    private Integer sortOrder;
}
