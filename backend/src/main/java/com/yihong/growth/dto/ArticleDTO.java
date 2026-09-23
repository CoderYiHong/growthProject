package com.yihong.growth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleDTO {
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "标题不超过200字符")
    private String title;

    @Size(max = 500, message = "摘要不超过500字符")
    private String summary;

    private String category;
    private String cover;
    private String tags;
    private String content;
    private String status;
    private Boolean isRecommended;
    private Boolean isHot;
    private Integer visible;
    private String author;
    private String sourceUrl;
}
