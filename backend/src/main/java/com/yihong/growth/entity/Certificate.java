package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("certificate")
public class Certificate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String issuer;
    private String issueDate;
    private String description;
    private String credentialNo;
    private String verificationUrl;
    private String imageUrl;
    private Integer visible;
    private Integer featured;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
