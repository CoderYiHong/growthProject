package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("growth_stage")
public class GrowthStage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String period;
    private String title;
    private String subtitle;
    private String icon;
    private String description;
    private String achievements;
    private String skills;
    private String color;
    private Integer sortOrder;
    private Integer visible;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
