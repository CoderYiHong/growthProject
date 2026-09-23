package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String category;
    /** 后台上传的自定义封面。为空时前台使用 sourceCover。 */
    private String cover;
    /** CSDN 原文中的默认封面。 */
    private String sourceCover;
    private String sourceType;
    private Long sourceId;
    private String tags;
    private String content;
    private String status;
    private Integer isRecommended;
    private Integer isHot;
    private Integer views;
    private String readTime;
    private String author;
    private String sourceUrl;
    private Integer visible;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
