package com.hxq.smart_campus.exception;

/**
 * 资源不存在异常
 * 由全局异常处理器拦截，统一返回 HTTP 404
 *
 * @author XiongQi He
 * @since 2026-05-05
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String errorMessage) {
        super("NOT_FOUND", errorMessage);
    }

    public NotFoundException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public NotFoundException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    /**
     * 资源不存在快捷工厂方法
     *
     * @param resourceName 资源名称（如"请假申请"）
     * @param id           资源ID
     * @return NotFoundException
     */
    public static NotFoundException notFound(String resourceName, Long id) {
        return new NotFoundException(resourceName + "不存在: id=" + id);
    }

    /**
     * 资源不存在快捷工厂方法（无ID场景）
     *
     * @param message 描述信息
     * @return NotFoundException
     */
    public static NotFoundException notFound(String message) {
        return new NotFoundException(message);
    }
}