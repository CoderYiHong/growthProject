package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plan_item")
public class PlanItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long planId;
    private String title;
    private String status;
    private Integer estPomodoros;
    private Integer actualPomodoros;
    private Integer weight;
    private Integer sortOrder;
    private String startDate;
    private String endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
