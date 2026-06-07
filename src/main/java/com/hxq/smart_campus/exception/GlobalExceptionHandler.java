package com.hxq.smart_campus.exception;

import com.hxq.smart_campus.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 判断是否为SSE流式请求
     */
    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return MediaType.TEXT_EVENT_STREAM_VALUE.equals(request.getContentType())
                || (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    /**
     * 资源不存在异常 → HTTP 404
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: errorCode={}, errorMessage={}", e.getErrorCode(), e.getErrorMessage());
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }


    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: errorCode={}, errorMessage={}", e.getErrorCode(), e.getErrorMessage());
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(CourseScheduleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleCourseScheduleException(CourseScheduleException e) {
        log.warn("排课异常: errorMessage={}", e.getErrorMessage());
        return Result.error(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error("PARAM_ERROR", "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", errorMessage);
        return Result.error("VALIDATION_ERROR", "参数校验失败: " + errorMessage);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", errorMessage);
        return Result.error("BIND_ERROR", "参数绑定失败: " + errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        log.error("系统异常: type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
        log.debug("系统异常详情", e);
        // 响应已提交（如SSE流式进行中），无法再写入，仅记录日志
        if (response.isCommitted()) {
            log.warn("响应已提交，无法返回错误信息到客户端: {}", request.getRequestURI());
            return null;
        }
        // SSE 端点返回 Flux<ServerSentEvent>，避免 Content-Type 冲突
        if (isSseRequest(request)) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("系统异常，请稍后重试")
                    .build());
        }
        return Result.error("SYSTEM_ERROR", "系统异常，请稍后重试");
    }
}