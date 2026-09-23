package com.yihong.growth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_plan")
public class StudyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String type;
    private String name;
    private String status;
    private String startDate;
    private String endDate;
    private Double plannedHours;
    private Double actualHours;
    private Integer progress;
    private Integer importance;
    private String note;
    private String subject;
    private String chapters;
    private Integer questionsDone;
    private Integer questionsTotal;
    private Integer accuracy;
    private Integer studyRound;
    private LocalDateTime lastStudyTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
