package com.gomain.cm.tool.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author caimeng
 * @date 2024/1/31 16:28
 */
@Getter
@ToString
@EqualsAndHashCode
public final class Result<T> implements Serializable {
    private T data = null;
    private Integer code;
    private String message;
    public static final Integer SUCCESS_CODE = 0;
    public static final Integer UNAUTHORIZED_CODE = 401;
    public static final Integer SERVER_ERROR_CODE = 500;
    public static final Integer PARAMETER_INVALID_CODE = 3001;
    public static final String PARAMETER_INVALID_MSG = "参数错误";
    public static final Integer SERVICE_INVALID_CODE = 3000;
    public static final String SERVICE_INVALID_MSG = "请求操作失败";
    public static final String SERVICE_SUCCESS_MSG = "操作成功";

    private Result() {
        this.code = SUCCESS_CODE;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.setCode(SUCCESS_CODE);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(SERVER_ERROR_CODE);
        return result;
    }

    public static <T>  Result<T> fail(Integer errCode, String errMsg) {
        Result<T>  result = fail();
        result.setCode(errCode);
        result.setMessage(errMsg);
        return result;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public Result<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

}