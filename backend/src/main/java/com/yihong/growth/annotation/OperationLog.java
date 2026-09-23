package com.yihong.growth.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 — 标注在 Controller 方法上自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作描述，如 "新增项目"、"删除文章" */
    String value() default "";

    /** 操作模块，如 "项目管理"、"文章管理" */
    String module() default "";
}
