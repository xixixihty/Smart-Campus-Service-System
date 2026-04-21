package com.hxq.smart_campus.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {

    private String code;
    private String msg; // 修复拼写错误
    private T data;

    /**
     * 无参成功返回结果
     */
    public static <T> Result<T> success(){
        return new Result<>("200", "success", null);
    }

    /**
     * 有参成功返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>("200", "success", data);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> error(String msg) { // 修复拼写错误
        return new Result<>("0", msg, null);
    }
}