package com.free.aiknowledgehub.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description  统一返回结果类
 * @Author: Liberty-Swine
 * @Date 2026/4/6 16:45
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    // 成功
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }


    // 失败
    public static <T> Result<T> error() {
        return error(ResultCode.ERROR.getMessage());
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.ERROR.getCode());
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    // 自定义状态码
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(null);
        return result;
    }
}
