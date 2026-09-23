package com.yihong.growth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactFormDTO {
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名过长")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱过长")
    private String email;

    @NotBlank(message = "主题不能为空")
    @Size(max = 200, message = "主题过长")
    private String subject;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 5000, message = "留言内容过长")
    private String content;
}
