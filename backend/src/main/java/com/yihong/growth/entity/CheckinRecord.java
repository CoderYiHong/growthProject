package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long itemId;
    private String date;
    private Integer duration;
    private String content;
    private String note;
    private LocalDateTime createdAt;
}
