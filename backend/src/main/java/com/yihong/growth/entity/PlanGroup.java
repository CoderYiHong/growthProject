package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plan_group")
public class PlanGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private String name;
    private Integer sortOrder;
    private String startDate;
    private String endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
