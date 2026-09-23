package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("schedule_entry")
public class ScheduleEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Integer dayOfWeek;
    private Integer slot;
    private Integer startWeek;
    private Integer endWeek;
    private String room;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
