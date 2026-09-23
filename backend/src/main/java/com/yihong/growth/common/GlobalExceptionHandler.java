package com.yihong.growth.common;

import com.yihong.growth.service.impl.AuthServiceImpl.AuthenticationException;
import com.yihong.growth.service.CsdnArticleSyncService.CsdnSyncException;
import com.yihong.growth.service.GithubProjectSyncService.GithubSyncException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 统一返回 JSON 格式的 Result，HTTP 状态码与业务 code 一致
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败 → 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    /** 参数校验异常 → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArg(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    /** 认证失败 → 401 */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuth(AuthenticationException e) {
        return Result.error(401, e.getMessage());
    }

    /** CSDN 同步失败 → 502 */
    @ExceptionHandler(CsdnSyncException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<?> handleCsdnSync(CsdnSyncException e) {
        log.warn("CSDN 同步失败: {}", e.getMessage());
        return Result.error(502, e.getMessage());
    }

    /** GitHub 同步失败 → 502 */
    @ExceptionHandler(GithubSyncException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Result<?> handleGithubSync(GithubSyncException e) {
        log.warn("GitHub 同步失败: {}", e.getMessage());
        return Result.error(502, e.getMessage());
    }

    /** 文件过大 → 413 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<?> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.error(413, "上传文件过大，单个文件不超过10MB");
    }

    /** JSON 格式错误 → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(400, "请求数据格式错误，请检查JSON格式");
    }

    /** 路径参数类型不匹配 → 400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "请求参数类型不正确: " + e.getName());
    }

    /** 通用业务异常 → 500（不泄露内部消息） */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleRuntime(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(500, "服务器内部错误");
    }

    /** 兜底 → 500 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleGeneral(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        return Result.error(500, "服务器内部错误");
    }
}
