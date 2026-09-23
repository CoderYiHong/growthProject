package com.yihong.growth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CertificateDTO {
    public interface Create {}

    @NotBlank(message = "证书名称不能为空", groups = Create.class)
    @Size(max = 100, message = "名称不超过100字符")
    @Pattern(regexp = ".*\\S.*", message = "证书名称不能为空")
    private String name;

    @Size(max = 30, message = "分类不超过30字符")
    private String category;

    @Size(max = 100, message = "颁发机构不超过100字符")
    private String issuer;

    @Size(max = 30, message = "获得日期不超过30字符")
    private String issueDate;

    @Size(max = 500, message = "描述不超过500字符")
    private String description;

    @Size(max = 100, message = "证书编号不超过100字符")
    private String credentialNo;

    @Size(max = 255, message = "验证网址过长")
    @Pattern(regexp = "^(https?://.*)?$", message = "验证网址格式不正确")
    private String verificationUrl;

    @Size(max = 255, message = "图片地址过长")
    private String imageUrl;

    private Boolean visible;
    private Boolean featured;

    @Min(value = 0, message = "排序不能为负数")
    private Integer sortOrder;
}
