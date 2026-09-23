package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    /** 后台上传的自定义封面。为空时前台使用 sourceCover。 */
    private String cover;
    /** GitHub 仓库的 Open Graph 默认封面。 */
    private String sourceCover;
    private String sourceType;
    private Long sourceId;
    private String summary;
    private String description;
    private String techStack;
    private String status;
    private Integer isRecommended;
    private Integer sortOrder;
    private String role;
    private String devPeriod;
    private String demoUrl;
    private String sourceUrl;
    private String features;
    private String background;
    private String goals;
    private String architecture;
    private String challenges;
    private String results;
    private String review;
    private Integer visible;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
