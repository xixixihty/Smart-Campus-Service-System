package com.hxq.smart_campus.exception;

import com.hxq.smart_campus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("SYSTEM_ERROR", "系统异常，请稍后重试");
    }
}